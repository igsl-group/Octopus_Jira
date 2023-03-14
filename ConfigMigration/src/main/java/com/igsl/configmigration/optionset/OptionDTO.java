package com.igsl.configmigration.optionset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.issue.fields.option.Option;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class OptionDTO extends JiraConfigDTO {

	private String description;
	private List<OptionDTO> childOptions;
	private String id;
	private String name;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
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
		this.uniqueKey = this.name;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Child Options", new JiraConfigProperty(OptionUtil.class, this.childOptions));
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Name", new JiraConfigProperty(this.name));
		return r;
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

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return OptionUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return Option.class;
	}

}
