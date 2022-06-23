package com.igsl.customfieldtypes.efforttable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.config.item.DefaultValueConfigItem;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.classloader.PluginClassLoader;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.igsl.customfieldtypes.I18nResource;
import com.igsl.customfieldtypes.changerequest.ChangeReqeustData;

//TODO to support project export/import, implement ProjectImportableCustomField 
//TODO to support sorting, implement SortableCustomField

/**
 * Implemented as GenericTextCFType to be compatible with Service Management customer portal.
 */
@Named
public class EffortTable2 extends GenericTextCFType {

	private static final Logger LOGGER = LoggerFactory.getLogger(EffortTable2.class);
	
	protected static final String SETTINGS_TASK_LIST = "taskList";
	
	private static final String VELOCITY_PARAM_GSON = "gson";
	private static final String VELOCITY_PARAM_DATA_CLASS = "class";
	private static final String VELOCITY_PARAM_TASK_LIST = "taskList";
	
	@Inject
	public EffortTable2(
			@ComponentImport CustomFieldValuePersister customFieldValuePersister, 
			@ComponentImport GenericConfigManager genericConfigManagr, 
			@ComponentImport TextFieldCharacterLengthValidator textFieldCharacterLengthValidator, 
			@ComponentImport JiraAuthenticationContext jiraAuthenticationContext) {
		super(customFieldValuePersister, genericConfigManagr, textFieldCharacterLengthValidator, jiraAuthenticationContext);
	}
	
	@Override
	public String availableForBulkEdit(BulkEditBean arg0) {
		// Return null to allow bulk edit, return error message otherwise
		return null;
	}

	@Override
	public List<FieldConfigItemType> getConfigurationItemTypes() {
		// To allow configuration on custom field
		List<FieldConfigItemType> result = new ArrayList<FieldConfigItemType>();
		result.add(new DefaultValueConfigItem());
		result.add(new EffortTableConfigurationItemType());
		return result;
	}

	@Override
	public void setDefaultValue(FieldConfig arg0, String arg1) {
		LOGGER.debug("setDefaultValue: " + arg0 + ", " + arg1);
		String value = null;
		if (arg1 != null) {
			try {
				EffortTableData data = null;
				data = EffortTableData.fromString(arg1);
				value = data.toString();
			} catch (FieldValidationException fvex) {
				LOGGER.error("Invalid default value", fvex);
			}
		}
		this.genericConfigManager.update("DefaultValue", arg0.getId().toString(), value);
	}
	
	@Override
	public String getDefaultValue(FieldConfig arg0) {		
		LOGGER.debug("getDefaultValue: " + arg0);
		Object o = this.genericConfigManager.retrieve("DefaultValue", arg0.getId().toString());
		if (o != null) {
			String s = String.valueOf(o);
			LOGGER.debug("DefaultValue: " + s);
			try {
				EffortTableData result = EffortTableData.fromString(s);
				return result.toString();
			} catch (Exception ex) {
				LOGGER.warn("Default value is invalid: [" + s + "]");
				return null;
			}
		}
		return null;
	}

	@Override
	public List<FieldIndexer> getRelatedIndexers(CustomField arg0) {
		// Return null for no indexer
		return null;
	}

	@Override
	public String getSingularObjectFromString(String arg0) throws FieldValidationException {
		LOGGER.debug("getSingularObjectFromString: " + arg0);
		if (arg0 != null) {
			EffortTableData value = EffortTableData.fromString(arg0);
			LOGGER.debug("getSingularObjectFromString = " + value);
			return value.toString();
		}
		return null;
	}

	@Override
	public String getStringFromSingularObject(String arg0) {
		LOGGER.debug("getStringFromSingularObject: " + arg0);
		if (arg0 != null) {
			try {
				EffortTableData value = EffortTableData.fromString(arg0);
				LOGGER.debug("getStringFromSingularObject = " + value);
				return value.toString();
			} catch (FieldValidationException fvex) {
				return null;
			}
		}
		return null;
	}

	@Override
	public Object getStringValueFromCustomFieldParams(CustomFieldParams arg0) {
		LOGGER.debug("getStringValueFromCustomFieldParams: " + arg0);
		String result = getValueFromCustomFieldParams(arg0);
		LOGGER.debug("getStringValueFromCustomFieldParams = " + result);
		return result;
	}

