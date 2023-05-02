package com.igsl.configmigration.priorityscheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.PrioritySchemeService;
import com.atlassian.jira.issue.fields.config.manager.PrioritySchemeManager;
import com.atlassian.jira.project.Project;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldconfigscheme.FieldConfigSchemeDTO;
import com.igsl.configmigration.fieldconfigscheme.FieldConfigSchemeUtil;
import com.igsl.configmigration.priority.PriorityDTO;
import com.igsl.configmigration.priority.PriorityUtil;
import com.igsl.configmigration.project.ProjectDTO;
import com.igsl.configmigration.project.ProjectUtil;

/**
 * PriorityScheme is actually a FieldConfigScheme.
 * So this class is a wrapper over FieldConfigScheme. 
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class PrioritySchemeDTO extends JiraConfigDTO {

	private static final PrioritySchemeManager MANAGER = ComponentAccessor.getComponent(PrioritySchemeManager.class);
	
	private FieldConfigSchemeDTO scheme;
	private List<PriorityDTO> priorities;
	private PriorityDTO defaultPriority;
	private List<ProjectDTO> projects;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		ProjectUtil projectUtil = (ProjectUtil) JiraConfigTypeRegistry.getConfigUtil(ProjectUtil.class);
		PriorityUtil priorityUtil = (PriorityUtil) JiraConfigTypeRegistry.getConfigUtil(PriorityUtil.class);
		FieldConfigScheme obj = (FieldConfigScheme) o;
		this.scheme = new FieldConfigSchemeDTO();
		this.scheme.setJiraObject(obj);
		FieldConfig fc = (FieldConfig) this.scheme.getOneAndOnlyConfig().getJiraObject();
		this.priorities = new ArrayList<>();
		List<String> priorityList = MANAGER.getOptions(fc);
		if (priorityList != null) {
			for (String id : priorityList) {
				PriorityDTO dto = (PriorityDTO) priorityUtil.findByInternalId(id);
				if (dto != null) {
					this.priorities.add(dto);
				}
			}
		}
		String defaultId = MANAGER.getDefaultOption(fc);
		if (defaultId != null) {
			PriorityDTO dto = (PriorityDTO) priorityUtil.findByInternalId(defaultId);
			if (dto != null) {
				this.defaultPriority = dto;
			}
		}
		this.projects = new ArrayList<>();
		Set<Project> projects = MANAGER.getProjectsWithScheme(obj);
		if (projects != null) {
			for (Project p : projects) {
				ProjectDTO dto = (ProjectDTO) projectUtil.findByUniqueKey(p.getName());
				if (dto != null) {
					this.projects.add(dto);
				}
			}
		}		
		this.uniqueKey = this.scheme.getName();
	}
	
	@Override
	public void setupRelatedObjects() {
		if (this.defaultPriority != null) {
			this.addRelatedObject(this.defaultPriority);
			this.defaultPriority.addReferencedObject(this);
		}
		for (PriorityDTO dto : this.priorities) {
			this.addRelatedObject(dto);
			dto.addReferencedObject(this);
		}
		for (ProjectDTO dto : this.projects) {
			this.addRelatedObject(dto);
			dto.addReferencedObject(this);
		}
	}

	@Override
	public String getConfigName() {
		return this.scheme.getName();
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Scheme", new JiraConfigProperty(FieldConfigSchemeUtil.class, this.scheme));
		r.put("Priorites", new JiraConfigProperty(PriorityUtil.class, this.priorities));
		r.put("Default Priority", new JiraConfigProperty(PriorityUtil.class, this.defaultPriority));
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
		return this.scheme.getInternalId();
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return PrioritySchemeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldConfigScheme.class;	// TODO Will this be a problem?
	}

	public FieldConfigSchemeDTO getScheme() {
		return scheme;
	}

	public void setScheme(FieldConfigSchemeDTO scheme) {
		this.scheme = scheme;
	}

	public List<PriorityDTO> getPriorities() {
		return priorities;
	}

	public void setPriorities(List<PriorityDTO> priorities) {
		this.priorities = priorities;
	}

	public PriorityDTO getDefaultPriority() {
		return defaultPriority;
	}

	public void setDefaultPriority(PriorityDTO defaultPriority) {
		this.defaultPriority = defaultPriority;
	}

	public List<ProjectDTO> getProjects() {
		return projects;
	}

	public void setProjects(List<ProjectDTO> projects) {
		this.projects = projects;
	}

}
