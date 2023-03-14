package com.igsl.configmigration.project;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.project.type.ProjectTypeKey;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectTypeKeyDTO extends JiraConfigDTO {

	private String key;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		ProjectTypeKey o = (ProjectTypeKey) obj;
		this.key = o.getKey();
		this.uniqueKey = this.key;
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Key", new JiraConfigProperty(this.key));
		return r;
	}

	@Override
	public String getInternalId() {
		return this.getKey();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getKey");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return ProjectTypeKeyUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return ProjectTypeKey.class;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
