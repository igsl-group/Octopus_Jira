package com.igsl.configmigration.customfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.customfieldsearcher.CustomFieldSearcherDTO;
import com.igsl.configmigration.customfieldsearcher.CustomFieldSearcherUtil;
import com.igsl.configmigration.customfieldtype.CustomFieldTypeDTO;
import com.igsl.configmigration.defaultvalueoperations.DefaultValueOperationsDTO;
import com.igsl.configmigration.defaultvalueoperations.DefaultValueOperationsUtil;
import com.igsl.configmigration.fieldconfigscheme.FieldConfigSchemeDTO;
import com.igsl.configmigration.fieldconfigscheme.FieldConfigSchemeUtil;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.options.OptionsDTO;
import com.igsl.configmigration.options.OptionsUtil;
import com.igsl.configmigration.propertyset.PropertySetDTO;
import com.igsl.configmigration.propertyset.PropertySetUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class CustomFieldDTO extends JiraConfigDTO {

	private List<IssueTypeDTO> associatedIssueTypes;
	private CustomFieldTypeDTO customFieldType;
	private String defaultSortOrder;
	private String description;
	private String fieldName;
	private String id;
	private Long idAsLong;
	private String name;
	private CustomFieldSearcherDTO customFieldSearcher;
	private List<FieldConfigSchemeDTO> configurationSchemes;
	private PropertySetDTO propertySet;
	private OptionsDTO options;
	private DefaultValueOperationsDTO defaultValueOperations;
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Name", new JiraConfigProperty(name));
		r.put("ID", new JiraConfigProperty(id));
		r.put("Description", new JiraConfigProperty(description));
		r.put("Sort Order", new JiraConfigProperty(defaultSortOrder));
		r.put("Field Name", new JiraConfigProperty(fieldName));
		r.put("Custom Field Searcher", 
				new JiraConfigProperty(CustomFieldSearcherUtil.class, customFieldSearcher));
		r.put("Configuration Schemes", new JiraConfigProperty(FieldConfigSchemeUtil.class, this.configurationSchemes));
		r.put("Property Set", new JiraConfigProperty(PropertySetUtil.class, this.propertySet));
		r.put("Options", new JiraConfigProperty(OptionsUtil.class, this.options));
		r.put("Default Value", new JiraConfigProperty(DefaultValueOperationsUtil.class, this.defaultValueOperations));
		return r;
	}
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		CustomField obj = (CustomField) o;
		this.associatedIssueTypes = new ArrayList<>();
		for (IssueType it : obj.getAssociatedIssueTypes()) {
			IssueTypeDTO item = new IssueTypeDTO();
			item.setJiraObject(it);
			this.associatedIssueTypes.add(item);
		}
		//obj.getConfigurationItemTypes();
		this.configurationSchemes = new ArrayList<>();
		for (FieldConfigScheme scheme : obj.getConfigurationSchemes()) {
			FieldConfigSchemeDTO item = new FieldConfigSchemeDTO();
			item.setJiraObject(scheme);
			this.configurationSchemes.add(item);
		}
		this.customFieldType = new CustomFieldTypeDTO();
		this.customFieldType.setJiraObject(obj.getCustomFieldType());
		this.customFieldSearcher = new CustomFieldSearcherDTO();
		this.customFieldSearcher.setJiraObject(obj.getCustomFieldSearcher(), this.customFieldType);
		this.defaultSortOrder = obj.getDefaultSortOrder();
		this.description = obj.getDescription();
		this.fieldName = obj.getFieldName();
		this.id = obj.getId();
		this.idAsLong = obj.getIdAsLong();
		this.name = obj.getName();
		this.propertySet = new PropertySetDTO();
		this.propertySet.setJiraObject(obj.getPropertySet());
		this.options = new OptionsDTO();
		// TODO Can there be more than 1 scheme? What about no scheme?
		FieldConfigScheme scheme = (FieldConfigScheme) this.configurationSchemes.get(0).getJiraObject();
		FieldConfig fieldConfig = scheme.getOneAndOnlyConfig();
		this.options.setJiraObject(obj.getOptions(null, fieldConfig, null), fieldConfig);
		this.defaultValueOperations = new DefaultValueOperationsDTO();
		this.defaultValueOperations.setJiraObject(obj.getDefaultValueOperations(), fieldConfig);
		this.uniqueKey = obj.getName();
	}

	@Override
	public String getInternalId() {
		return this.getId();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getDescription",
				"getFieldName",
				"getDefaultSortOrder",
				"getCustomFieldType",
				"getAssociatedIssueTypes",
				"getCustomFieldSearcher",
				"getConfigurationSchemes",
				"getPropertySet",
				"getOptions",
				"getDefaultValueOperations");
	}

	public List<IssueTypeDTO> getAssociatedIssueTypes() {
		return associatedIssueTypes;
	}

	public void setAssociatedIssueTypes(List<IssueTypeDTO> associatedIssueTypes) {
		this.associatedIssueTypes = associatedIssueTypes;
	}

	public CustomFieldTypeDTO getCustomFieldType() {
		return customFieldType;
	}

	public void setCustomFieldType(CustomFieldTypeDTO customFieldType) {
		this.customFieldType = customFieldType;
	}

	public String getDefaultSortOrder() {
		return defaultSortOrder;
	}

	public void setDefaultSortOrder(String defaultSortOrder) {
		this.defaultSortOrder = defaultSortOrder;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CustomFieldSearcherDTO getCustomFieldSearcher() {
		return customFieldSearcher;
	}

	public void setCustomFieldSearcher(CustomFieldSearcherDTO customFieldSearcher) {
		this.customFieldSearcher = customFieldSearcher;
	}

	public List<FieldConfigSchemeDTO> getConfigurationSchemes() {
		return configurationSchemes;
	}

	public void setConfigurationSchemes(List<FieldConfigSchemeDTO> configurationSchemes) {
		this.configurationSchemes = configurationSchemes;
	}

	public PropertySetDTO getPropertySet() {
		return propertySet;
	}

	public void setPropertySet(PropertySetDTO propertySet) {
		this.propertySet = propertySet;
	}

	public OptionsDTO getOptions() {
		return options;
	}

	public void setOptions(OptionsDTO options) {
		this.options = options;
	}

	public DefaultValueOperationsDTO getDefaultValueOperations() {
		return defaultValueOperations;
	}

	public void setDefaultValueOperations(DefaultValueOperationsDTO defaultValueOperations) {
		this.defaultValueOperations = defaultValueOperations;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return CustomFieldUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return CustomField.class;
	}

	public Long getIdAsLong() {
		return idAsLong;
	}

	public void setIdAsLong(Long idAsLong) {
		this.idAsLong = idAsLong;
	}

}
