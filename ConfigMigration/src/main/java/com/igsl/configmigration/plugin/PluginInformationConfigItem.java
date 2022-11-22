package com.igsl.configmigration.plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.PluginInformation;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class PluginInformationConfigItem extends JiraConfigItem {

	private String description;
	private Map<String, String> parameters;
	private String vendorName;
	private String venorUrl;
	private String version;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		PluginInformation obj = (PluginInformation) o;
		this.description = obj.getDescription();
		this.parameters = obj.getParameters();
		this.vendorName = obj.getVendorName();
		this.venorUrl = obj.getVendorUrl();
		this.version = obj.getVersion();
	}

	@Override
	public String getUniqueKey() {
		return this.getDescription() + " " + this.getVersion();
	}

	@Override
	public String getInternalId() {
		return Integer.toString(this.hashCode());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getVendorName",
				"getVersion",
				"getParameters",
				"getDescription");
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVenorUrl() {
		return venorUrl;
	}

	public void setVenorUrl(String venorUrl) {
		this.venorUrl = venorUrl;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
