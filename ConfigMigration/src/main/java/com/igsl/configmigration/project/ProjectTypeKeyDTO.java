package com.igsl.configmigration.project;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.project.type.ProjectTypeKey;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectTypeKeyDTO extends JiraConfigDTO {

	private String key;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		ProjectTypeKey o = (ProjectTypeKey) obj;
		this.key = o.getKey();
	}

	@Override
	public String getUniqueKey() {
		return this.getKey();
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
		return null;
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
