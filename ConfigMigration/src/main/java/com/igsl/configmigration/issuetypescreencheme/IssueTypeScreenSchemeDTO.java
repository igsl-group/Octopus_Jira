package com.igsl.configmigration.issuetypescreencheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.project.ProjectDTO;
import com.igsl.configmigration.project.ProjectUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeScreenSchemeDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(IssueTypeScreenSchemeDTO.class);
	public static final String GENERIC_VALUE_PROJECT_ID = "id";
	
	private String description;
	private Long id;
	private String name;
	private Collection<ProjectDTO> projects;
	private Collection<IssueTypeScreenSchemeEntityDTO> entities;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		ProjectUtil projectUtil = (ProjectUtil) JiraConfigTypeRegistry.getConfigUtil(ProjectUtil.class);
		IssueTypeScreenScheme obj = (IssueTypeScreenScheme) o;
		this.description = obj.getDescription();
		this.entities = new ArrayList<>();
		for (IssueTypeScreenSchemeEntity e : obj.getEntities()) {
			IssueTypeScreenSchemeEntityDTO dto = new IssueTypeScreenSchemeEntityDTO();
			dto.setJiraObject(e, obj);
			this.entities.add(dto);
		}
		this.id = obj.getId();
		this.name = obj.getName();
		this.projects = new ArrayList<>();
		LOGGER.debug("getProject GenericValue: ");
		for (GenericValue item : obj.getProjects()) {
			String projectId = item.getString(GENERIC_VALUE_PROJECT_ID);
			ProjectDTO dto = (ProjectDTO) projectUtil.findByInternalId(projectId);
			if (dto != null) {
				projects.add(dto);
			}
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
				"getProjects",
				"getEntities");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return IssueTypeScreenSchemeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return IssueTypeScreenScheme.class;
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

	public Collection<ProjectDTO> getProjects() {
		return projects;
	}

	public void setProjects(Collection<ProjectDTO> projects) {
		this.projects = projects;
	}

	public Collection<IssueTypeScreenSchemeEntityDTO> getEntities() {
		return entities;
	}

	public void setEntities(Collection<IssueTypeScreenSchemeEntityDTO> entities) {
		this.entities = entities;
	}

}
