package com.igsl.configmigration.workflow.mapper;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowModuleDescriptor;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.PluginAccessorHelper;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.workflow.mapper.generated.Workflow;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigWrapper;

public class WorkflowMapper extends JiraWebActionSupport {

	public static final PluginAccessor PLUGIN_ACCESSOR = ComponentAccessor.getPluginAccessor();
	public static final ModuleDescriptorFactory MODULE_DESCRIPTOR_FACTORY = ComponentAccessor.getComponent(ModuleDescriptorFactory.class);
	public static final String WORKFLOW_FUNCTION_DESCRIPTOR_TYPE = "workflow-function";

	private static final String SESSION_DATA = WorkflowMapper.class.getCanonicalName() + ".SessionData";
	
	static class SessionData {
		public String selectedWorkflow;
		public String xml;
		public Workflow workflow;
		public Map<String, WorkflowPartWrapper> workflowPartsRegistry = new HashMap<>();	// Key is hashCode
		public WorkflowPartWrapper part;	// Editing part
		public MapperConfigWrapper mapping;	// Mapping being edited
		public Iterator<?> partIterator;	// Matches for mapping
		public String partIteratorXPath;	// XPath used for search
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
	
	// Edit part action
	private static final String ACTION_LOAD_PART = "loadPart";
	private static final String PARAM_PART = "part";
	
	// Edit mapping
	private static final String ACTION_LOAD_MAPPING = "loadMapping";
	private static final String ACTION_CREATE_MAPPING = "createMapping";
	private static final String ACTION_SAVE_MAPPING = "saveMapping";
	private static final String ACTION_DELETE_MAPPING = "deleteMapping";
	private static final String PARAM_MAPPING = "mapping";
	private static final String PARAM_MAPPING_DESCRIPTION = "mappingDesc";
	private static final String PARAM_MAPPING_OBJECT_TYPE = "mappingObjectType";
	private static final String PARAM_MAPPING_ARRAY = "mappingArray";
	private static final String PARAM_MAPPING_DISABLED = "mappingDisabled";
	private static final String PARAM_MAPPING_XPATH = "mappingXPath";
	
	// Part matching
	private static final String ACTION_RESET_PART = "resetPartSearch";
	private static final String ACTION_NEXT_PART = "nextPart";
	
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
	
	private String searchResult;
	public String getSearchResult() {
		return this.searchResult;
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

	public Map<String, WorkflowPartWrapper> getMappableWorkflowParts() {
		return this.sessionData.workflowPartsRegistry;
	}
	
	private void registerWorkflowParts(Workflow wf) {
		this.sessionData.workflowPartsRegistry.clear();
		registerWorkflowParts((WorkflowPart) wf, "");
	}
	private void registerWorkflowParts(WorkflowPart part, String parentXPath) {
		this.sessionData.workflowPartsRegistry.put(Integer.toString(part.hashCode()), new WorkflowPartWrapper(part, parentXPath));
		for (WorkflowPart child : part.getChildren()) {
			String path;
			if (parentXPath != null && parentXPath.length() != 0) {
				path = parentXPath + "/" + part.getPartXPath();
			} else {
				path = part.getPartXPath();
			}
			registerWorkflowParts(child, path);
		}
	}
	
	public WorkflowPartWrapper getEditingWorkflowPartWrapper() {
		return this.sessionData.part;
	}
	
	public Map<String, String> getObjectTypes() {
		Map<String, String> result = new TreeMap<>();
		for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(false)) {
			result.put(util.getName(), util.getDTOClass().getCanonicalName());
		}
		return result;
	}
	
	public Map<String, MapperConfigWrapper> getMappings() {
		return MapperConfigUtil.getMapperConfigs(this.ao);
	}
	
