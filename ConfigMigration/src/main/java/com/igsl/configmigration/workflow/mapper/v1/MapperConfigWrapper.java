package com.igsl.configmigration.workflow.mapper.v1;

public class MapperConfigWrapper {
	private boolean array;
	private boolean disabled;
	private String description;
	private String xPath;
	private String objectType;	
	private MapperConfig config;
	public MapperConfigWrapper() {
		// Do nothing
	}
	public MapperConfigWrapper(MapperConfig config) {
		this.config = config;
		this.array = config.isArray();
		this.description = config.getDescription();
		this.disabled = config.isDisabled();
		this.objectType = config.getObjectType();
		this.xPath = config.getXPath();
	}
	public void copyTo(MapperConfig config) {
		config.setArray(this.array);
		config.setDescription(this.description);
		config.setDisabled(this.disabled);
		config.setObjectType(this.objectType);
		config.setXPath(this.xPath);
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
	public boolean isArray() {
		return array;
	}
	public void setArray(boolean array) {
		this.array = array;
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
}
