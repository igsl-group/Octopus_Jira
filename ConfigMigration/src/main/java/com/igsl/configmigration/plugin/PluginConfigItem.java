package com.igsl.configmigration.plugin;

import java.util.Arrays;
import java.util.List;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginState;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class PluginConfigItem extends JiraConfigItem {

	private String key;
	private String name;
	private PluginInformationConfigItem pluginInformation;
	private PluginState pluginState;
	private PluginArtifactConfigItem pluginArtifact;
	private int pluginVersion;
	private boolean bundledPlugin;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		Plugin obj = (Plugin) o;
		this.key = obj.getKey();
		this.name = obj.getName();
		this.pluginInformation = new PluginInformationConfigItem();
		this.pluginInformation.setJiraObject(obj.getPluginInformation());
		this.pluginState = obj.getPluginState();
		this.pluginArtifact = new PluginArtifactConfigItem();
		this.pluginArtifact.setJiraObject(obj.getPluginArtifact());
		this.pluginVersion = obj.getPluginsVersion();
		this.bundledPlugin = obj.isBundledPlugin();
	}

	/**
	 * Plugin bytes too massive for display
	 */
	@Override
	public List<String> getMapIgnoredMethods() {
		return Arrays.asList("getPluginArtifact");
	}
	
	@Override
	public String getUniqueKey() {
		return this.getName();
	}

	@Override
	public String getInternalId() {
		return this.getKey();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getKey",
				"getPluginVersion",
				"getPluginInformation",
				"getPluginState");
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PluginState getPluginState() {
		return pluginState;
	}

	public void setPluginState(PluginState pluginState) {
		this.pluginState = pluginState;
	}

	public PluginInformationConfigItem getPluginInformation() {
		return pluginInformation;
	}

	public void setPluginInformation(PluginInformationConfigItem pluginInformation) {
		this.pluginInformation = pluginInformation;
	}

	public int getPluginVersion() {
		return pluginVersion;
	}

	public void setPluginVersion(int pluginVersion) {
		this.pluginVersion = pluginVersion;
	}

	public PluginArtifactConfigItem getPluginArtifact() {
		return pluginArtifact;
	}

	public void setPluginArtifact(PluginArtifactConfigItem pluginArtifact) {
		this.pluginArtifact = pluginArtifact;
	}

	public boolean isBundledPlugin() {
		return bundledPlugin;
	}

	public void setBundledPlugin(boolean bundledPlugin) {
		this.bundledPlugin = bundledPlugin;
	}

}
