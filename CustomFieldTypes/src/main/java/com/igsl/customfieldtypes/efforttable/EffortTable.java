package com.igsl.customfieldtypes.efforttable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.gson.Gson;
import com.igsl.customfieldtypes.I18nResource;

@Named
public class EffortTable implements CustomFieldType<EffortTableData, EffortTableData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(EffortTable.class);
	
	protected static final String KEY_TASK = "task";
	protected static final String KEY_HEAD_COUNT_DAY = "headCountDay";
	protected static final String KEY_TOTAL_HEAD_COUNT_DAY = "totalHeadCountDay";
	protected static final String KEY_EXPENSES = "expenses";
	protected static final String KEY_DUMMY = "dummy";	// Dummy form data so that we know to use the data from null key (loaded from issue) or the separate keys
	
	protected OptionsManager optionsManager;
	protected CustomFieldValuePersister customFieldValuePersister;	// This is how we access custom field values directly from database
	protected GenericConfigManager genericConfigManager;
	protected CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor;
	
	@Inject
	public EffortTable(
			@ComponentImport OptionsManager optionsManager, 
			@ComponentImport CustomFieldValuePersister customFieldValuePersister, 
			@ComponentImport GenericConfigManager genericConfigManager) {
		this.optionsManager = optionsManager;
		this.customFieldValuePersister = customFieldValuePersister;
		this.genericConfigManager = genericConfigManager;
	}
	
	@Override
	public String availableForBulkEdit(BulkEditBean arg0) {
		// Return null to allow bulk edit, return error message otherwise
		return null;
	}

	@Override
	public void createValue(CustomField arg0, Issue arg1, EffortTableData arg2) {
		LOGGER.debug("createValue: " + arg0 + ", " + arg1 + ", " + arg2);
		// Convert arg2 into a string for saving into database
		this.customFieldValuePersister.createValues(arg0, arg1.getId(), PersistenceFieldType.TYPE_UNLIMITED_TEXT, Collections.singletonList(arg2.toString()));
		LOGGER.debug("createValue ends");
	}

	@Override
	public String getChangelogString(CustomField arg0, EffortTableData arg1) {
		if (arg1 != null) {
			return arg1.toString();
		}
		return null;
	}

	@Override
	public String getChangelogValue(CustomField arg0, EffortTableData arg1) {
		if (arg1 != null) {
			return arg1.toString();
		}
		return null;
	}

	@Override
	public List<FieldConfigItemType> getConfigurationItemTypes() {
		// To allow configuration on custom field
		List<FieldConfigItemType> result = new ArrayList<FieldConfigItemType>();
		result.add(new DefaultValueConfigItem());
		return result;
	}

	@Override
	public EffortTableData getDefaultValue(FieldConfig arg0) {
		LOGGER.debug("getDefaultValue: " + arg0);
		Object o = this.genericConfigManager.retrieve("DefaultValue", arg0.getId().toString());
		if (o != null) {
			String s = String.valueOf(o);
			LOGGER.debug("DefaultValue: " + s);
			try {
				EffortTableData result = EffortTableData.fromString(s);
				return result;
			} catch (Exception ex) {
				LOGGER.warn("Default value is invalid: [" + s + "]");
				return null;
			}
		}
		return null;
	}

	@Override
	public String getDescription() {
		return this.customFieldTypeModuleDescriptor.getDescription();
	}

	@Override
	public CustomFieldTypeModuleDescriptor getDescriptor() {
		return this.customFieldTypeModuleDescriptor;
	}

	@Override
	public String getKey() {
		return this.customFieldTypeModuleDescriptor.getCompleteKey();
	}

	@Override
	public String getName() {
		return this.customFieldTypeModuleDescriptor.getName();
	}

	@Override
	public List<FieldIndexer> getRelatedIndexers(CustomField arg0) {
		// Return null for no indexer
		return null;
	}

	@Override
	public EffortTableData getSingularObjectFromString(String arg0) throws FieldValidationException {
		LOGGER.debug("getSingularObjectFromString: " + arg0);
		EffortTableData value = EffortTableData.fromString(arg0);
		LOGGER.debug("getSingularObjectFromString = " + value);
		return value;
	}

	@Override
	public String getStringFromSingularObject(EffortTableData arg0) {
		LOGGER.debug("getStringFromSingularObject: " + arg0);
		String value = arg0.toString();
		LOGGER.debug("getStringFromSingularObject = " + value);
		return value;
	}

	@Override
	public Object getStringValueFromCustomFieldParams(CustomFieldParams arg0) {
		LOGGER.debug("getStringValueFromCustomFieldParams: " + arg0);
		EffortTableData result = getValueFromCustomFieldParams(arg0);
		LOGGER.debug("getStringValueFromCustomFieldParams = " + result);
		if (result != null) {
			return result.toString();
		}
		return null;
	}

	// After form validation, CustomFieldParams contains values of HTML elements with name equals to custom field ID.
	@Override
	public EffortTableData getValueFromCustomFieldParams(CustomFieldParams arg0) throws FieldValidationException {
		LOGGER.debug("getValueFromCustomFieldParams: " + arg0);
		LOGGER.debug("getValueFromCustomFieldParams: getAllKeys: " + arg0.getAllKeys());
		LOGGER.debug("getValueFromCustomFieldParams: getAllValues: " + arg0.getAllValues());
		LOGGER.debug("getValueFromCustomFieldParams: getQueryString: " + arg0.getQueryString());
		// We need to handle two modes of input here
		// Because we are unable to have the edit form submit a complex object, from edit form it will be values stored in distinct keys
		// But when loading from issue, it will be a complex object in null key.
		// The absence of KEY_DUMMY will tell us it is an initial load instead of form submit, in which case we use the null key.		
		EffortTableData result = null;
		if (!arg0.containsKey(KEY_DUMMY)) {
			LOGGER.debug("Using null key value");
			Object nullKeyValue = arg0.getFirstValueForNullKey();
			if (nullKeyValue != null) {
				// Parse as complete object
				String s = String.valueOf(nullKeyValue);
				if (!s.isEmpty()) {
					result = EffortTableData.fromString(s);
				}
			}
		} else {
			LOGGER.debug("Using individual fields");
			// Parse as individual fields
			Collection<String> taskList = arg0.getValuesForKey(KEY_TASK);
			Collection<String> headCountDayList = arg0.getValuesForKey(KEY_HEAD_COUNT_DAY);
			Object expenses = arg0.getFirstValueForKey(KEY_EXPENSES);
			Object totalHeadCountDay = arg0.getFirstValueForKey(KEY_TOTAL_HEAD_COUNT_DAY);
			if (expenses != null || totalHeadCountDay != null || taskList != null) {
				if (expenses != null) {
					if (result == null) {
						result = new EffortTableData();
					}
					try {
						Float f = Float.parseFloat(String.valueOf(expenses));
						result.setExpenses(f);
					} catch (NumberFormatException nfex) {
						throw new FieldValidationException(I18nResource.getText(I18nResource.EFFORTTABLE_EXPENSES_LABEL) + " is invalid: " + expenses);
					}
				}
				if (totalHeadCountDay != null) {
					if (result == null) {
						result = new EffortTableData();
					}
					try {
						Float f = Float.parseFloat(String.valueOf(totalHeadCountDay));
						result.setTotalHeadCountDay(f);
					} catch (NumberFormatException nfex) {
						throw new FieldValidationException(I18nResource.getText(I18nResource.EFFORTTABLE_TOTALHEADCOUNTDAY_LABEL) + " is invalid: " + expenses);
					}
				}
				if (taskList != null && taskList.size() != 0) {
					if (result == null) {
						result = new EffortTableData();
					}
					for (String task : taskList) {
						EffortTableDataRow item = new EffortTableDataRow();
						item.setTask(task);
						result.getRows().add(item);
					}
					int idx = 0;
					if (headCountDayList != null) {
						for (String headCountDay : headCountDayList) {
							if (headCountDay != null && !headCountDay.isEmpty()) {
								try {
									Float f = Float.parseFloat(headCountDay);
									result.getRows().get(idx).setHeadCountDay(f);
								} catch (NumberFormatException nfex) {
									throw new FieldValidationException(I18nResource.getText(I18nResource.EFFORTTABLE_HEADCOUNTDAY_LABEL) + " is invalid: " + headCountDay);
								}
							}
							idx++;
						}
					}
				}
			}
		}
		LOGGER.debug("getValueFromCustomFieldParams = " + result);
		return result;
	}

	@Override
	public EffortTableData getValueFromIssue(CustomField arg0, Issue arg1) {
		LOGGER.debug("getValueFromIssue: " + arg0 + ", " + arg1);
		EffortTableData result = null;
		if (arg1 != null) {
			List<Object> values = this.customFieldValuePersister.getValues(arg0, arg1.getId(), PersistenceFieldType.TYPE_UNLIMITED_TEXT);
			if (values != null && !values.isEmpty()) {
				if (values.size() != 1) {
					LOGGER.warn("More than one values found, using first value");
				}
				result = EffortTableData.fromString(String.valueOf(values.get(0)));
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

	// Initialization method invoked by Jira
	@Override
	public void init(CustomFieldTypeModuleDescriptor arg0) {
		LOGGER.debug("init: " + arg0);
		this.customFieldTypeModuleDescriptor = arg0;
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
	public void setDefaultValue(FieldConfig arg0, EffortTableData arg1) {
		LOGGER.debug("setDefaultValue: " + arg0 + ", " + arg1);
		String value = new Gson().toJson(arg1);
		this.genericConfigManager.update("DefaultValue", arg0.getId().toString(), value);
	}

	@Override
	public void updateValue(CustomField arg0, Issue arg1, EffortTableData arg2) {
		LOGGER.debug("updateValue: " + arg0 + ", " + arg1 + ", " + arg2);
		this.customFieldValuePersister.updateValues(arg0, arg1.getId(), PersistenceFieldType.TYPE_UNLIMITED_TEXT, Collections.singletonList(arg2.toString()));
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
	public boolean valuesEqual(EffortTableData arg0, EffortTableData arg1) {
		LOGGER.debug("valuesEqual: " + arg0 + ", " + arg1);
		if (arg0 == null || arg1 == null) {
			return false;
		}
		return (arg0.compareTo(arg1) == 0);
	}

}
