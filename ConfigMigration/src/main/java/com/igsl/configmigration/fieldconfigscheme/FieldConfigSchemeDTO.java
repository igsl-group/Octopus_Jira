package com.igsl.configmigration.fieldconfigscheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
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
public class FieldConfigSchemeDTO extends JiraConfigDTO {

	private List<IssueTypeDTO> associatedIssueTypes;
	private List<ProjectDTO> assocatedProjectObjects;
	private String description;
	private Long id;
	private String name;
	private FieldConfigDTO oneAndOnlyConfig;
	private List<ProjectDTO> projectId;
	private Map<String, FieldConfigDTO> configs;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		FieldConfigScheme obj = (FieldConfigScheme) o;
		this.associatedIssueTypes = new ArrayList<>();
		for (IssueType it : obj.getAssociatedIssueTypes()) {
			IssueTypeDTO item = new IssueTypeDTO();
			item.setJiraObject(it);
			this.associatedIssueTypes.add(item);
		}
		this.assocatedProjectObjects = new ArrayList<>();
		for (Project it : obj.getAssociatedProjectObjects()) {
			ProjectDTO item = new ProjectDTO();
			item.setJiraObject(it);
			this.assocatedProjectObjects.add(item);
		}
		this.configs = new HashMap<>();
		for (Map.Entry<String, FieldConfig> entry : obj.getConfigs().entrySet()) {
			FieldConfigDTO item = new FieldConfigDTO();
			item.setJiraObject(entry.getValue());
			String key;
			if (entry.getKey() == null) {
				key = "null";	// JSON does not allow null map keys
			} else {
				key = entry.getKey();
			}
			this.configs.put(key, item);
		}
		this.projectId = new ArrayList<>();
		for (JiraContextNode node : obj.getContexts()) {
			ProjectDTO item = new ProjectDTO();
			item.setJiraObject(node.getProjectObject());
			this.projectId.add(item);
		}
		this.description = obj.getDescription();
		//obj.getField();
		this.id = obj.getId();
		this.name = obj.getName();
		this.oneAndOnlyConfig = new FieldConfigDTO();
		this.oneAndOnlyConfig.setJiraObject(obj.getOneAndOnlyConfig());
		// Nested object under CustomField, so simply use id
		this.uniqueKey = Long.toString(this.id);
	}
	
	@Override
	public String getConfigName() {
		return this.name;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Associated Issue Types", new JiraConfigProperty(IssueTypeUtil.class, this.associatedIssueTypes));
		r.put("Associated Projects", new JiraConfigProperty(ProjectUtil.class, this.assocatedProjectObjects));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("ID", new JiraConfigProperty(Long.toString(this.id)));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Field Config", new JiraConfigProperty(FieldConfigUtil.class, this.oneAndOnlyConfig));
		r.put("Project", new JiraConfigProperty(ProjectUtil.class, this.projectId));
		r.put("Configs", new JiraConfigProperty(FieldConfigUtil.class, configs));
		return r;
	}
	
	protected void setupRelatedObjects() throws Exception {
		// Add self to associated Project's related object list
		// But remove projects from relatedObjects
		for (ProjectDTO proj : this.assocatedProjectObjects) {
			proj.addRelatedObject(this);
			this.addReferencedObject(proj);
		}
		for (ProjectDTO proj : this.projectId) {
			proj.addRelatedObject(this);
			this.addReferencedObject(proj);
		}
		for (IssueTypeDTO issueType : this.associatedIssueTypes) {
			addRelatedObject(issueType);
			issueType.addReferencedObject(this);
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
				"getAssocatedProjectObjects",
				"getOneAndOnlyConfig");
	}

	public List<IssueTypeDTO> getAssociatedIssueTypes() {
		return associatedIssueTypes;
	}

	public void setAssociatedIssueTypes(List<IssueTypeDTO> associatedIssueTypes) {
		this.associatedIssueTypes = associatedIssueTypes;
	}

	public List<ProjectDTO> getAssocatedProjectObjects() {
		return assocatedProjectObjects;
	}

	public void setAssocatedProjectObjects(List<ProjectDTO> assocatedProjectObjects) {
		this.assocatedProjectObjects = assocatedProjectObjects;
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

	public FieldConfigDTO getOneAndOnlyConfig() {
		return oneAndOnlyConfig;
	}

	public void setOneAndOnlyConfig(FieldConfigDTO oneAndOnlyConfig) {
		this.oneAndOnlyConfig = oneAndOnlyConfig;
	}

	public List<ProjectDTO> getProjectId() {
		return projectId;
	}

	public void setProjectId(List<ProjectDTO> projectId) {
		this.projectId = projectId;
	}

	public Map<String, FieldConfigDTO> getConfigs() {
		return configs;
	}

	public void setConfigs(Map<String, FieldConfigDTO> configs) {
		this.configs = configs;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return FieldConfigSchemeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldConfigScheme.class;
	}

}
