package com.igsl.configmigration.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

	private static List<OptionDTO> getAllOptionsHelper(OptionDTO option) {
		List<OptionDTO> result = new ArrayList<>();
		if (option != null) {
			result.add(option);
			if (option.getChildOptions() != null) {
				for (OptionDTO child : option.getChildOptions()) {
					result.addAll(getAllOptionsHelper(child));
				}
			}
		}
		return result;
	}
	
	@JsonIgnore
	public List<OptionDTO> getAllOptions() {
		List<OptionDTO> result = new ArrayList<>();
		for (OptionDTO dto : this.rootOptions) {
			result.addAll(getAllOptionsHelper(dto));			
		}
		return result;
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
		// Referenced from CustomFieldDTO only
		return null;
	}

	@Override
	public Class<?> getJiraClass() {
		return Options.class;
	}

}
