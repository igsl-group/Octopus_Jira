package com.igsl.configmigration.issuesecuritylevelscheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelScheme;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueSecurityLevelSchemeDTO extends JiraConfigDTO {

	private static final IssueSecurityLevelManager LEVEL_MANAGER = 
			ComponentAccessor.getIssueSecurityLevelManager();
	
	private Long id;
	private String description;
	private String name;
	private Long defaultSecurityLevelId;
	private List<IssueSecurityLevelDTO> issueSecurityLevels;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		IssueSecurityLevelScheme obj = (IssueSecurityLevelScheme) o;
		this.id = obj.getId();
		this.description = obj.getDescription();
		this.name = obj.getName();
		this.defaultSecurityLevelId = obj.getDefaultSecurityLevelId();
		this.issueSecurityLevels = new ArrayList<>();
		for (IssueSecurityLevel level : LEVEL_MANAGER.getIssueSecurityLevels(obj.getId())) {
			IssueSecurityLevelDTO item = new IssueSecurityLevelDTO();
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

	public List<IssueSecurityLevelDTO> getIssueSecurityLevels() {
		return issueSecurityLevels;
	}

	public void setIssueSecurityLevels(List<IssueSecurityLevelDTO> issueSecurityLevels) {
		this.issueSecurityLevels = issueSecurityLevels;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return IssueSecurityLevelSchemeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return IssueSecurityLevelScheme.class;
	}

}
