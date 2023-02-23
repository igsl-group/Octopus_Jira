package com.igsl.configmigration.status;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class StatusUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(StatusUtil.class);
	private static StatusManager MANAGER = ComponentAccessor.getComponent(StatusManager.class);
	private static StatusCategoryManager CATEGORY_MANAGER = ComponentAccessor.getComponent(StatusCategoryManager.class);
	
	@Override
	public String getName() {
		return "Status";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Status s = MANAGER.getStatus(id);
		if (s != null) {
			StatusDTO item = new StatusDTO();
			item.setJiraObject(s);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (Status it : MANAGER.getStatuses()) {
			if (uniqueKey.equals(it.getName())) {
				StatusDTO item = new StatusDTO();
				item.setJiraObject(it);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		StatusDTO original = null;
		if (oldItem != null) {
			original = (StatusDTO) oldItem;
		} else {
			original = (StatusDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		Status originalJira = (original != null)? (Status) original.getJiraObject(): null;
		StatusDTO src = (StatusDTO) newItem;
		String name = src.getName();
		String description = src.getDescription();
		StatusCategoryDTO category = src.getStatusCategoryConfigItem();
		StatusCategory cat = CATEGORY_MANAGER.getStatusCategoryByKey(category.getKey());
		final String DUMMY_ICON_URL = ".";
		if (original != null) {
			// Update
			MANAGER.editStatus(originalJira, name, description, DUMMY_ICON_URL, cat);
			return findByInternalId(originalJira.getId());
		} else {
			// Create
			Status createdJira = MANAGER.createStatus(name, description, DUMMY_ICON_URL, cat);
			StatusDTO created = new StatusDTO();
			created.setJiraObject(createdJira);
			return created;
		}
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return StatusDTO.class;
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
		for (Status it : MANAGER.getStatuses()) {
			String name = it.getName().toLowerCase();
			String desc = (it.getDescription() == null)? "" : it.getDescription().toLowerCase();
			if (filter != null) {
				if (!name.contains(filter) && 
					!desc.contains(filter)) {
					continue;
				}
			}
			StatusDTO item = new StatusDTO();
			item.setJiraObject(it);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	@Override
	public String getSearchHints() {
		return "Case-insensitive wildcard search on name and description";
	}

}
