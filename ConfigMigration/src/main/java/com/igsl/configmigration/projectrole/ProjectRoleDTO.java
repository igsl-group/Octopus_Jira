package com.igsl.configmigration.projectrole;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.icon.IconType;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.security.roles.ProjectRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.avatar.AvatarDTO;
import com.igsl.configmigration.avatar.AvatarUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectRoleDTO extends JiraConfigDTO {

	private String description;
	private Long id;
	private String name;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		ProjectRole obj = (ProjectRole) o;
		this.description = obj.getDescription();
		this.id = obj.getId();
		this.name = obj.getName();
		this.uniqueKey = this.name;
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("ID", new JiraConfigProperty(this.id));
		return r;
	}
	
	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getDescription",
				"getAvatarConfigItem");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return ProjectRoleUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return IssueType.class;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
