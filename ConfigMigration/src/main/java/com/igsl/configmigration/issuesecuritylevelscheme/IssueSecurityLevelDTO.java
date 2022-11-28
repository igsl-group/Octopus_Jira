package com.igsl.configmigration.issuesecuritylevelscheme;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueSecurityLevelDTO extends JiraConfigDTO {

	private Long id;
	private String description;
	private String name;
	private Long schemeId;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		IssueSecurityLevel obj = (IssueSecurityLevel) o;
		this.id = obj.getId();
		this.description = obj.getDescription();
		this.name = obj.getName();
		this.schemeId = obj.getSchemeId();
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
				"getDescription");
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

	public Long getSchemeId() {
		return schemeId;
	}

	public void setSchemeId(Long schemeId) {
		this.schemeId = schemeId;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return IssueSecurityLevelUtil.class;
	}

}
