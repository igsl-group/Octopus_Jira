package com.igsl.configmigration.plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.plugin.PluginInformation;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class PluginInformationDTO extends JiraConfigDTO {

	private String description;
	private Map<String, String> parameters;
	private String vendorName;
	private String venorUrl;
	private String version;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		PluginInformation obj = (PluginInformation) o;
		this.description = obj.getDescription();
		this.parameters = obj.getParameters();
		this.vendorName = obj.getVendorName();
		this.venorUrl = obj.getVendorUrl();
		this.version = obj.getVersion();
		this.uniqueKey = this.vendorName + " - " + this.description + " - " + this.version;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Parameters", new JiraConfigProperty(this.parameters));
		r.put("Vendor Name", new JiraConfigProperty(this.vendorName));
		r.put("Vendor URL", new JiraConfigProperty(this.venorUrl));
		r.put("Version", new JiraConfigProperty(this.version));
		return r;
	}

	@Override
	public String getInternalId() {
		return this.uniqueKey;
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

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return PluginInformationUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return PluginInformation.class;
	}

}
