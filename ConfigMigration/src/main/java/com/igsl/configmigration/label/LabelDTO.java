package com.igsl.configmigration.label;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.label.Label;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class LabelDTO extends JiraConfigItem {

	private Long id;
	private String label;
	
	@Override
	public void fromJiraObject(Object obj, Object... params) throws Exception {
		Label o = (Label) obj;
		this.id = o.getId();
		this.label = o.getLabel();
	}

	@Override
	public String getUniqueKey() {
		return this.getLabel();
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getLabel");
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
