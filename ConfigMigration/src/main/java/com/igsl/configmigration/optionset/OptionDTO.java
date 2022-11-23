package com.igsl.configmigration.optionset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.fields.option.Option;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class OptionDTO extends JiraConfigItem {

	private String description;
	private List<OptionDTO> childOptions;
	private String id;
	private String name;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		Option obj = (Option) o;
		this.description = obj.getDescription();
		this.childOptions = new ArrayList<>();
		for (Object e : obj.getChildOptions()) {
			Option opt = (Option) e;
			OptionDTO item = new OptionDTO();
			item.setJiraObject(opt);
			this.childOptions.add(item);
		}
		this.id = obj.getId();
		this.name = obj.getName();
	}
		
	@Override
	public String getUniqueKey() {
		return getName();
	}

	@Override
	public String getInternalId() {
		return getId();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getDescription",
				"getChildOptions");
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<OptionDTO> getChildOptions() {
		return childOptions;
	}

	public void setChildOptions(List<OptionDTO> childOptions) {
		this.childOptions = childOptions;
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

}
