package com.igsl.customfieldtypes.generictable;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.google.gson.Gson;
import com.igsl.customfieldtypes.I18nResource;

public class GenericTableConfigurationItemType implements FieldConfigItemType {

	private static Logger LOGGER = LoggerFactory.getLogger(GenericTableConfigurationItemType.class);
	
	protected static final String SETTINGS = "settings";
	
	@Override
	public String getBaseEditUrl() {
		// This is the alias in atlassian-plugin.xml
		return "GenericTableConfigurationItemAction.jspa";
	}

	@Override
	public Object getConfigurationObject(Issue arg0, FieldConfig arg1) {
		// Return object for getVelocityParameters()
		LOGGER.debug("getConfigurationObject: " + arg0 + ", " + arg1);
		GenericTableSettings settings = GenericTableSettings.getSettings(arg1.getCustomField());		
		Map<String, Object> result = new HashMap<String, Object>();	
		result.put(SETTINGS, settings);
		return result;
	}

	@Override
	public String getDisplayName() {
		// Name of configuration, used in Field Configuration Scheme screen
		return "Generic Table";
	}

	@Override
	public String getDisplayNameKey() {
		// i18n key for display in Field Configuration screen
		return I18nResource.GENERICTABLE_CONFIG_NAME;
	}

	@Override
	public String getObjectKey() {
		// Web Work key
		return "GenericTableConfig";
	}

	@Override
	public String getViewHtml(FieldConfig arg0, FieldLayoutItem arg1) {
		// Return current value to be displayed
		LOGGER.debug("getViewHtml: " + arg0 + ", " + arg1);
		StringBuilder sb = new StringBuilder();
		// TODO
		// Name
		// Field name
		// Script
		// Row HTML fragment
		// Allow add rows
		// Initial rows
		// Rendered
		return sb.toString();
	}

}