	// After form validation, CustomFieldParams contains values of HTML elements with name equals to custom field ID.
	@Override
	public String getValueFromCustomFieldParams(CustomFieldParams arg0) throws FieldValidationException {
		LOGGER.debug("getValueFromCustomFieldParams: " + arg0);
		LOGGER.debug("getValueFromCustomFieldParams: getAllKeys: " + arg0.getAllKeys());
		LOGGER.debug("getValueFromCustomFieldParams: getAllValues: " + arg0.getAllValues());
		LOGGER.debug("getValueFromCustomFieldParams: getQueryString: " + arg0.getQueryString());
		// We need to handle two modes of input here
		// Because we are unable to have the edit form submit a complex object, from edit form it will be values stored in distinct keys
		// But when loading from issue, it will be a complex object in null key.
		String result = null;
		Object nullKeyValue = arg0.getFirstValueForNullKey();
		if (nullKeyValue != null) {
			// Parse as complete object
			String s = String.valueOf(nullKeyValue);
			if (!s.isEmpty()) {
				EffortTableData data = EffortTableData.fromString(s);
				if (data != null) {
					result = data.toString();
				}
			}			
		}
		LOGGER.debug("getValueFromCustomFieldParams = " + result);
		return result;
	}
	
	@Override
	public void createValue(CustomField arg0, Issue arg1, String arg2) {
		LOGGER.debug("createValue: " + arg0 + ", " + arg1 + ", " + arg2);
		// Convert arg2 into a string for saving into database
		this.customFieldValuePersister.createValues(arg0, arg1.getId(), PersistenceFieldType.TYPE_UNLIMITED_TEXT, Collections.singletonList(arg2));
		LOGGER.debug("createValue ends");
	}
	
	@Override
	public void updateValue(CustomField arg0, Issue arg1, String arg2) {
		LOGGER.debug("updateValue: " + arg0 + ", " + arg1 + ", " + arg2);
		this.customFieldValuePersister.updateValues(arg0, arg1.getId(), PersistenceFieldType.TYPE_UNLIMITED_TEXT, Collections.singletonList(arg2));
		LOGGER.debug("updateValue ends"); 
	}
	
	@Override
	public String getValueFromIssue(CustomField arg0, Issue arg1) {
		LOGGER.debug("getValueFromIssue: " + arg0 + ", " + arg1);
		String result = null;
		if (arg1 != null) {
			List<Object> values = this.customFieldValuePersister.getValues(arg0, arg1.getId(), PersistenceFieldType.TYPE_UNLIMITED_TEXT);
			if (values != null && !values.isEmpty()) {
				if (values.size() != 1) {
					LOGGER.warn("More than one values found, using first value");
				}
				try {
					EffortTableData data = EffortTableData.fromString(String.valueOf(values.get(0)));
					if (data != null) {
						result = data.toString();
					}
				} catch (FieldValidationException fvex) {
					LOGGER.warn("Data cannot be parsed", fvex);
				}
			}
		}
		LOGGER.debug("getValueFromIssue = " + result);
		return result;
	}

	@Override
	public Map<String, Object> getVelocityParameters(Issue arg0, CustomField arg1, FieldLayoutItem arg2) {
		LOGGER.debug("getVelocityParameters: " + arg0 + ", " + arg1 + ", " + arg2);
		// Provide Gson to Velocity template, so it can parse string to EffortTableData class
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(VELOCITY_PARAM_GSON, new Gson());
		result.put(VELOCITY_PARAM_DATA_CLASS, EffortTableData.class);
		result.put(VELOCITY_PARAM_TASK_LIST, getTaskListSettings());
		LOGGER.debug("getVelocityParameters = " + result);
		return result;
	}

	@Override
	public boolean isRenderable() {
		// true if the field is configurable for use with the renderers, a text based field, false otherwise.
		return false;
	}

	@Nonnull
	protected PersistenceFieldType getDataBaseType() {
		return PersistenceFieldType.TYPE_UNLIMITED_TEXT;
	}

	public static void saveTaskListSettings(List<String> data) {
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		String result = null;
		if (data != null) {
			result = new Gson().toJson(data);
		}
		settings.put(SETTINGS_TASK_LIST, result);
	}
	
	public static List<String> getTaskListSettings() {
		List<String> result = null;
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		Object o = settings.get(SETTINGS_TASK_LIST);
		if (o != null) {
			try {
				Type collectionType = new TypeToken<List<String>>() {}.getType();
				result = new Gson().fromJson(String.valueOf(o), collectionType);
			} catch (JsonSyntaxException jsex) {
				LOGGER.error("Settings is in invalid format, settings ignored", jsex);
			}
		}
		return result;
	}
}