	public MapperConfigWrapper getMapping() {
		return this.sessionData.mapping;
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
	
	/**
	 * Jira workflow stores a workflow function with its class name.
	 * 
	 * But display name and description are actually stored in the workflow function's factory.
	 * The mapping of workflow function to its factory is stored in plugin manifest only.
	 * 
	 * This method takes workflow function class name and try to find a matching display name.
	 * @param className
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getFunctionDisplayName(String className) {
		Collection<ModuleDescriptor> moduleDescriptors = 
				PluginAccessorHelper.getEnabledModuleDescriptorsByType(PLUGIN_ACCESSOR, MODULE_DESCRIPTOR_FACTORY, WORKFLOW_FUNCTION_DESCRIPTOR_TYPE);
		for (ModuleDescriptor desc : moduleDescriptors) {
			/*
			 * Note: 
			 * Jira's API did NOT expose the function class anywhere from the ModuleDescriptor.
			 * The function class is stored in a protected field AbstractWorkflowModuleDescriptor.implementationClass (type Class).
			 * 
			 * So our only solution is to either read it with reflection, or parse the plugin manifest.
			 * We choose the former.
			 */
			if (desc instanceof AbstractWorkflowModuleDescriptor) {
				try {
					Field field = AbstractWorkflowModuleDescriptor.class.getDeclaredField("implementationClass");
					if (field != null) {
						field.setAccessible(true);
						Class<?> functionClass = (Class<?>) field.get(desc);
						field.setAccessible(false);
						if (functionClass.getCanonicalName().equals(className)) {
							return desc.getDisplayName();
						}
					}
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					LOGGER.error("Failed to read function-class", e);
				}
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
		
		// Update mapping data in session
		if (this.sessionData.mapping != null) {
			Boolean array = Boolean.parseBoolean(req.getParameter(PARAM_MAPPING_ARRAY));
			this.sessionData.mapping.setArray(array);
			Boolean disabled = Boolean.parseBoolean(req.getParameter(PARAM_MAPPING_DISABLED));
			this.sessionData.mapping.setDisabled(disabled);
			this.sessionData.mapping.setDescription(req.getParameter(PARAM_MAPPING_DESCRIPTION));
			this.sessionData.mapping.setObjectType(req.getParameter(PARAM_MAPPING_OBJECT_TYPE));
			this.sessionData.mapping.setxPath(req.getParameter(PARAM_MAPPING_XPATH));
		}
		
		if (ACTION_LOAD_WORKFLOW.equals(action)) {
			this.sessionData.selectedWorkflow = req.getParameter(PARAM_WORKFLOW);
			LOGGER.debug("Selecting workflow: " + this.sessionData.selectedWorkflow);
			this.sessionData.workflow = null;
			this.sessionData.xml = null;
			this.sessionData.part = null;
			this.sessionData.workflowPartsRegistry.clear();
			this.sessionData.partIterator = null;
			this.sessionData.partIteratorXPath = null;
			if (this.sessionData.selectedWorkflow != null && this.sessionData.selectedWorkflow.length() != 0) {
				JiraWorkflow workflow = MANAGER.getWorkflow(this.sessionData.selectedWorkflow);
				if (workflow != null) {
					this.sessionData.workflow = parseWorkflow(workflow);
					this.sessionData.xml = serializeWorkflow(this.sessionData.workflow);
					registerWorkflowParts(this.sessionData.workflow);
				}
			}
		} else if (ACTION_LOAD_PART.equals(action)) {
			WorkflowPartWrapper part = this.sessionData.workflowPartsRegistry.get(req.getParameter(PARAM_PART));
			if (part != null) {
				this.sessionData.part = part;
				this.sessionData.mapping = MapperConfigUtil.getMapperConfigByXPath(this.ao, part.getXPath());
				if (this.sessionData.mapping == null) {
					// No existing mapping, create a blank one with prefilled XPath
					this.sessionData.mapping = new MapperConfigWrapper();
					this.sessionData.mapping.setxPath(this.sessionData.part.getXPath());
				}
			}
		} else if (ACTION_LOAD_MAPPING.equals(action)) {
			String mappingId = req.getParameter(PARAM_MAPPING);
			if (mappingId != null && mappingId.length() != 0) {
				this.sessionData.mapping = MapperConfigUtil.getMapperConfigById(this.ao, mappingId);
			} else {
				this.sessionData.mapping = null;
			}
			this.sessionData.partIterator = null;
			this.sessionData.partIteratorXPath = null;
		} else if (ACTION_CREATE_MAPPING.equals(action)) {
			this.sessionData.mapping = new MapperConfigWrapper();
			this.sessionData.partIterator = null;
			this.sessionData.partIteratorXPath = null;
		} else if (ACTION_SAVE_MAPPING.equals(action)) {
			if (this.sessionData.mapping != null) {
				MapperConfigUtil.saveMapperConfig(this.ao, this.sessionData.mapping);
			}
		} else if (ACTION_DELETE_MAPPING.equals(action)) {
			if (this.sessionData.mapping != null) {
				MapperConfigUtil.deleteMapperConfig(this.ao, this.sessionData.mapping);
				this.sessionData.mapping = null;
				this.sessionData.partIterator = null;
				this.sessionData.partIteratorXPath = null;
			}
		} else if (ACTION_RESET_PART.equals(action)) {
			this.sessionData.partIterator = null;
			this.sessionData.partIteratorXPath = null;
		} else if (ACTION_NEXT_PART.equals(action)) {
			this.searchResult = "";
			String xPath = req.getParameter(PARAM_MAPPING_XPATH);
			if (this.sessionData.workflow != null && this.sessionData.mapping != null) {
				if (this.sessionData.partIteratorXPath == null ||
					!this.sessionData.partIteratorXPath.equals(xPath)) {	
					// Need a new search
					this.sessionData.partIterator = null;
				}
				this.sessionData.partIteratorXPath = xPath;
				if (this.sessionData.partIteratorXPath != null && 
					this.sessionData.partIteratorXPath.length() != 0) {
					if (this.sessionData.partIterator == null) {
						// Start new search
						JXPathContext ctx = JXPathContext.newContext(this.sessionData.workflow);
						this.sessionData.partIterator = ctx.iterate(this.sessionData.partIteratorXPath);
					}
					// Find next result
					if (this.sessionData.partIterator.hasNext()) {
						WorkflowPart part = (WorkflowPart) this.sessionData.partIterator.next();
						this.sessionData.part = this.sessionData.workflowPartsRegistry.get(Integer.toString(part.hashCode()));
						this.searchResult = "Next match located";
					} else {
						// No more result
						this.sessionData.partIterator = null;
						this.searchResult = "No match found";
					}
				} else {
					this.searchResult = "Please enter a XPath";
				}
			} else {
				this.searchResult = "Please select a workflow";
			}
		}
		
		return JiraWebActionSupport.INPUT;
	}

}
