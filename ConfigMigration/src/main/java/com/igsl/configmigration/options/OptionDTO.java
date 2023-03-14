package com.igsl.configmigration.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldconfig.FieldConfigDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class OptionDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(OptionDTO.class);
	
	private Long parentId;
	private Long optionId;
	private String value;
	private boolean disabled;
	private List<OptionDTO> childOptions;
	private Long sequence;

	/**
	 * #0: FieldConfig
	 * #1: Parent ID as Long
	 */
	@Override
	protected int getObjectParameterCount() {
		return 2;
	}
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		FieldConfig fieldConfig = (FieldConfig) objectParameters[0];
		this.parentId = (Long) objectParameters[1];
		Option obj = (Option) o;
		this.optionId = obj.getOptionId();
		this.value = obj.getValue();
		this.disabled = obj.getDisabled();
		this.childOptions = new ArrayList<>();
		for (Option opt : obj.getChildOptions()) {
			OptionDTO item = new OptionDTO();
			item.setJiraObject(opt, fieldConfig, this.optionId);
			this.childOptions.add(item);
		}
		this.sequence = obj.getSequence();
		this.uniqueKey = this.value;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Parent ID", new JiraConfigProperty(this.parentId));
		r.put("Option ID", new JiraConfigProperty(this.optionId));
		r.put("Value", new JiraConfigProperty(this.value));
		r.put("Disabled", new JiraConfigProperty(this.disabled));
		r.put("Child Options", new JiraConfigProperty(OptionUtil.class, this.childOptions));
		r.put("Sequence", new JiraConfigProperty(this.sequence));
		return r;
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getOptionId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getValue",
				"isDisabled",
				"getChildOptions",
				"getSequence");
	}

	public Long getOptionId() {
		return optionId;
	}

	public void setOptionId(Long optionId) {
		this.optionId = optionId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public List<OptionDTO> getChildOptions() {
		return childOptions;
	}

	public void setChildOptions(List<OptionDTO> childOptions) {
		this.childOptions = childOptions;
	}

	public Long getSequence() {
		return sequence;
	}

	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return OptionUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return Option.class;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

}
