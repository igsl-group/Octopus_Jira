package com.igsl.configmigration.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.customfields.option.Option;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class OptionDTO extends JiraConfigDTO {

	private Long optionId;
	private String value;
	private boolean disabled;
	private List<OptionDTO> childOptions;
	private Long sequence;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		Option obj = (Option) o;
		this.optionId = obj.getOptionId();
		this.value = obj.getValue();
		this.disabled = obj.getDisabled();
		this.childOptions = new ArrayList<>();
		for (Option opt : obj.getChildOptions()) {
			OptionDTO item = new OptionDTO();
			item.setJiraObject(opt);
			this.childOptions.add(item);
		}
		this.sequence = obj.getSequence();
	}

	@Override
	public String getUniqueKey() {
		return this.getValue();
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
		// TODO Auto-generated method stub
		return null;
	}

}
