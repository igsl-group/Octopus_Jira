package com.igsl.configmigration.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class OptionsDTO extends JiraConfigDTO {

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

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
