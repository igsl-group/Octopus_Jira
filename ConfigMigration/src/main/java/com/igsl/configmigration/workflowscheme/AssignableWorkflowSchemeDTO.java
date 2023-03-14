package com.igsl.configmigration.workflowscheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.project.ProjectDTO;
import com.igsl.configmigration.project.ProjectUtil;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class AssignableWorkflowSchemeDTO extends JiraConfigDTO {
	
	public static final String NULL_KEY = "null";
	private static final Logger LOGGER = Logger.getLogger(AssignableWorkflowSchemeDTO.class);
	private static final WorkflowSchemeManager MANAGER = ComponentAccessor.getWorkflowSchemeManager();
	
	private String configuredDefaultWorkflow;
	private String description;
	private Long id;
	private Map<String, String> mappings;
	private String name;
	private List<ProjectDTO> projects;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		AssignableWorkflowScheme wf = (AssignableWorkflowScheme) obj;
		this.configuredDefaultWorkflow = wf.getConfiguredDefaultWorkflow();
		this.description = wf.getDescription();
		this.id = wf.getId();
		this.mappings = new HashMap<>();
		for (Map.Entry<String, String> entry : wf.getMappings().entrySet()) {
			if (entry.getKey() != null) {
				this.mappings.put(entry.getKey(), entry.getValue());
			} else {
				this.mappings.put(NULL_KEY, entry.getValue());
			}
		}
		this.name = wf.getName();
		this.projects = new ArrayList<>();
		LOGGER.debug("Projects associated with workflow scheme: " + wf.getName());
		for (Project p : MANAGER.getProjectsUsing(wf)) {
			LOGGER.debug("Project Key: " + p.getKey() + " Name: " + p.getName());
			ProjectDTO dto = new ProjectDTO();
			dto.setJiraObject(p);
			this.projects.add(dto);
		}
		this.uniqueKey = this.name;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Configured Default Workflow", new JiraConfigProperty(this.configuredDefaultWorkflow));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Mappings", new JiraConfigProperty(this.mappings));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Projects", new JiraConfigProperty(ProjectUtil.class, this.projects));
		return r;
	}
	
	protected void setupRelatedObjects() throws Exception {
		super.setupRelatedObjects();
		// Add self to associated Project's related object list
		for (ProjectDTO proj : this.projects) {
			proj.addRelatedObject(this);
			removeRelatedObject(proj);
		}
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getConfiguredDefaultWorkflow",
				"getDescription",
				"getMappings",
				"getName");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return WorkflowSchemeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return WorkflowScheme.class;
	}

	public String getConfiguredDefaultWorkflow() {
		return configuredDefaultWorkflow;
	}

	public void setConfiguredDefaultWorkflow(String configuredDefaultWorkflow) {
		this.configuredDefaultWorkflow = configuredDefaultWorkflow;
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

	public Map<String, String> getMappings() {
		return mappings;
	}

	public void setMappings(Map<String, String> mappings) {
		this.mappings = mappings;
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

}
