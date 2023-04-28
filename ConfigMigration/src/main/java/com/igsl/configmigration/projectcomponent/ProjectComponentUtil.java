package com.igsl.configmigration.projectcomponent;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.project.component.MutableProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectCategoryImpl;
import com.atlassian.jira.project.ProjectManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.project.ProjectUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectComponentUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ProjectComponentUtil.class);
	private static final ProjectComponentManager MANAGER = ComponentAccessor.getProjectComponentManager();
	private static final ProjectManager PROJECT_MANAGER = ComponentAccessor.getProjectManager();
	
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
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
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
			result.setNewDTO(findByInternalId(Long.toString(originalJira.getId())));
		} else {
			// Map project ids
			Project p = PROJECT_MANAGER.getProjectObjByKey(src.getProjectKey());
			if (p == null) {
				throw new Exception("Project cannot be found with key: \"" + src.getProjectKey() + "\"");
			}
			// Create
			ProjectComponent createdJira = MANAGER.create(
					src.getName(), 
					src.getDescription(),
					src.getLead(),
					src.getAssigneeType(),
					p.getId());
			ProjectComponentDTO created = new ProjectComponentDTO();
			created.setJiraObject(createdJira);
			result.setNewDTO(created);
		}
		return result;
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
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		// Filter ignored
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		if (params != null && params.length == 1) {
			Long projectId = (Long) params[0];
			for (ProjectComponent it : MANAGER.getComponents(Arrays.asList(projectId))) {
				ProjectComponentDTO item = new ProjectComponentDTO();
				item.setJiraObject(it);
				result.put(item.getUniqueKey(), item);
			}
		}
		return result;
	}

}
