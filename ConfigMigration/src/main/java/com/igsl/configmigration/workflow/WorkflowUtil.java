package com.igsl.configmigration.workflow;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflowFactory;
import com.atlassian.jira.workflow.WorkflowManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowFactory;
import com.opensymphony.workflow.loader.XMLWorkflowFactory;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class WorkflowUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(WorkflowUtil.class);
	private static final WorkflowManager WORKFLOW_MANAGER = ComponentAccessor.getWorkflowManager();
	
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
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (JiraWorkflow wf : WORKFLOW_MANAGER.getWorkflows()) {
			WorkflowDTO2 item = new WorkflowDTO2();
			item.setJiraObject(wf);
			result.put(item.getUniqueKey(), item);
		}
		return result;
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
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		LOGGER.debug("merge starts");
		ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		WorkflowDTO2 original = null;
		if (oldItem != null) {
			original = (WorkflowDTO2) oldItem;
		} else {
			original = (WorkflowDTO2) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		WorkflowDTO2 src = (WorkflowDTO2) newItem;
		// TODO Remap data in XML
		if (original != null) {
			// Update
			WorkflowDescriptor wfDesc = 
					com.atlassian.jira.workflow.WorkflowUtil.convertXMLtoWorkflowDescriptor(src.getXml());
			ConfigurableJiraWorkflow wf = (ConfigurableJiraWorkflow) WORKFLOW_MANAGER.getWorkflow(src.getName());
			wf.setDescriptor(wfDesc);			
			WORKFLOW_MANAGER.updateWorkflow(currentUser, wf);
			return findByUniqueKey(wf.getName());
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
			return created;
		}
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return WorkflowDTO2.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

}
