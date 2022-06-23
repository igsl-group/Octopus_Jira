package com.igsl.customfieldtypes.staticimage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.igsl.customfieldtypes.I18nResource;

@Named
public class StaticImage extends GenericTextCFType {

	private static final Logger LOGGER = LoggerFactory.getLogger(StaticImage.class);
	private static Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareTo); 
	
	@Inject
	public StaticImage(
			@ComponentImport CustomFieldValuePersister customFieldValuePersister, 
			@ComponentImport GenericConfigManager genericConfigManagr, 
			@ComponentImport TextFieldCharacterLengthValidator textFieldCharacterLengthValidator, 
			@ComponentImport JiraAuthenticationContext jiraAuthenticationContext) {
		super(customFieldValuePersister, genericConfigManagr, textFieldCharacterLengthValidator, jiraAuthenticationContext);
	}
	
	@Override
	public String availableForBulkEdit(BulkEditBean arg0) {
		// Return null to allow bulk edit, return error message otherwise
		return I18nResource.getText(I18nResource.NO_BULK_EDIT);
	}

	@Override
	public void createValue(CustomField arg0, Issue arg1, String arg2) {
		LOGGER.debug("createValue: " + arg0 + ", " + arg1 + ", " + arg2);
		// Convert arg2 into a string for saving into database
		this.customFieldValuePersister.createValues(arg0, arg1.getId(), PersistenceFieldType.TYPE_UNLIMITED_TEXT, Collections.singletonList(arg2));
		LOGGER.debug("createValue ends");
	}

	@Override
	public List<FieldConfigItemType> getConfigurationItemTypes() {
		// To allow configuration on custom field
		List<FieldConfigItemType> result = new ArrayList<FieldConfigItemType>();
		result.add(new DefaultValueConfigItem());
		return result;
	}

	@Override
	public String getDefaultValue(FieldConfig arg0) {
		LOGGER.debug("getDefaultValue: " + arg0);
		String s = null;
		Object o = this.genericConfigManager.retrieve("DefaultValue", arg0.getId().toString());
		if (o != null) {
			s = String.valueOf(o);
		}
		LOGGER.debug("DefaultValue: " + s);
		return s;
	}

	@Override
	public List<FieldIndexer> getRelatedIndexers(CustomField arg0) {
		// Return null for no indexer
		return null;
	}

	@Override
	public String getSingularObjectFromString(String arg0) throws FieldValidationException {
		return arg0;
	}

	@Override
	public String getStringFromSingularObject(String arg0) {
		return arg0;
	}

	@Override
	public Object getStringValueFromCustomFieldParams(CustomFieldParams arg0) {
		LOGGER.debug("getStringValueFromCustomFieldParams: " + arg0);
		String result = getValueFromCustomFieldParams(arg0);
		LOGGER.debug("getStringValueFromCustomFieldParams = " + result);
		if (result != null) {
			return result;
		}
		return null;
	}

	// After form validation, CustomFieldParams contains values of HTML elements with name equals to custom field ID.
	@Override
	public String getValueFromCustomFieldParams(CustomFieldParams arg0) throws FieldValidationException {
		LOGGER.debug("getValueFromCustomFieldParams: " + arg0);
		LOGGER.debug("getValueFromCustomFieldParams: getAllKeys: " + arg0.getAllKeys());
		LOGGER.debug("getValueFromCustomFieldParams: getAllValues: " + arg0.getAllValues());
		LOGGER.debug("getValueFromCustomFieldParams: getQueryString: " + arg0.getQueryString());
		String value = null;
		Object o = arg0.getFirstValueForNullKey();
		if (o != null) {
			value = String.valueOf(o);
		}
		LOGGER.debug("getValueFromCustomFieldParams = " + value);
		return value;
	}

	@Override
	public String getValueFromIssue(CustomField arg0, Issue arg1) {
		LOGGER.debug("getValueFromIssue: " + arg0 + ", " + arg1);
		String result = null;
		if (arg1 != null) {
			List<Object> values = this.customFieldValuePersister.getValues(arg0, arg1.getId(), PersistenceFieldType.TYPE_UNLIMITED_TEXT);
			if (values != null && !values.isEmpty()) {
				if (values.size() != 1) {
					LOGGER.warn("Multiple values found, using first value");
				}
				Object o = values.get(0);
				if (o != null) {
					result = String.valueOf(o);
				}
			}
		}
		LOGGER.debug("getValueFromIssue = " + result);
		return result;
	}

	@Override
	public Map<String, Object> getVelocityParameters(Issue arg0, CustomField arg1, FieldLayoutItem arg2) {
		LOGGER.debug("getVelocityParameters: " + arg0 + ", " + arg1 + ", " + arg2);
		Map<String, Object> result = new HashMap<String, Object>();
		LOGGER.debug("getVelocityParameters = " + result);
		return result;
	}

	@Override
	public boolean isRenderable() {
		// true if the field is configurable for use with the renderers, a text based field, false otherwise.
		return false;
	}

	@Override
	public Set<Long> remove(CustomField arg0) {
		LOGGER.debug("remove: " + arg0);
		// Return list of ID of affected issues
		Set<Long> result =  this.customFieldValuePersister.removeAllValues(arg0.getId());
		LOGGER.debug("remove = " + result);
		return result;
	}

	@Override
	public void setDefaultValue(FieldConfig arg0, String arg1) {
		LOGGER.debug("setDefaultValue: " + arg0 + ", " + arg1);
		this.genericConfigManager.update("DefaultValue", arg0.getId().toString(), arg1);
	}

	@Override
	public void updateValue(CustomField arg0, Issue arg1, String arg2) {
		LOGGER.debug("updateValue: " + arg0 + ", " + arg1 + ", " + arg2);
		this.customFieldValuePersister.updateValues(arg0, arg1.getId(), PersistenceFieldType.TYPE_UNLIMITED_TEXT, Collections.singletonList(arg2));
		LOGGER.debug("updateValue ends"); 
	}

	@Override
	public void validateFromParams(CustomFieldParams arg0, ErrorCollection arg1, FieldConfig arg2) {
		LOGGER.debug("validateFromParams: " + arg0 + ", " + arg1 + ", " + arg2);
		try {
			getValueFromCustomFieldParams(arg0);
		} catch (FieldValidationException fvex) {
			arg1.addError(arg2.getCustomField().getId(), fvex.getMessage(), ErrorCollection.Reason.VALIDATION_FAILED);
			LOGGER.debug("validateFromParams = " + arg2);
		}
		LOGGER.debug("validateFromParams = no error");
	}

	@Override
	public boolean valuesEqual(String arg0, String arg1) {
		LOGGER.debug("valuesEqual: " + arg0 + ", " + arg1);
		return (nullSafeStringComparator.compare(arg0, arg1) == 0);
	}

}
