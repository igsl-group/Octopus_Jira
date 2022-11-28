package com.igsl.configmigration.customfieldtype;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.vdi.NonNullCustomFieldProvider;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class CustomFieldTypeDTO extends JiraConfigDTO {

	private String key;
	private String name;
	private String description;
	private List<FieldConfigItemType> fieldConfigItemTypes;
	private CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor;
	private NonNullCustomFieldProvider nonNullCustomFieldProvider;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		CustomFieldType<?, ?> obj = (CustomFieldType<?, ?>) o;
		// obj.getConfigurationItemTypes();
		this.description = obj.getDescription();
		// obj.getDescriptor();
		this.key = obj.getKey();
		this.name = obj.getName();
		// obj.getNonNullCustomFieldProvider();
	}

	@Override
	public String getUniqueKey() {
		return this.getKey();
	}

	@Override
	public String getInternalId() {
		return this.getKey();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName", 
				"getDescription");
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return CustomFieldTypeUtil.class;
	}

}
