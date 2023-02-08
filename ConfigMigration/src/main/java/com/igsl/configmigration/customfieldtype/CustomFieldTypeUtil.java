package com.igsl.configmigration.customfieldtype;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class CustomFieldTypeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(CustomFieldTypeUtil.class);
	private static CustomFieldManager CF_MANAGER = ComponentAccessor.getComponent(CustomFieldManager.class);
	
	@Override
	public String getName() {
		return "Custom Field Type";
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (CustomFieldType<?, ?> p : CF_MANAGER.getCustomFieldTypes()) {
			CustomFieldTypeDTO item = new CustomFieldTypeDTO();
			item.setJiraObject(p, params);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		return findByUniqueKey(id);
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (CustomFieldType<?, ?> p : CF_MANAGER.getCustomFieldTypes()) {
			if (p.getKey().equals(uniqueKey)) {
				CustomFieldTypeDTO dto = new CustomFieldTypeDTO();
				dto.setJiraObject(p, params);
				return dto;
			}
		}
		return null;
	}

	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		throw new Exception("CustomFieldType is only added via plugins");
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return CustomFieldTypeDTO.class;
	}

	@Override
	public boolean isVisible() {
		// Referenced from CustomField only
		return false;
	}

}
