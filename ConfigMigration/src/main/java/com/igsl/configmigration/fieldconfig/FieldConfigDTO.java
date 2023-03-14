package com.igsl.configmigration.fieldconfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItem;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldConfigDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(FieldConfigDTO.class);
	
	private Long id;
	private String name;
	private String description;
	private String fieldId;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		FieldConfig o = (FieldConfig) obj;
		this.id = o.getId();
		this.name = o.getName();
		this.description = o.getDescription();
		this.fieldId = o.getFieldId();
		//o.getConfigurableField();
		this.uniqueKey = this.getName();
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(Long.toString(this.id)));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Field ID", new JiraConfigProperty(this.fieldId));
		return r;
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName", 
				"getDescription",
				"getFieldId");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return FieldConfigUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldConfig.class;
	}

}
