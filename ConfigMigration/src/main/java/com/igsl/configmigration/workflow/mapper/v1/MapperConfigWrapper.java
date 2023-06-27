package com.igsl.configmigration.workflow.mapper.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.igsl.configmigration.JiraConfigSearchType;

public class MapperConfigWrapper {
	@JsonIgnore
	private boolean updated = false;
	private String regex = "^(.*)$";
	private String captureGroups = "1";
	private String replacement = "$1";
	private boolean disabled;
	private String workflowName;
	private String description;
	private String xPath;
	private String objectType;
	private String searchType;
	@JsonIgnore
	private MapperConfig config;
	public MapperConfigWrapper() {}
	public MapperConfigWrapper(MapperConfig config) {
		this.config = config;
		this.regex = config.getRegex();
		this.captureGroups = config.getCaptureGroups();
		this.replacement = config.getReplacement();
		this.description = config.getDescription();
		this.disabled = config.isDisabled();
		this.objectType = config.getObjectType();
		this.searchType = config.getSearchType();
		this.xPath = config.getXPath();
		this.workflowName = config.getWorkflowName();
	}
	public void copyTo(MapperConfigWrapper config) {
		config.setRegex(this.regex);
		config.setCaptureGroups(this.captureGroups);
		config.setReplacement(this.replacement);
		config.setDescription(this.description);
		config.setDisabled(this.disabled);
		config.setObjectType(this.objectType);
		config.setSearchType(this.searchType);
		config.setxPath(this.xPath);
		config.setWorkflowName(this.workflowName);
		config.copyTo(config.getConfig());
	}
	public void copyTo(MapperConfig config) {
		if (config != null) {
			config.setRegex(this.regex);
			config.setCaptureGroups(this.captureGroups);
			config.setReplacement(this.replacement);
			config.setDescription(this.description);
			config.setDisabled(this.disabled);
			config.setObjectType(this.objectType);
			config.setSearchType(this.searchType);
			config.setXPath(this.xPath);
			config.setWorkflowName(this.workflowName);
		}
	}
	@JsonIgnore
	public String getId() {
		if (this.config != null) {
			return Integer.toString(this.config.getID());
		}
		return null;
	}
	
	public void setConfig(MapperConfig config) {
		this.config = config;
	}	
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getxPath() {
		return xPath;
	}
	public void setxPath(String xPath) {
		this.xPath = xPath;
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public MapperConfig getConfig() {
		return config;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
	public String getCaptureGroups() {
		return captureGroups;
	}
	public void setCaptureGroups(String captureGroups) {
		this.captureGroups = captureGroups;
	}
	public String getReplacement() {
		return replacement;
	}
	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}
	public String getWorkflowName() {
		return workflowName;
	}
	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}
	public boolean isUpdated() {
		return updated;
	}
	public void setUpdated(boolean updated) {
		this.updated = updated;
	}
	public String getSearchType() {
		return searchType;
	}
	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}
}
