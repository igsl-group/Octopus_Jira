package com.igsl.configmigration.workflow.mapper;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowModuleDescriptor;
import com.atlassian.jira.plugin.workflow.UpdateIssueFieldFunctionPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.PluginAccessorHelper;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.factories.AbstractPluginFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.igsl.configmigration.workflow.mapper.generated.Function;
import com.igsl.configmigration.workflow.mapper.generated.Workflow;

public class WorkflowMapper extends JiraWebActionSupport {

	public static final PluginAccessor PLUGIN_ACCESSOR = ComponentAccessor.getPluginAccessor();
	public static final ModuleDescriptorFactory MODULE_DESCRIPTOR_FACTORY = ComponentAccessor.getComponent(ModuleDescriptorFactory.class);
	public static final String WORKFLOW_FUNCTION_DESCRIPTOR_TYPE = "workflow-function";

	private static final String SESSION_DATA = WorkflowMapper.class.getCanonicalName() + ".SessionData";
	static class SessionData {
		public String selectedWorkflow;
		public String xml;
		public Workflow workflow;
	}
	private SessionData sessionData;
	
	private void getSessionData(HttpServletRequest req) {
		Object data = req.getSession().getAttribute(SESSION_DATA);
		if (data != null && data instanceof SessionData) {
			this.sessionData = (SessionData) data;
		} else {
			// Create new
			this.sessionData = new SessionData();
			saveSessionData(req);
		}
	}
	
	private void saveSessionData(HttpServletRequest req) {
		req.getSession().setAttribute(SESSION_DATA, this.sessionData);
	}
	
	private static final Logger LOGGER = Logger.getLogger(WorkflowMapper.class);
	private static final WorkflowManager MANAGER = ComponentAccessor.getWorkflowManager();
	
	private static final String PARAM_ACTION = "action";
	
	// Load workflow action
	private static final String ACTION_LOAD_WORKFLOW = "loadWorkflow";
	private static final String PARAM_WORKFLOW = "workflow";
	
	/**
	 * Notes:
	 * Function to load a workflow as example, extracting list of items that can be mapped.
	 * Has an internal list of mapping items.
	 * Has an user-customizable list of mapping items.
	 * 
	 * For each item, check the Arg items and provide function to test map them.
	 */
	
	private static final String XML_HEADER = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"<!DOCTYPE workflow PUBLIC \"-//OpenSymphony Group//DTD OSWorkflow 2.8//EN\" \"http://www.opensymphony.com/osworkflow/workflow_2_8.dtd\">\r\n";
	
	private static SAXParserFactory saxParserFactory; 
	
	static {
		saxParserFactory = SAXParserFactory.newInstance();
		// Disable DTD validation
		try {
			saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (SAXNotRecognizedException | SAXNotSupportedException | ParserConfigurationException e) {
			LOGGER.error("Error configuring SAX Parser", e);
		}
	}
	
	public String getSelectedWorkflow() {
		return this.sessionData.selectedWorkflow;
	}
	
	public String getXml() {
		return this.sessionData.xml;
	}
	
	public Workflow getWorkflow() {
		return this.sessionData.workflow;
	}
	
	@ComponentImport
	private final ActiveObjects ao;

	private static final long serialVersionUID = 1L;

	public WorkflowMapper(@ComponentImport ActiveObjects ao) {
		LOGGER.debug("Inject ActiveObjects: " + ao);
		this.ao = ao;
	}
	
	/**
	 * Get list of published workflows
	 * @return Collection of JiraWorkflow
	 */
	public Collection<JiraWorkflow> getWorkflows() {
		return MANAGER.getWorkflows();
	}
	
	private Workflow parseWorkflow(JiraWorkflow wf) throws Exception {
		String xml = WorkflowUtil.convertDescriptorToXML(wf.getDescriptor());
		Source xmlSource = new SAXSource(saxParserFactory.newSAXParser().getXMLReader(),
                new InputSource(new StringReader(xml)));
		JAXBContext ctx = JAXBContext.newInstance(Workflow.class);
		Unmarshaller parser = ctx.createUnmarshaller();
		Workflow result = (Workflow) parser.unmarshal(xmlSource);
		return result;
	}
	
	private String serializeWorkflow(Workflow wf) throws Exception {
		JAXBContext ctx = JAXBContext.newInstance(Workflow.class);
		Marshaller marshaller = ctx.createMarshaller();
		// Set XML header
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		sw.write(XML_HEADER);
		marshaller.marshal(wf, sw);
		return sw.toString();
	}
	
	@SuppressWarnings("rawtypes")
	public static String getFunctionDisplayName(String className) {
		LOGGER.debug("FuncDesc looking up: " + className);
		Collection<ModuleDescriptor> moduleDescriptors = 
				PluginAccessorHelper.getEnabledModuleDescriptorsByType(PLUGIN_ACCESSOR, MODULE_DESCRIPTOR_FACTORY, WORKFLOW_FUNCTION_DESCRIPTOR_TYPE);
		for (ModuleDescriptor desc : moduleDescriptors) {
			/*
			 * Note: 
			 * Jira's API did NOT expose the function class anywhere from the ModuleDescriptor.
			 * The function class is stored in a protected field AbstractWorkflowModuleDescriptor.implementationClass
			 * 
			 * So our only solution is to either read it with reflection, or go parse the plugin manifest.
			 * We choose the former.
			 */
			try {
				Field field = AbstractWorkflowModuleDescriptor.class.getDeclaredField("implementationClass");
				if (field != null) {
					field.setAccessible(true);
					Class<?> functionClass = (Class<?>) field.get(desc);
					LOGGER.debug("Plugin " + desc.getPlugin().getName() + " module " + desc.getName() + " implClass: " + functionClass.getCanonicalName());
					if (functionClass.getCanonicalName().equals(className)) {
						return desc.getDisplayName();
					}
					field.setAccessible(false);
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				LOGGER.error("Failed to read function-class", e);
			}
		}
		return className;
	}
	
	@Override
	protected void doValidation() {
		LOGGER.debug("doValidation");
	}

	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		HttpServletRequest req = this.getHttpRequest();
		getSessionData(req);
		
		String action = req.getParameter(PARAM_ACTION);
		LOGGER.debug("action: " + action);
		
		if (ACTION_LOAD_WORKFLOW.equals(action)) {
			this.sessionData.selectedWorkflow = req.getParameter(PARAM_WORKFLOW);
			LOGGER.debug("Selecting workflow: " + this.sessionData.selectedWorkflow);
			JiraWorkflow workflow = MANAGER.getWorkflow(this.sessionData.selectedWorkflow);
			this.sessionData.workflow = parseWorkflow(workflow);
			this.sessionData.xml = serializeWorkflow(this.sessionData.workflow);
			LOGGER.debug("Xml: " + this.sessionData.xml);
		}
		
		return JiraWebActionSupport.INPUT;
	}

}
