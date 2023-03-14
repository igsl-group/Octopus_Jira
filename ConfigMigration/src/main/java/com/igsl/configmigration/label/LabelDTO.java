package com.igsl.configmigration.label;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.issue.label.Label;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class LabelDTO extends JiraConfigDTO {

	private Long id;
	private String label;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		Label o = (Label) obj;
		this.id = o.getId();
		this.label = o.getLabel();
		this.uniqueKey = this.label;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Label", new JiraConfigProperty(this.label));
		return r;
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

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return LabelUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return Label.class;
	}

}
