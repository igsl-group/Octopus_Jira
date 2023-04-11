package com.igsl.configmigration.workflow;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraDraftWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.status.StatusDTO;
import com.igsl.configmigration.status.StatusUtil;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class WorkflowUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(WorkflowUtil.class);
	private static final WorkflowManager WORKFLOW_MANAGER = ComponentAccessor.getWorkflowManager();
	
	@Override
	public boolean isDefaultObject(JiraConfigDTO dto) {
		if (dto != null && 
			(
				JiraConfigDTO.NULL_KEY.equals(dto.getUniqueKey()) || 
				WorkflowDTO2.DEFAULT_WORKFLOW.equals(dto.getUniqueKey())
			)) {
			return true;
		}
		return false;
	}
	
	public static JiraConfigDTO getWorkflowDetails(Object item) throws Exception {
		JiraConfigDTO result = null;
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		StackTraceElement e = ste[2];
		LOGGER.debug("Invoked from: " + 
				e.getClassName() + "." + e.getMethodName() + 
				" (" + e.getFileName() + "@" + e.getLineNumber() + ")");		
		LOGGER.debug("getWorkflowDetails encountered: " + item.getClass().getCanonicalName());
		if (item instanceof AbstractDescriptor) {
			Class<? extends JiraConfigDTO> c = JiraConfigTypeRegistry.getDTOClass(item.getClass());
			if (c != null) {
				JiraConfigDTO dto = c.newInstance();
				dto.setJiraObject(item);
				result = dto;
			}
		}
		return result;
	}
	
	@Override
	public String getName() {
		return "Workflow";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		JiraWorkflow wf = WORKFLOW_MANAGER.getWorkflow(id);
		if (wf != null) {
			WorkflowDTO2 item = new WorkflowDTO2();
			item.setJiraObject(wf);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (JiraWorkflow wf : WORKFLOW_MANAGER.getWorkflows()) {
			if (uniqueKey.equals(wf.getName())) {
				WorkflowDTO2 item = new WorkflowDTO2();
				item.setJiraObject(wf);
				return item;
			}
		}
		return null;
	}

	@Override
	public MergeResult merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		LOGGER.debug("merge starts");
		StatusUtil statusUtil = (StatusUtil) JiraConfigTypeRegistry.getConfigUtil(StatusUtil.class);
		ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		WorkflowDTO2 original = null;
		if (oldItem != null) {
			original = (WorkflowDTO2) oldItem;
		} else {
			original = (WorkflowDTO2) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		WorkflowDTO2 src = (WorkflowDTO2) newItem;
		// Remap data in XML
		// status
		// //workflow/steps/step[name]
		// //workflow/steps/step/meta[name='jira.status.id']
		try {
			LOGGER.debug("Workflow XML: " + src.getXml());
			ByteArrayInputStream bais = new ByteArrayInputStream(src.getXml().getBytes());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// Ignore DTD
			factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(bais);
			LOGGER.debug("Workflow XML parsed");
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList) 
				xpath.evaluate("/workflow/steps/step", doc, XPathConstants.NODESET);
			LOGGER.debug("Step count: " + nodes.getLength());
			for (int idx = 0; idx < nodes.getLength(); idx++) {
				Node step = nodes.item(idx);
				Node statusNameNode = (Node)
					xpath.evaluate("@name", step, XPathConstants.NODE);
				Node statusIdNode = (Node) 
					xpath.evaluate("meta[@name='jira.status.id']", nodes.item(idx), XPathConstants.NODE);
				if (statusNameNode != null && statusIdNode != null) {
					String statusName = statusNameNode.getTextContent();
					String statusId = statusIdNode.getTextContent();
					LOGGER.debug("Status: " + statusName);
					LOGGER.debug("Status ID: " + statusId);
					StatusDTO statusFound = (StatusDTO) statusUtil.findByUniqueKey(statusName);
					if (statusFound != null) {
						LOGGER.debug("Remapping status " + statusName + " ID from " + statusId + " to " + statusFound.getId());
						statusIdNode.setTextContent(statusFound.getId());
					} else {
						LOGGER.warn("Status " + statusName + " is not found on current instance");
					}
				} else {
					LOGGER.error("Step is missing data");
				}
			}
			// Convert back to string
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
		    StreamResult r = new StreamResult(writer);
		    TransformerFactory tf = TransformerFactory.newInstance();
		    Transformer transformer = tf.newTransformer();
		    transformer.transform(domSource, r);
		    // Attach DTD
		    String newXML = writer.toString();
		    String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		    String dtd = "<!DOCTYPE workflow PUBLIC \"-//OpenSymphony Group//DTD OSWorkflow 2.8//EN\" \"http://www.opensymphony.com/osworkflow/workflow_2_8.dtd\">";
		    int index = newXML.indexOf(header);
		    if (index != -1) {
		    	newXML = newXML.substring(0, index + header.length()) + dtd + newXML.substring(index + header.length());
		    }
		    src.setXml(newXML);
		    LOGGER.debug("Workflow XML updated: " + src.getXml());
		} catch (Exception ex) {
			LOGGER.error("Error updating Status in Workflow XML", ex);
		}
		// TODO custom fields?
		if (original != null) {
			// Update
			WorkflowDescriptor wfDesc = 
					com.atlassian.jira.workflow.WorkflowUtil.convertXMLtoWorkflowDescriptor(src.getXml());
			ConfigurableJiraWorkflow wf = (ConfigurableJiraWorkflow) WORKFLOW_MANAGER.getWorkflow(src.getName());
			if (wf.isActive()) {
				JiraWorkflow draft = WORKFLOW_MANAGER.getDraftWorkflow(src.getName());
				if (draft == null) {
					// Create draft
					draft = WORKFLOW_MANAGER.createDraftWorkflow(currentUser, src.getName());
				}
				WorkflowDescriptor draftDesc = draft.getDescriptor();
				// Copy all data from wfDesc to draftDesc
				// Unlike ConfigurableJiraWorkflow class
				// JiraDraftWorkflow does NOT have .setDescriptor().
				// Instead of waste time trying to figure out how to properly copy everything over
				// Modify it with reflection
				Field descriptorField = JiraDraftWorkflow.class.getSuperclass().getDeclaredField("descriptor");
				descriptorField.setAccessible(true);
				descriptorField.set(draft, wfDesc);
				descriptorField.setAccessible(false);
				WORKFLOW_MANAGER.updateWorkflow(currentUser, draft);
				// Move draft to original
				WORKFLOW_MANAGER.overwriteActiveWorkflow(currentUser, src.getName());
			} else {
				wf.setDescriptor(wfDesc);		
				WORKFLOW_MANAGER.updateWorkflow(currentUser, wf);
			}
			result.setNewDTO(findByUniqueKey(wf.getName()));
		} else {
			WorkflowDescriptor wfDesc = 
					com.atlassian.jira.workflow.WorkflowUtil.convertXMLtoWorkflowDescriptor(src.getXml());
			// Create ConfigurationWorkflow
			ConfigurableJiraWorkflow wf = new ConfigurableJiraWorkflow(src.getName(), WORKFLOW_MANAGER);
			wf.setDescriptor(wfDesc);
			wf.setDescription(src.getDescription());
			// Commit
			WORKFLOW_MANAGER.createWorkflow(currentUser, wf);
			WorkflowDTO2 created = new WorkflowDTO2();
			created.setJiraObject(wf);
			result.setNewDTO(created);
		}
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return WorkflowDTO2.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (JiraWorkflow wf : WORKFLOW_MANAGER.getWorkflows()) {
			WorkflowDTO2 item = new WorkflowDTO2();
			item.setJiraObject(wf);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
