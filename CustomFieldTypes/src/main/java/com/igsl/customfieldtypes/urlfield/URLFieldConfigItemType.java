package com.igsl.customfieldtypes.urlfield;

import java.util.HashMap;
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

public class URLFieldConfigItemType implements FieldConfigItemType {

	private static Logger LOGGER = LoggerFactory.getLogger(URLFieldConfigItemType.class);
	private PluginSettingsFactory factory;
	private PluginSettings settings;
	
	public URLFieldConfigItemType() {
		factory = (PluginSettingsFactory) ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		settings = factory.createGlobalSettings();
	}
	
	@Override
	public String getBaseEditUrl() {
		// This is the alias in atlassian-plugin.xml
		return "URLFieldConfigurationItemAction.jspa";
	}

	@Override
	public Object getConfigurationObject(Issue arg0, FieldConfig arg1) {
		// Return object for getVelocityParameters()
		LOGGER.debug("getConfigurationObject: " + arg0 + ", " + arg1);
		Map<String, Object> result = new HashMap<String, Object>();		
		Object o = settings.get(URLField.CONFIG_MAX);
		LOGGER.debug("o: " + o);
		result.put(URLField.CONFIG_MAX, o);
		return result;
	}

	@Override
	public String getDisplayName() {
		// Name of configuration, used in Field Configuration Scheme screen
		return "URL Field Configuration";
	}

	@Override
	public String getDisplayNameKey() {
		// i18n key for display in Field Configuration screen
		return I18nResource.URLFIELD_CONFIG_NAME;
	}

	@Override
	public String getObjectKey() {
		// Web Work key
		return "URLCustomFieldConfig";
	}

	@Override
	public String getViewHtml(FieldConfig arg0, FieldLayoutItem arg1) {
		// Return current value to be displayed
		String result = null;
		LOGGER.debug("getViewHtml: " + arg0 + ", " + arg1);
		Object o = settings.get(URLField.CONFIG_MAX);
		LOGGER.debug("o: " + o);
		if (o != null) {
			result = String.valueOf(o);
		}
		return result;
	}

}
