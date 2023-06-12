package com.igsl.configmigration.fieldlayoutscheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfigItem;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldconfig.FieldConfigDTO;
import com.igsl.configmigration.fieldconfig.FieldConfigUtil;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.issuetype.IssueTypeUtil;
import com.igsl.configmigration.project.ProjectDTO;
import com.igsl.configmigration.project.ProjectUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldLayoutSchemeDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(FieldLayoutSchemeDTO.class);
	
	private String description;
	private Long id;
	private String name;
	private List<ProjectDTO> projects;
	private List<FieldLayoutSchemeEntityDTO> entities;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		FieldLayoutScheme obj = (FieldLayoutScheme) o;
		this.description = obj.getDescription();
		this.entities = new ArrayList<>();
		for (FieldLayoutSchemeEntity e : obj.getEntities()) {
			FieldLayoutSchemeEntityDTO dto = new FieldLayoutSchemeEntityDTO();
			dto.setJiraObject(e, this);
			entities.add(dto);
		}
		this.id = obj.getId();
		this.name = obj.getName();
		this.projects = new ArrayList<>();
		for (Project p : obj.getProjectsUsing()) {
			ProjectDTO dto = new ProjectDTO();
			dto.setJiraObject(p);
			this.projects.add(dto);
		}
		this.uniqueKey = this.name;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Projects", new JiraConfigProperty(ProjectUtil.class, this.projects));
		r.put("Entities", new JiraConfigProperty(FieldLayoutSchemeEntityUtil.class, this.entities));
		return r;
	}

	protected void setupRelatedObjects() throws Exception {
		if (this.projects != null) {
			for (ProjectDTO dto : this.projects) {
				dto.addRelatedObject(this);
				this.addReferencedObject(dto);
			}
		}
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
				"getAssociatedIssueTypes",
				"getAssociatedProjects",
				"getFieldConfig");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return FieldLayoutSchemeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldLayoutScheme.class;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public List<ProjectDTO> getProjects() {
		return projects;
	}

	public void setProjects(List<ProjectDTO> projects) {
		this.projects = projects;
	}

	public List<FieldLayoutSchemeEntityDTO> getEntities() {
		return entities;
	}

	public void setEntities(List<FieldLayoutSchemeEntityDTO> entities) {
		this.entities = entities;
	}

}
