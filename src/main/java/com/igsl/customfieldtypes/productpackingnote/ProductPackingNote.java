package com.igsl.customfieldtypes.productpackingnote;

import java.lang.reflect.Type;
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

import com.atlassian.jira.component.ComponentAccessor;
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
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

@Named
public class ProductPackingNote implements CustomFieldType<List<ProductPackingNoteData>, ProductPackingNoteData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductPackingNote.class);
	
	protected static final String KEY_NAME = "name";
	protected static final String KEY_VERSION = "version";
	protected static final String KEY_MD5_SIGNATURE = "md5Signature";

	protected OptionsManager optionsManager;
	protected CustomFieldValuePersister customFieldValuePersister;	// This is how we access custom field values directly from database
	protected GenericConfigManager genericConfigManager;
	protected CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor;
	
	@Inject
	public ProductPackingNote(
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
	public void createValue(CustomField arg0, Issue arg1, List<ProductPackingNoteData> arg2) {
		LOGGER.debug("createValue: " + arg0 + ", " + arg1 + ", " + arg2);
		// Convert arg2 into a string for saving into database
		Collection<String> data = null;
		if (arg2 != null) {
			data = new ArrayList<String>();
			for (ProductPackingNoteData item : arg2) {
				data.add(item.toString());
			}
		}
		this.customFieldValuePersister.createValues(arg0, arg1.getId(), PersistenceFieldType.TYPE_UNLIMITED_TEXT, data);
		LOGGER.debug("createValue ends");
	}

	@Override
	public String getChangelogString(CustomField arg0, List<ProductPackingNoteData> arg1) {
		if (arg1 != null) {
			StringBuilder sb = new StringBuilder();
			for (ProductPackingNoteData item : arg1) {
				sb.append("; ").append(item.toReadableString());
			}
			return sb.toString().substring(1);
		}
		return null;
	}

	@Override
	public String getChangelogValue(CustomField arg0, List<ProductPackingNoteData> arg1) {
		if (arg1 != null) {
			StringBuilder sb = new StringBuilder();
			for (ProductPackingNoteData item : arg1) {
				sb.append("; ").append(item.toString());
			}
			return sb.toString().substring(1);
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
	public List<ProductPackingNoteData> getDefaultValue(FieldConfig arg0) {
		LOGGER.debug("getDefaultValue: " + arg0);
		String s = String.valueOf(this.genericConfigManager.retrieve("DefaultValue", arg0.getId().toString()));
		LOGGER.debug("DefaultValue: " + s);
		try {
			Type collectionType = new TypeToken<List<ProductPackingNoteData>>() {}.getType();
			List<ProductPackingNoteData> result = new Gson().fromJson(s, collectionType);
			LOGGER.warn("getDefaultValue = " + result);
			return result;
		} catch (Exception ex) {
			LOGGER.warn("Default value is invalid: [" + s + "]");
			return null;
		}
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
	public ProductPackingNoteData getSingularObjectFromString(String arg0) throws FieldValidationException {
		LOGGER.debug("getSingularObjectFromString: " + arg0);
		ProductPackingNoteData value = ProductPackingNoteData.fromString(arg0);
		LOGGER.debug("getSingularObjectFromString = " + value);
		return value;
	}

	@Override
	public String getStringFromSingularObject(ProductPackingNoteData arg0) {
		LOGGER.debug("getStringFromSingularObject: " + arg0);
		String value = arg0.toString();
		LOGGER.debug("getStringFromSingularObject = " + value);
		return value;
	}

	@Override
	public Object getStringValueFromCustomFieldParams(CustomFieldParams arg0) {
		LOGGER.debug("getStringValueFromCustomFieldParams: " + arg0);
		List<ProductPackingNoteData> result = getValueFromCustomFieldParams(arg0);
		LOGGER.debug("getStringValueFromCustomFieldParams = " + result);
		if (result != null) {
			return new Gson().toJson(result);
		}
		return null;
	}

	// After form validation, CustomFieldParams contains values of HTML elements with name equals to custom field ID.
	@Override
	public List<ProductPackingNoteData> getValueFromCustomFieldParams(CustomFieldParams arg0) throws FieldValidationException {
		LOGGER.debug("getValueFromCustomFieldParams: " + arg0);
		LOGGER.debug("getValueFromCustomFieldParams: getAllKeys: " + arg0.getAllKeys());
		LOGGER.debug("getValueFromCustomFieldParams: getAllValues: " + arg0.getAllValues());
		LOGGER.debug("getValueFromCustomFieldParams: getQueryString: " + arg0.getQueryString());
		Collection<String> nameList = arg0.getValuesForKey(KEY_NAME);
		Collection<String> versionList = arg0.getValuesForKey(KEY_VERSION);
		Collection<String> md5SignatureList = arg0.getValuesForKey(KEY_MD5_SIGNATURE);
		List<ProductPackingNoteData> itemList = null;
		if (nameList != null) {
			itemList = new ArrayList<ProductPackingNoteData>();
			for (String name : nameList) {
				ProductPackingNoteData item = new ProductPackingNoteData();
				item.setName(name);
				itemList.add(item);
			}
			int idx = 0;
			for (String version : versionList) {
				itemList.get(idx).setVersion(version);
				idx++;
			}
			idx = 0;
			for (String md5Signature : md5SignatureList) {
				itemList.get(idx).setMd5Signature(md5Signature);
				idx++;
			}
		}
		LOGGER.debug("getValueFromCustomFieldParams = " + itemList);
		return itemList;
	}

	@Override
	public List<ProductPackingNoteData> getValueFromIssue(CustomField arg0, Issue arg1) {
		LOGGER.debug("getValueFromIssue: " + arg0 + ", " + arg1);
		List<ProductPackingNoteData> result = null;
		if (arg1 != null) {
			List<Object> values = this.customFieldValuePersister.getValues(arg0, arg1.getId(), PersistenceFieldType.TYPE_UNLIMITED_TEXT);
			if (values != null && !values.isEmpty()) {
				result = new ArrayList<ProductPackingNoteData>();
				for (Object o : values) {
					if (o != null) {
						ProductPackingNoteData item = ProductPackingNoteData.fromString(String.valueOf(o));
						result.add(item);
					}
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
	public void setDefaultValue(FieldConfig arg0, List<ProductPackingNoteData> arg1) {
		LOGGER.debug("setDefaultValue: " + arg0 + ", " + arg1);
		String value = new Gson().toJson(arg1);
		this.genericConfigManager.update("DefaultValue", arg0.getId().toString(), value);
	}

	@Override
	public void updateValue(CustomField arg0, Issue arg1, List<ProductPackingNoteData> arg2) {
		LOGGER.debug("updateValue: " + arg0 + ", " + arg1 + ", " + arg2);
		Collection<String> data = null;
		if (arg2 != null) {
			data = new ArrayList<String>();
			for (ProductPackingNoteData item : arg2) {
				data.add(item.toString());
			}
		}
		this.customFieldValuePersister.updateValues(arg0, arg1.getId(), PersistenceFieldType.TYPE_UNLIMITED_TEXT, data);
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
	public boolean valuesEqual(List<ProductPackingNoteData> arg0, List<ProductPackingNoteData> arg1) {
		LOGGER.debug("valuesEqual: " + arg0 + ", " + arg1);
		if (arg0 == null || arg1 == null) {
			return false;
		}
		if (arg0.size() != arg1.size()) {
			return false;
		}
		boolean result = true;
		for (int i = 0; i < arg0.size(); i++) {
			result &= (arg0.get(i).compareTo(arg1.get(i)) == 0);
			if (!result) {
				break;
			}
		}
		return result;
	}

}
