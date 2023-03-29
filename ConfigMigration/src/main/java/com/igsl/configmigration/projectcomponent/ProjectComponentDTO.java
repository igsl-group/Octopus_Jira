package com.igsl.configmigration.projectcomponent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.project.ProjectCategory;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.applicationuser.ApplicationUserDTO;

/**
 * Project wrapper
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectComponentDTO extends JiraConfigDTO {

	private Long assigneeType;
	private ApplicationUserDTO componentLead;
	private String description;
	private Long id;
	private String lead;
	private String name;
	private Long projectId;
	private boolean archived;
	
	/**
	 * #0: project ID as Long
	 */
	@Override
	protected int getObjectParameterCount() {
		return 1;
	}
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		ProjectComponent o = (ProjectComponent) obj;
		this.archived = o.isArchived();
		this.assigneeType = o.getAssigneeType();
		this.componentLead = new ApplicationUserDTO();
		this.componentLead.setJiraObject(o.getComponentLead());
		this.description = o.getDescription();
		this.id = o.getId();
		this.lead = o.getLead();
		this.name = o.getName();
		this.projectId = o.getProjectId();
		this.uniqueKey = this.name;
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Archived", new JiraConfigProperty(this.archived));
		r.put("Assignee Type", new JiraConfigProperty(this.assigneeType));
		r.put("Component Lead", new JiraConfigProperty(this.componentLead));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Lead", new JiraConfigProperty(this.lead));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Project ID", new JiraConfigProperty(this.projectId));
		return r;
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"isArchived",
				"getAssigneeType",
				"getComponentLead",
				"getDescription",
				"getLead",
				"getName");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return ProjectComponentUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return ProjectCategory.class;
	}

	public Long getAssigneeType() {
		return assigneeType;
	}

	public void setAssigneeType(Long assigneeType) {
		this.assigneeType = assigneeType;
	}

	public ApplicationUserDTO getComponentLead() {
		return componentLead;
	}

	public void setComponentLead(ApplicationUserDTO componentLead) {
		this.componentLead = componentLead;
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

	public String getLead() {
		return lead;
	}

	public void setLead(String lead) {
		this.lead = lead;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

}
