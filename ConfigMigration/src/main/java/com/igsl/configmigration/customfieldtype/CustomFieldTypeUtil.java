package com.igsl.configmigration.customfieldtype;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;
import com.igsl.configmigration.annotation.ConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class CustomFieldTypeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(CustomFieldTypeUtil.class);
	private static CustomFieldManager CF_MANAGER = ComponentAccessor.getComponent(CustomFieldManager.class);
	
	@Override
	public String getName() {
		return "Custom Field Type";
	}
	
	@Override
	public Map<String, JiraConfigDTO> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (CustomFieldType<?, ?> p : CF_MANAGER.getCustomFieldTypes()) {
			CustomFieldTypeDTO item = new CustomFieldTypeDTO();
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
		for (CustomFieldType<?, ?> p : CF_MANAGER.getCustomFieldTypes()) {
			if (p.getKey().equals(identifier)) {
				return p;
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
		return CustomFieldTypeDTO.class;
	}

}
