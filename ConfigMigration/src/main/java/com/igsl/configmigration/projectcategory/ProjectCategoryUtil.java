package com.igsl.configmigration.projectcategory;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectCategoryImpl;
import com.atlassian.jira.project.ProjectManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectCategoryUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ProjectCategoryUtil.class);
	private static final ProjectManager MANAGER = ComponentAccessor.getProjectManager();
	
	@Override
	public String getName() {
		return "Project Category";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		ProjectCategory s = MANAGER.getProjectCategory(Long.parseLong(id));
		if (s != null) {
			ProjectCategoryDTO item = new ProjectCategoryDTO();
			item.setJiraObject(s);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (ProjectCategory it : MANAGER.getAllProjectCategories()) {
			if (uniqueKey.equals(it.getName())) {
				ProjectCategoryDTO item = new ProjectCategoryDTO();
				item.setJiraObject(it);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		ProjectCategoryDTO original = null;
		if (oldItem != null) {
			original = (ProjectCategoryDTO) oldItem;
		} else {
			original = (ProjectCategoryDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		ProjectCategory originalJira = (original != null)? (ProjectCategory) original.getJiraObject(): null;
		ProjectCategoryDTO src = (ProjectCategoryDTO) newItem;
		if (original != null) {
			// Update
			ProjectCategoryImpl item = new ProjectCategoryImpl(originalJira.getId(), src.getName(), src.getDescription());
			MANAGER.updateProjectCategory(item);
			return findByInternalId(Long.toString(originalJira.getId()));
		} else {
			// Create
			ProjectCategory createdJira = MANAGER.createProjectCategory(src.getName(), src.getDescription());
			ProjectCategoryDTO created = new ProjectCategoryDTO();
			created.setJiraObject(createdJira);
			return created;
		}
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ProjectCategoryDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (ProjectCategory it : MANAGER.getAllProjectCategories()) {
			ProjectCategoryDTO item = new ProjectCategoryDTO();
			item.setJiraObject(it);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
