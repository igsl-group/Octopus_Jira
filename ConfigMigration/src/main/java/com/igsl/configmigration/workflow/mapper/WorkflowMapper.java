package com.igsl.configmigration.workflow.mapper;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;

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
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.igsl.configmigration.workflow.mapper.nodes.Workflow;

public class WorkflowMapper extends JiraWebActionSupport {

	static class SessionData {
		// TODO
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
	
	private String selectedWorkflow;
	public String getSelectedWorkflow() {
		return selectedWorkflow;
	}
	
	private String xml;
	public String getXml() {
		return xml;
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
	
	@Override
	protected void doValidation() {
		LOGGER.debug("doValidation");
	}

	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		HttpServletRequest req = this.getHttpRequest();
		String action = req.getParameter(PARAM_ACTION);
		LOGGER.debug("action: " + action);
		if (ACTION_LOAD_WORKFLOW.equals(action)) {
			this.selectedWorkflow = req.getParameter(PARAM_WORKFLOW);
			LOGGER.debug("Selecting workflow: " + selectedWorkflow);
			JiraWorkflow workflow = MANAGER.getWorkflow(selectedWorkflow);
			Workflow wf = parseWorkflow(workflow);
			this.xml = serializeWorkflow(wf);
			LOGGER.debug("Xml: " + xml);
		}
		return JiraWebActionSupport.INPUT;
	}

}
