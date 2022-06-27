package com.igsl.customfieldtypes.efforttable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.util.system.check.I18nMessage;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.igsl.customfieldtypes.I18nResource;

public class EffortTableConfigurationItemType implements FieldConfigItemType {

	private static Logger LOGGER = LoggerFactory.getLogger(EffortTableConfigurationItemType.class);
	
	@Override
	public String getBaseEditUrl() {
		// This is the alias in atlassian-plugin.xml
		return "EffortTableConfigurationItemAction.jspa";
	}

	@Override
	public Object getConfigurationObject(Issue arg0, FieldConfig arg1) {
		// Return object for getVelocityParameters()
		LOGGER.debug("getConfigurationObject: " + arg0 + ", " + arg1);
		Map<String, Object> result = new HashMap<String, Object>();	
		List<String> settings = EffortTable2.getTaskListSettings();
		result.put(EffortTable2.SETTINGS_TASK_LIST, settings);
		return result;
	}

	@Override
	public String getDisplayName() {
		// Name of configuration, used in Field Configuration Scheme screen
		return "Effort Table Task List";
	}

	@Override
	public String getDisplayNameKey() {
		// i18n key for display in Field Configuration screen
		return I18nResource.EFFORTTABLE_CONFIG_NAME;
	}

	@Override
	public String getObjectKey() {
		// Web Work key
		return "EffortTableConfig";
	}

	@Override
	public String getViewHtml(FieldConfig arg0, FieldLayoutItem arg1) {
		// Return current value to be displayed
		LOGGER.debug("getViewHtml: " + arg0 + ", " + arg1);
		List<String> settings = EffortTable2.getTaskListSettings();
		StringBuilder sb = new StringBuilder();
		for (String s : settings) {
			sb.append("<p>").append(s).append("</p>");
		}
		return sb.toString();
	}

}
