package com.igsl.configmigration.project;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.project.Project;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectDTO extends JiraConfigDTO {

	protected Long id;
	protected String key;
	protected String name;
	
	@Override
	public void fromJiraObject(Object obj, Object... params) throws Exception {
		Project o = (Project) obj;
		this.id = o.getId();
		this.key = o.getKey();
		this.name = o.getName();
	}

	@Override
	public String getUniqueKey() {
		return this.getName();
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getKey");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
