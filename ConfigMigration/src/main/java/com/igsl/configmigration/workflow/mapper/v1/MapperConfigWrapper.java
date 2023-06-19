package com.igsl.configmigration.workflow.mapper.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MapperConfigWrapper {
	private String regex = "^(.*)$";
	private String captureGroups = "1";
	private String replacement = "$1";
	private boolean disabled;
	private String workflowName;
	private String description;
	private String xPath;
	private String objectType;	
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
		config.setxPath(this.xPath);
		config.setWorkflowName(this.workflowName);
		config.copyTo(config.getConfig());
	}
	public void copyTo(MapperConfig config) {
		config.setRegex(this.regex);
		config.setCaptureGroups(this.captureGroups);
		config.setReplacement(this.replacement);
		config.setDescription(this.description);
		config.setDisabled(this.disabled);
		config.setObjectType(this.objectType);
		config.setXPath(this.xPath);
		config.setWorkflowName(this.workflowName);
	}
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
}
