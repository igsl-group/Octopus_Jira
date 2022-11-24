package com.igsl.configmigration.status;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.ConfigUtil;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;
import com.igsl.configmigration.statuscategory.StatusCategoryDTO;

@ConfigUtil
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
	public TypeReference<?> getTypeReference() {
		return new TypeReference<Map<String, StatusDTO>>() {};
	}
	
	@Override
	public Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigItem> result = new TreeMap<>();
		for (Status it : MANAGER.getStatuses()) {
			StatusDTO item = new StatusDTO();
			item.setJiraObject(it);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	/**
	 * params[0]: Status name
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		return MANAGER.getStatus((String) params[0]);
	}
	
	@Override
	public Object merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception {
		Status original = null;
		if (oldItem != null) {
			if (oldItem.getJiraObject() != null) {
				original = (Status) oldItem.getJiraObject();
			} else {
				original = (Status) findObject(oldItem.getUniqueKey());
			}
		} else {
			original = (Status) findObject(newItem.getUniqueKey());
		}
		StatusDTO src = (StatusDTO) newItem;
		String name = src.getName();
		String description = src.getDescription();
		StatusCategoryDTO category = src.getStatusCategoryConfigItem();
		StatusCategory cat = CATEGORY_MANAGER.getStatusCategoryByKey(category.getKey());
		final String DUMMY_ICON_URL = ".";
		if (original != null) {
			// Update
			MANAGER.editStatus(original, name, description, DUMMY_ICON_URL, cat);
			return original;
		} else {
			// Create
			return MANAGER.createStatus(name, description, DUMMY_ICON_URL, cat);
		}
	}
	
	@Override
	public void merge(Map<String, ImportData> items) throws Exception {
		for (ImportData data : items.values()) {
			try {
				merge(data.getServer(), data.getData());
				data.setImportResult("Updated");
			} catch (Exception ex) {
				data.setImportResult(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
				throw ex;
			}
		}
	}

}
