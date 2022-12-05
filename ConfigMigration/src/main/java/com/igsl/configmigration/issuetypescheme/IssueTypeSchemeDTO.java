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
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldconfig.FieldConfigDTO;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.project.ProjectDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeSchemeDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(IssueTypeSchemeDTO.class);
	private static IssueTypeSchemeManager MANAGER = ComponentAccessor.getComponent(IssueTypeSchemeManager.class);
	
	private Long id;
	private String name;
	private String description;
	private List<IssueTypeDTO> associatedIssueTypes;
	private List<ProjectDTO> associatedProjects;
	private FieldConfigDTO fieldConfig;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		FieldConfigScheme obj = (FieldConfigScheme) o;
		this.associatedIssueTypes = new ArrayList<>();
		// getAssociatedIssueTypeObjects() always return nothing. 
		// Need to call IssueTypeSchemeManager in newer Jira version.
		for (IssueType type : MANAGER.getIssueTypesForScheme(obj)) {
			if (type != null) {
				IssueTypeDTO item = new IssueTypeDTO();
				item.setJiraObject(type);
				associatedIssueTypes.add(item);
			}
		}
		this.associatedProjects = new ArrayList<>();
		for (Project p : obj.getAssociatedProjectObjects()) {
			if (p != null) {
				ProjectDTO item = new ProjectDTO();
				item.setJiraObject(p);
				associatedProjects.add(item);
			}
		}
		this.id = obj.getId();
		this.name = obj.getName();
		this.description = obj.getDescription();
		this.fieldConfig = new FieldConfigDTO();
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

	public List<IssueTypeDTO> getAssociatedIssueTypes() {
		return associatedIssueTypes;
	}

	public void setAssociatedIssueTypes(List<IssueTypeDTO> associatedIssueTypes) {
		this.associatedIssueTypes = associatedIssueTypes;
	}

	public List<ProjectDTO> getAssociatedProjects() {
		return associatedProjects;
	}

	public void setAssociatedProjects(List<ProjectDTO> associatedProjects) {
		this.associatedProjects = associatedProjects;
	}

	public FieldConfigDTO getFieldConfig() {
		return fieldConfig;
	}

	public void setFieldConfig(FieldConfigDTO fieldConfig) {
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

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return IssueTypeSchemeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldConfigScheme.class;
	}

}
