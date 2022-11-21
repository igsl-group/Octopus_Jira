package com.igsl.configmigration.optionset;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;
import com.igsl.configmigration.issuetype.IssueTypeConfigItem;
import com.igsl.configmigration.issuetypescheme.IssueTypeSchemeConfigItem;
import com.igsl.configmigration.project.ProjectConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class OptionSetConfigUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(OptionSetConfigUtil.class);
	private static OptionSetManager MANAGER = ComponentAccessor.getComponent(OptionSetManager.class);
	
	@Override
	public String getName() {
		return "Option Set";
	}
	
	@Override
	public TypeReference<?> getTypeReference() {
		return new TypeReference<Map<String, IssueTypeSchemeConfigItem>>() {};
	}
	
	/**
	 * params[0]: FieldConfig
	 */
	@Override
	public Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception {
		assert params.length == 1 && FieldConfig.class.isAssignableFrom(params[0].getClass());
		FieldConfig fieldConfig = (FieldConfig) params[0];
		Map<String, JiraConfigItem> result = new HashMap<>();
		OptionSet os = MANAGER.getOptionsForConfig(fieldConfig);
		OptionSetConfigItem item = new OptionSetConfigItem();
		item.setJiraObject(os);
		item.getMap().put(OptionSetConfigItem.KEY_FIELD_CONFIG, OM.writeValueAsString(fieldConfig));
		result.put(item.getKey(), item);
		return result;
	}

	/**
	 * params[0]: FieldConfig
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		assert params.length == 1 && FieldConfig.class.isAssignableFrom(params[0].getClass());
		FieldConfig fc = (FieldConfig) params[0];
		OptionSet os = MANAGER.getOptionsForConfig(fc);
		return os;
	}
	
	@Override
	public boolean merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception {
		OptionSet original = null;
		if (oldItem != null && oldItem.getJiraObject() != null) {
			original = (OptionSet) oldItem.getJiraObject();
		} else {
			original = (OptionSet) findObject(oldItem.getKey());
		}
		String fieldConfigString = newItem.getMap().get(OptionSetConfigItem.KEY_FIELD_CONFIG);
		FieldConfig fieldConfig = OM.readValue(fieldConfigString, FieldConfig.class);
		String optionIdsString = newItem.getMap().get(OptionSetConfigItem.KEY_OPTION_IDS);
		Collection<String> optionIds = OM.readValue(optionIdsString, new TypeReference<Collection<String>>() {});
		String optionsString = newItem.getMap().get(OptionSetConfigItem.KEY_OPTIONS);
		Collection<Option> options = OM.readValue(optionsString, new TypeReference<Collection<Option>>() {});
		if (original != null) {
			// Update
			MANAGER.updateOptionSet(fieldConfig, optionIds);
			return true;
		} else {
			// Create
			MANAGER.createOptionSet(fieldConfig, optionIds);
//			OptionSet created = MANAGER.getOptionsForConfig(fieldConfig);
			return false;
		}
	}
	
	@Override
	public void merge(Map<String, ImportData> items) throws Exception {
		for (ImportData data : items.values()) {
			try {
				if (merge(data.getServer(), data.getData())) {
					data.setImportResult("Updated");
				} else {
					data.setImportResult("Created");
				}
			} catch (Exception ex) {
				data.setImportResult(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
				throw ex;
			}
		}
	}

}
