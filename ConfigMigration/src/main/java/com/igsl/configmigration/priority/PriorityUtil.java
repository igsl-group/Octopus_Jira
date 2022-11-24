package com.igsl.configmigration.priority;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.issue.priority.Priority;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.ConfigUtil;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;

@ConfigUtil
@JsonDeserialize(using = JsonDeserializer.None.class)
public class PriorityUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(PriorityUtil.class);
	private static PriorityManager PRIORITY_MANAGER = ComponentAccessor.getComponent(PriorityManager.class);
	
	@Override
	public String getName() {
		return "Priority";
	}
	
	@Override
	public TypeReference<?> getTypeReference() {
		return new TypeReference<Map<String, PriorityDTO>>() {};
	}
	
	@Override
	public Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigItem> result = new HashMap<>();
		for (Priority p : PRIORITY_MANAGER.getPriorities()) {
			PriorityDTO item = new PriorityDTO();
			item.setJiraObject(p);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	/**
	 * params[0]: name
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		String identifier = (String) params[0];
		for (Priority p : PRIORITY_MANAGER.getPriorities()) {
			if (p.getName().equals(identifier)) {
				return p;
			}
		}
		return null;
	}
	
	public Object merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception {
		Priority original = null;
		if (oldItem != null) {
			if (oldItem.getJiraObject() != null) {
				original = (Priority) oldItem.getJiraObject();
			} else {
				original = (Priority) findObject(oldItem.getUniqueKey());
			}
		} else {
			original = (Priority) findObject(newItem.getUniqueKey());
		}
		PriorityDTO src = (PriorityDTO) newItem;
		if (original != null) {
			// Update
			PRIORITY_MANAGER.editPriority(
					original, src.getName(), src.getDescription(), src.getIconUrl(), src.getStatusColor());
			return original;
		} else {
			// Create
			return PRIORITY_MANAGER.createPriority(
					src.getName(), src.getDescription(), src.getIconUrl(), src.getStatusColor());
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
