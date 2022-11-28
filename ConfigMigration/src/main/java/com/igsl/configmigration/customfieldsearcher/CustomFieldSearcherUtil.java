package com.igsl.configmigration.customfieldsearcher;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.managers.CustomFieldSearcherManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class CustomFieldSearcherUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(CustomFieldSearcherUtil.class);
	private static CustomFieldSearcherManager MANAGER = 
			ComponentAccessor.getComponent(CustomFieldSearcherManager.class);
	
	@Override
	public String getName() {
		return "Custom Field Searcher";
	}
	
	/**
	 * params[0]: CustomFieldType<?, ?>
	 */
	@Override
	public Map<String, JiraConfigDTO> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (CustomFieldSearcher s : MANAGER.getSearchersValidFor((CustomFieldType<?, ?>) params[0])) {
			CustomFieldSearcherDTO item = new CustomFieldSearcherDTO();
			item.setJiraObject(s);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	/**
	 * params[0]: CustomFieldType<?, ?>
	 * params[1]: Descriptor key
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		CustomFieldType<?, ?> type = (CustomFieldType<?, ?>) params[0];
		String identifier = (String) params[1];
		LOGGER.debug("CustomFieldType: " + type);
		for (CustomFieldSearcher s : MANAGER.getSearchersValidFor(type)) {
			LOGGER.debug(s.getDescriptor().getCompleteKey() + " vs " + identifier);
			if (s.getDescriptor().getCompleteKey().equals(identifier)) {
				return s;
			}
		}
		return null;
	}
	
	public Object merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		return null;
	}
	
	@Override
	public void merge(Map<String, ImportData> items) throws Exception {
		for (ImportData data : items.values()) {
			try {
				merge(data.getServer(), data.getData());
				data.setImportResult("N/A");
			} catch (Exception ex) {
				data.setImportResult(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
				throw ex;
			}
		}
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return CustomFieldSearcherDTO.class;
	}

}
