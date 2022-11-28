package com.igsl.configmigration.optionset;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class OptionSetUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(OptionSetUtil.class);
	private static OptionSetManager MANAGER = ComponentAccessor.getComponent(OptionSetManager.class);
	
	@Override
	public String getName() {
		return "Option Set";
	}
	
	/**
	 * params[0]: FieldConfig
	 */
	@Override
	public Map<String, JiraConfigDTO> readAllItems(Object... params) throws Exception {
		FieldConfig fieldConfig = (FieldConfig) params[0];
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		OptionSet os = MANAGER.getOptionsForConfig(fieldConfig);
		OptionSetDTO item = new OptionSetDTO();
		item.setJiraObject(os);
		item.setFieldConfig(fieldConfig);
		result.put(item.getUniqueKey(), item);
		return result;
	}

	/**
	 * params[0]: FieldConfig
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		FieldConfig fc = (FieldConfig) params[0];
		OptionSet os = MANAGER.getOptionsForConfig(fc);
		return os;
	}
	
	@Override
	public Object merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		OptionSet original = null;
		if (oldItem != null) {
			if (oldItem.getJiraObject() != null) {
				original = (OptionSet) oldItem.getJiraObject();
			} else {
				original = (OptionSet) findObject(oldItem.getUniqueKey());
			}
		} else {
			original = (OptionSet) findObject(newItem.getUniqueKey());
		}
		OptionSetDTO src = (OptionSetDTO) newItem;
		Collection<String> optionIds = src.getOptionIds();
		FieldConfig fieldConfig = src.getFieldConfig();
		if (original != null) {
			// Update
			MANAGER.updateOptionSet(fieldConfig, optionIds);
			return original;
		} else {
			// Create
			return MANAGER.createOptionSet(fieldConfig, optionIds);
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

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return OptionSetDTO.class;
	}

	@Override
	public boolean isPublic() {
		// Referenced only
		return false;
	}

}
