package com.igsl.configmigration.projectcomponent;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.project.component.MutableProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectCategoryImpl;
import com.atlassian.jira.project.ProjectManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectComponentUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ProjectComponentUtil.class);
	private static final ProjectComponentManager MANAGER = ComponentAccessor.getProjectComponentManager();
	
	@Override
	public String getName() {
		return "Project Component";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		ProjectComponent s = MANAGER.getProjectComponent(Long.parseLong(id));
		if (s != null) {
			ProjectComponentDTO item = new ProjectComponentDTO();
			item.setJiraObject(s);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		Long projectId = (Long) params[0];
		for (ProjectComponent it : MANAGER.getComponents(Arrays.asList(projectId))) {
			if (uniqueKey.equals(it.getName())) {
				ProjectComponentDTO item = new ProjectComponentDTO();
				item.setJiraObject(it);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		ProjectComponentDTO original = null;
		if (oldItem != null) {
			original = (ProjectComponentDTO) oldItem;
		} else {
			original = (ProjectComponentDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		ProjectComponent originalJira = (original != null)? (ProjectComponent) original.getJiraObject(): null;
		ProjectComponentDTO src = (ProjectComponentDTO) newItem;
		if (original != null) {
			// Update
			MutableProjectComponent com = MutableProjectComponent.copy(originalJira);
			com.setArchived(src.isArchived());
			com.setAssigneeType(src.getAssigneeType());
			com.setDescription(src.getDescription());
			com.setLead(src.getLead());
			com.setName(src.getName());
			MANAGER.update(com);
			return findByInternalId(Long.toString(originalJira.getId()));
		} else {
			// TODO Map ids
			// Create
			ProjectComponent createdJira = MANAGER.create(
					src.getName(), 
					src.getDescription(),
					src.getLead(),
					src.getAssigneeType(),
					src.getProjectId());
			ProjectComponentDTO created = new ProjectComponentDTO();
			created.setJiraObject(createdJira);
			return created;
		}
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ProjectComponentDTO.class;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		// Filter ignored
		Long projectId = (Long) params[0];
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (ProjectComponent it : MANAGER.getComponents(Arrays.asList(projectId))) {
			ProjectComponentDTO item = new ProjectComponentDTO();
			item.setJiraObject(it);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
