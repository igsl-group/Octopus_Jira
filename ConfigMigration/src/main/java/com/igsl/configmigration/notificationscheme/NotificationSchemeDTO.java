package com.igsl.configmigration.notificationscheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.project.ProjectDTO;
import com.igsl.configmigration.project.ProjectUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class NotificationSchemeDTO extends JiraConfigDTO {

	private static final NotificationSchemeManager MANAGER = ComponentAccessor.getNotificationSchemeManager();
	
	private Long id;
	private String description;
	private String name;
	private String type;
	private List<NotificationSchemeEntityDTO> entities;
	private List<ProjectDTO> projects;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		Scheme scheme = (Scheme) o;
		this.id = scheme.getId();
		this.description = scheme.getDescription();
		this.name = scheme.getName();
		this.type = scheme.getType();
		this.entities = new ArrayList<>();
		if (scheme.getEntities() != null) {
			for (SchemeEntity e : scheme.getEntities()) {
				NotificationSchemeEntityDTO dto = new NotificationSchemeEntityDTO();
				dto.setJiraObject(e, this);
				this.entities.add(dto);
			}
		}
		this.projects = new ArrayList<>();
		List<Project> list = MANAGER.getProjects(scheme);
		if (list != null) {
			for (Project p : list) {
				ProjectDTO dto = new ProjectDTO();
				dto.setJiraObject(p);
				this.projects.add(dto);
			}
		}
		this.uniqueKey = this.name;
	}
	
	@Override
	public void setupRelatedObjects() {
		for (ProjectDTO dto : this.projects) {
			dto.addRelatedObject(this);
			this.addReferencedObject(dto);
		}
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Entities", new JiraConfigProperty(NotificationSchemeEntityUtil.class, this.entities));
		r.put("Projects", new JiraConfigProperty(ProjectUtil.class, this.projects));
		return r;
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getDescription",
				"getCompleteIconUrl",
				"getStatusColor",
				"getSvgIconUrl",
				"getSequence",
				"getIconUrl",
				"getRasterIconUrl");
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.id);
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return NotificationSchemeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return Scheme.class;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<NotificationSchemeEntityDTO> getEntities() {
		return entities;
	}

	public void setEntities(List<NotificationSchemeEntityDTO> entities) {
		this.entities = entities;
	}

	public List<ProjectDTO> getProjects() {
		return projects;
	}

	public void setProjects(List<ProjectDTO> projects) {
		this.projects = projects;
	}

}
