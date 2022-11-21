package com.igsl.configmigration.issuetypescheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.fieldconfig.FieldConfigConfigItem;
import com.igsl.configmigration.issuetype.IssueTypeConfigItem;
import com.igsl.configmigration.project.ProjectConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeSchemeConfigItem extends JiraConfigItem {

	private static final Logger LOGGER = Logger.getLogger(IssueTypeSchemeConfigItem.class);
	private static IssueTypeSchemeManager MANAGER = ComponentAccessor.getComponent(IssueTypeSchemeManager.class);
	
	private Long id;
	private String name;
	private String description;
	private List<IssueTypeConfigItem> associatedIssueTypes;
	private List<ProjectConfigItem> associatedProjects;
	private FieldConfigConfigItem fieldConfig;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		FieldConfigScheme obj = (FieldConfigScheme) o;
		this.associatedIssueTypes = new ArrayList<>();
		// getAssociatedIssueTypeObjects() always return nothing. 
		// Need to call IssueTypeSchemeManager in newer Jira version.
		for (IssueType type : MANAGER.getIssueTypesForScheme(obj)) {
			if (type != null) {
				IssueTypeConfigItem item = new IssueTypeConfigItem();
				item.setJiraObject(type);
				associatedIssueTypes.add(item);
			}
		}
		this.associatedProjects = new ArrayList<>();
		for (Project p : obj.getAssociatedProjectObjects()) {
			if (p != null) {
				ProjectConfigItem item = new ProjectConfigItem();
				item.setJiraObject(p);
				associatedProjects.add(item);
			}
		}
		this.id = obj.getId();
		this.name = obj.getName();
		this.description = obj.getDescription();
		this.fieldConfig = new FieldConfigConfigItem();
		this.fieldConfig.setJiraObject(obj.getOneAndOnlyConfig());
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<IssueTypeConfigItem> getAssociatedIssueTypes() {
		return associatedIssueTypes;
	}

	public void setAssociatedIssueTypes(List<IssueTypeConfigItem> associatedIssueTypes) {
		this.associatedIssueTypes = associatedIssueTypes;
	}

	public List<ProjectConfigItem> getAssociatedProjects() {
		return associatedProjects;
	}

	public void setAssociatedProjects(List<ProjectConfigItem> associatedProjects) {
		this.associatedProjects = associatedProjects;
	}

	public FieldConfigConfigItem getFieldConfig() {
		return fieldConfig;
	}

	public void setFieldConfig(FieldConfigConfigItem fieldConfig) {
		this.fieldConfig = fieldConfig;
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getDescription",
				"getAssociatedIssueTypes",
				"getAssociatedProjects",
				"getFieldConfig");
	}

}
