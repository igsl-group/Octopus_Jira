package com.igsl.configmigration.issuesecuritylevelscheme;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueSecurityLevelDTO extends JiraConfigDTO {

	private Long id;
	private String description;
	private String name;
	private Long schemeId;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		IssueSecurityLevel obj = (IssueSecurityLevel) o;
		this.id = obj.getId();
		this.description = obj.getDescription();
		this.name = obj.getName();
		this.schemeId = obj.getSchemeId();
		this.uniqueKey = Integer.toString(obj.hashCode());
	}
	
	@Override
	public String getConfigName() {
		return this.name;
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Schema ID", new JiraConfigProperty(this.schemeId));
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

	@Override
	public Class<?> getJiraClass() {
		return IssueSecurityLevel.class;
	}

}
