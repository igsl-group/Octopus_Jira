package com.igsl.configmigration.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.priority.Priority;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.fieldconfig.FieldConfigDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class OptionsDTO extends JiraConfigItem {

	private List<OptionDTO> rootOptions;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		Options obj = (Options) o;
		this.rootOptions = new ArrayList<>();
		for (Option opt : obj.getRootOptions()) {
			OptionDTO item = new OptionDTO();
			item.setJiraObject(opt);
			this.rootOptions.add(item);
		}
	}

	@Override
	public String getUniqueKey() {
		return Integer.toString(this.hashCode());
	}

	@Override
	public String getInternalId() {
		return Integer.toString(this.hashCode());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getRootOptions");
	}

	public List<OptionDTO> getRootOptions() {
		return rootOptions;
	}

	public void setRootOptions(List<OptionDTO> rootOptions) {
		this.rootOptions = rootOptions;
	}

}
