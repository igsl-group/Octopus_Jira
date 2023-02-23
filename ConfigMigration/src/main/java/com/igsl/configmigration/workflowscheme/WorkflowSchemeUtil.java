package com.igsl.configmigration.workflowscheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.workflow.WorkflowSchemeService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.project.ProjectDTO;
import com.igsl.configmigration.project.ProjectUtil;

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
		ProjectUtil projectUtil = (ProjectUtil) JiraConfigTypeRegistry.getConfigUtil(ProjectUtil.class);
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
		// Remap projects
		List<Project> projects = new ArrayList<>();
		for (ProjectDTO dto : src.getProjects()) {
			ProjectDTO p = (ProjectDTO) projectUtil.findByDTO(dto);
			if (p != null) {
				projects.add((Project) p.getJiraObject());
			}
		}
		
		LOGGER.debug("src desc: [" + src.getDescription() + "]");
		
		if (original != null) {
			// Update
			AssignableWorkflowScheme scheme = (AssignableWorkflowScheme) original.getJiraObject();
			scheme = scheme.builder()
				.setDefaultWorkflow(src.getConfiguredDefaultWorkflow())
				.setDescription(src.getDescription())
				.setMappings(src.getMappings())
				.setName(src.getName()).build();
			
			LOGGER.debug("Update desc: [" + scheme.getDescription() + "]");
			
			scheme = MANAGER.updateWorkflowScheme(scheme);
			Scheme sch = MANAGER.getSchemeObject(scheme.getId());
			for (Project p : projects) {
				MANAGER.addSchemeToProject(p, sch);
			}
			MANAGER.clearWorkflowCache();
			return findByUniqueKey(src.getUniqueKey());
		} else {
			AssignableWorkflowScheme scheme = SERVICE.assignableBuilder()
				.setDefaultWorkflow(src.getConfiguredDefaultWorkflow())
				.setDescription(src.getDescription())
				.setMappings(src.getMappings())
				.setName(src.getName())
				.build();
			
			LOGGER.debug("Create desc: [" + scheme.getDescription() + "]");
			
			scheme = MANAGER.createScheme(scheme);
			Scheme sch = MANAGER.getSchemeObject(scheme.getId());
			for (Project p : projects) {
				MANAGER.addSchemeToProject(p, sch);
			}
			MANAGER.clearWorkflowCache();
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

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		if (filter != null) {
			filter = filter.toLowerCase();
		}
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (AssignableWorkflowScheme wf : MANAGER.getAssignableSchemes()) {
			String name = wf.getName().toLowerCase();
			String desc = (wf.getDescription() == null)? "" : wf.getDescription().toLowerCase();
			if (filter != null) {
				if (!name.contains(filter) && 
					!desc.contains(filter)) {
					continue;
				}
			}
			AssignableWorkflowSchemeDTO item = new AssignableWorkflowSchemeDTO();
			item.setJiraObject(wf);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	@Override
	public String getSearchHints() {
		return "Case-insensitive wildcard search on name and description";
	}

}
