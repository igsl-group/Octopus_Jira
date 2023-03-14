package com.igsl.configmigration.optionset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class OptionSetDTO extends JiraConfigDTO {

	private List<String> optionIds;
	private List<OptionDTO> options;

	@JsonIgnore
	private FieldConfig fieldConfig;
	
	/**
	 * #0: FieldConfigDTO
	 */
	@Override
	protected int getObjectParameterCount() {
		return 1;
	}
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		this.fieldConfig = (FieldConfig) objectParameters[0];
		OptionSet obj = (OptionSet) o;
		this.optionIds = new ArrayList<String>();
		for (String s : obj.getOptionIds()) {
			this.optionIds.add(s);
		}
		this.options = new ArrayList<>();
		for (Option opt : obj.getOptions()) {
			OptionDTO item = new OptionDTO();
			item.setJiraObject(opt);
			this.options.add(item);
		}
		Collections.sort(this.optionIds);
		this.uniqueKey = Long.toString(this.hashCode());
	}
		
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Option IDs", new JiraConfigProperty(this.optionIds));
		r.put("Options", new JiraConfigProperty(OptionUtil.class, this.options));
		return r;
	}

	@Override
	public String getInternalId() {
		return optionIds.toString();
	}

	public List<String> getOptionIds() {
		return optionIds;
	}

	public void setOptionIds(List<String> optionIds) {
		this.optionIds = optionIds;
	}

	public FieldConfig getFieldConfig() {
		return fieldConfig;
	}

	public void setFieldConfig(FieldConfig fieldConfig) {
		this.fieldConfig = fieldConfig;
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getOptionIds",
				"getOptions",
				"getFieldConfig");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return OptionSetUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return OptionSet.class;
	}

}
