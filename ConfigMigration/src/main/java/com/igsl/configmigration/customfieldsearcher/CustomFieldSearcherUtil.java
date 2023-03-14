package com.igsl.configmigration.customfieldsearcher;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.managers.CustomFieldSearcherManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

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
	 * #0: CustomFieldTypeDTO
	 */
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		return findByUniqueKey(id, params);
	}

	/**
	 * #0: CustomFieldTypeDTO
	 */
	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		CustomFieldType<?, ?> customFieldType = (CustomFieldType<?, ?>) params[0];
		for (CustomFieldSearcher s : MANAGER.getSearchersValidFor(customFieldType)) {
			if (s.getDescriptor().getCompleteKey().equals(uniqueKey)) {
				CustomFieldSearcherDTO dto = new CustomFieldSearcherDTO();
				dto.setJiraObject(s, customFieldType);
				return dto;
			}
		}
		return null;
	}

	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		throw new Exception("CustomFieldSearcher is read only");
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return CustomFieldSearcherDTO.class;
	}

	@Override
	public boolean isVisible() {
		// Referenced from CustomField only
		return false;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		// Filter is ignored
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		CustomFieldType<?, ?> customFieldType = (CustomFieldType<?, ?>) params[0];
		for (CustomFieldSearcher s : MANAGER.getSearchersValidFor(customFieldType)) {
			CustomFieldSearcherDTO item = new CustomFieldSearcherDTO();
			item.setJiraObject(s, params);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
