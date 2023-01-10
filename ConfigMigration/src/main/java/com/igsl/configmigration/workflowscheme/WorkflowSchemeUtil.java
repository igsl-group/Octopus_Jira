package com.igsl.configmigration.workflowscheme;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.workflow.WorkflowSchemeService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class WorkflowSchemeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(WorkflowSchemeUtil.class);
	private static final WorkflowSchemeManager MANAGER = ComponentAccessor.getWorkflowSchemeManager();
	private static final WorkflowSchemeService SERVICE = 
			ComponentAccessor.getComponent(WorkflowSchemeService.class);
	
	@Override
	public String getName() {
		return "Workflow Scheme";
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (AssignableWorkflowScheme wf : MANAGER.getAssignableSchemes()) {
			AssignableWorkflowSchemeDTO item = new AssignableWorkflowSchemeDTO();
			item.setJiraObject(wf);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		AssignableWorkflowScheme wf = MANAGER.getWorkflowSchemeObj(Long.parseLong(id));
		if (wf != null) {
			AssignableWorkflowSchemeDTO item = new AssignableWorkflowSchemeDTO();
			item.setJiraObject(wf);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (AssignableWorkflowScheme wf : MANAGER.getAssignableSchemes()) {
			if (uniqueKey.equals(wf.getName())) {
				AssignableWorkflowSchemeDTO item = new AssignableWorkflowSchemeDTO();
				item.setJiraObject(wf);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		AssignableWorkflowSchemeDTO original = null;
		if (oldItem != null) {
			original = (AssignableWorkflowSchemeDTO) oldItem;
		} else {
			original = (AssignableWorkflowSchemeDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		AssignableWorkflowSchemeDTO src = (AssignableWorkflowSchemeDTO) newItem;
		// Fix null map key
		if (src.getMappings().containsKey(AssignableWorkflowSchemeDTO.NULL_KEY)) {
			String s = src.getMappings().remove(AssignableWorkflowSchemeDTO.NULL_KEY);
			src.getMappings().put(null, s);
		}
		if (original != null) {
			// Update
			AssignableWorkflowScheme scheme = (AssignableWorkflowScheme) original.getJiraObject();
			scheme = scheme.builder()
				.setDefaultWorkflow(src.getConfiguredDefaultWorkflow())
				.setDescription(src.getDescription())
				.setMappings(src.getMappings())
				.setName(src.getName()).build();
			scheme = MANAGER.updateWorkflowScheme(scheme);
			return findByUniqueKey(Long.toString(scheme.getId()));
		} else {
			AssignableWorkflowScheme scheme = SERVICE.assignableBuilder()
				.setDefaultWorkflow(src.getConfiguredDefaultWorkflow())
				.setDescription(src.getDescription())
				.setMappings(src.getMappings())
				.setName(src.getName())
				.build();
			scheme = MANAGER.createScheme(scheme);
			AssignableWorkflowSchemeDTO created = new AssignableWorkflowSchemeDTO();
			created.setJiraObject(scheme);
			return created;
		}
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return AssignableWorkflowSchemeDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

}
