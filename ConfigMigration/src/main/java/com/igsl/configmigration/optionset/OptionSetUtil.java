package com.igsl.configmigration.optionset;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.fieldconfig.FieldConfigDTO;
import com.igsl.configmigration.fieldconfig.FieldConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class OptionSetUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(OptionSetUtil.class);
	private static OptionSetManager MANAGER = ComponentAccessor.getComponent(OptionSetManager.class);
	
	@Override
	public String getName() {
		return "Option Set";
	}

	/**
	 * #0: FieldConfig
	 */
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		FieldConfigDTO fieldConfig = (FieldConfigDTO) params[0];
		OptionSet os = MANAGER.getOptionsForConfig((FieldConfig) fieldConfig.getJiraObject());
		OptionSetDTO item = new OptionSetDTO();
		item.setJiraObject(os, fieldConfig);
		return item;
	}

	/**
	 * #0: FieldConfig
	 */
	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		return findByInternalId(null, params);
	}

	@Override
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		final FieldConfigUtil FIELD_CONFIG_UTIL = 
				(FieldConfigUtil) JiraConfigTypeRegistry.getConfigUtil(FieldConfigUtil.class);
		OptionSetDTO original = null;
		if (oldItem != null) {
			original = (OptionSetDTO) oldItem;
		} else {
			original = (OptionSetDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		OptionSetDTO src = (OptionSetDTO) newItem;
		Collection<String> optionIds = src.getOptionIds();
		if (original != null) {
			// Update
			FieldConfig fieldConfig = (FieldConfig) original.getFieldConfig();
			MANAGER.updateOptionSet(fieldConfig, optionIds);
			result.setNewDTO(original);
		} else {
			// Create
			FieldConfig fieldConfig = (FieldConfig) src.getFieldConfig();
			OptionSet createdJira = MANAGER.createOptionSet(fieldConfig, optionIds);
			OptionSetDTO created = new OptionSetDTO();
			created.setJiraObject(createdJira, fieldConfig);
			result.setNewDTO(created);
		}
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return OptionSetDTO.class;
	}

	@Override
	public boolean isVisible() {
		// Referenced only
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		// Filter ignored
		Map<String, JiraConfigDTO> result = new HashMap<>();
		if (params != null && params.length == 1) {
			JiraConfigDTO dto = findByInternalId(null, params);
			result.put(dto.getUniqueKey(), dto);
		}
		return result;
	}

}
