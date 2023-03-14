package com.igsl.configmigration.customfieldtype;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class CustomFieldTypeDTO extends JiraConfigDTO {

	private String key;
	private String name;
	private String description;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		CustomFieldType<?, ?> obj = (CustomFieldType<?, ?>) o;
		// obj.getConfigurationItemTypes();
		this.description = obj.getDescription();
		// obj.getDescriptor();
		this.key = obj.getKey();
		this.name = obj.getName();
		// obj.getNonNullCustomFieldProvider();
		this.uniqueKey = this.key;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Key", new JiraConfigProperty(this.key));
		r.put("Name", new JiraConfigProperty(this.name));
		return r;
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

	@Override
	public Class<?> getJiraClass() {
		return CustomFieldType.class;
	}

}
