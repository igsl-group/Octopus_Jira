package com.igsl.configmigration.issuesecuritylevelscheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelScheme;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueSecurityLevelSchemeConfigItem extends JiraConfigItem {

	private static final IssueSecurityLevelManager LEVEL_MANAGER = 
			ComponentAccessor.getIssueSecurityLevelManager();
	
	private Long id;
	private String description;
	private String name;
	private Long defaultSecurityLevelId;
	private List<IssueSecurityLevelConfigItem> issueSecurityLevels;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		IssueSecurityLevelScheme obj = (IssueSecurityLevelScheme) o;
		this.id = obj.getId();
		this.description = obj.getDescription();
		this.name = obj.getName();
		this.defaultSecurityLevelId = obj.getDefaultSecurityLevelId();
		this.issueSecurityLevels = new ArrayList<>();
		for (IssueSecurityLevel level : LEVEL_MANAGER.getIssueSecurityLevels(obj.getId())) {
			IssueSecurityLevelConfigItem item = new IssueSecurityLevelConfigItem();
			item.setJiraObject(level);
			this.issueSecurityLevels.add(item);
		}
	}

	@Override
	public String getUniqueKey() {
		return this.getName();
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
				"getIssueSecurityLevels");
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Long getDefaultSecurityLevelId() {
		return defaultSecurityLevelId;
	}

	public void setDefaultSecurityLevelId(Long defaultSecurityLevelId) {
		this.defaultSecurityLevelId = defaultSecurityLevelId;
	}

	public List<IssueSecurityLevelConfigItem> getIssueSecurityLevels() {
		return issueSecurityLevels;
	}

	public void setIssueSecurityLevels(List<IssueSecurityLevelConfigItem> issueSecurityLevels) {
		this.issueSecurityLevels = issueSecurityLevels;
	}

}
