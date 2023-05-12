package com.igsl.configmigration.permissionscheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.PermissionScheme;
import com.atlassian.jira.permission.PermissionSchemeManager;
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
public class PermissionSchemeDTO extends JiraConfigDTO {

	private static final PermissionSchemeManager MANAGER = ComponentAccessor.getPermissionSchemeManager();
	
	private String description;
	private Long id;
	private String name;
	private List<PermissionSchemeEntityDTO> entities;
	private String type;
	private List<ProjectDTO> projects; 
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		Scheme obj = (Scheme) o;
		this.type = obj.getType();
		this.description = obj.getDescription();
		this.id = obj.getId();
		this.name = obj.getName();
		this.entities = new ArrayList<>();
		Collection<SchemeEntity> entities = obj.getEntities();
		Iterator<SchemeEntity> it = entities.iterator();
		while (it.hasNext()) {
			SchemeEntity e = it.next();
			PermissionSchemeEntityDTO dto = new PermissionSchemeEntityDTO();
			dto.setJiraObject(e);
			this.entities.add(dto);
		}
		this.entities.sort(new PermissionSchemeEntityComparator());
		// Project association
		this.projects = new ArrayList<>();
		for (Project p : MANAGER.getProjects(obj)) {
			ProjectDTO dto = new ProjectDTO();
			dto.setJiraObject(p);
			this.projects.add(dto);
		}
		this.uniqueKey = this.name;
	}
	
	@Override
	public void setupRelatedObjects() {
		if (this.projects != null) {
			for (ProjectDTO p : this.projects) {
				this.addRelatedObject(p);
				p.addReferencedObject(this);
			}
		}
		if (this.entities != null) {
			for (PermissionSchemeEntityDTO dto : this.entities) {
				if (dto.getParameter() != null) {
					Object value = dto.getParameter().getValue();
					if (value != null && value instanceof JiraConfigDTO) {
						JiraConfigDTO o = (JiraConfigDTO) value;
						this.addRelatedObject(o);
						o.addReferencedObject(this);
					}
				}
			}
		}
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Entities", new JiraConfigProperty(PermissionSchemeEntityUtil.class, this.entities));
		r.put("Type", new JiraConfigProperty(this.type));
		r.put("Projects", new JiraConfigProperty(ProjectUtil.class, this.projects));
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
				"getKey",
				"getPluginVersion",
				"getPluginInformation",
				"getPluginState");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return PermissionSchemeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return PermissionScheme.class;
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

	public List<PermissionSchemeEntityDTO> getEntities() {
		return entities;
	}

	public void setEntities(List<PermissionSchemeEntityDTO> entities) {
		this.entities = entities;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<ProjectDTO> getProjects() {
		return projects;
	}

	public void setProjects(List<ProjectDTO> projects) {
		this.projects = projects;
	}

}
