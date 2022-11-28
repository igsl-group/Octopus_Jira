package com.igsl.configmigration.fieldconfigscheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
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
	public void fromJiraObject(Object o, Object... params) throws Exception {
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
		// TODO Auto-generated method stub
		return null;
	}

}
