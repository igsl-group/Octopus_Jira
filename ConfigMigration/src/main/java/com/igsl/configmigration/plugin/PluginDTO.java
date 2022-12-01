package com.igsl.configmigration.plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginState;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class PluginDTO extends JiraConfigDTO {

	private String key;
	private String name;
	private PluginInformationDTO pluginInformation;
	private PluginState pluginState;
	private PluginArtifactDTO pluginArtifact;
	private int pluginVersion;
	private boolean bundledPlugin;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		Plugin obj = (Plugin) o;
		this.key = obj.getKey();
		this.name = obj.getName();
		this.pluginInformation = new PluginInformationDTO();
		this.pluginInformation.setJiraObject(obj.getPluginInformation());
		this.pluginState = obj.getPluginState();
		this.pluginArtifact = new PluginArtifactDTO();
		this.pluginArtifact.setJiraObject(obj.getPluginArtifact());
		this.pluginVersion = obj.getPluginsVersion();
		this.bundledPlugin = obj.isBundledPlugin();
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

	public PluginInformationDTO getPluginInformation() {
		return pluginInformation;
	}

	public void setPluginInformation(PluginInformationDTO pluginInformation) {
		this.pluginInformation = pluginInformation;
	}

	public int getPluginVersion() {
		return pluginVersion;
	}

	public void setPluginVersion(int pluginVersion) {
		this.pluginVersion = pluginVersion;
	}

	public PluginArtifactDTO getPluginArtifact() {
		return pluginArtifact;
	}

	public void setPluginArtifact(PluginArtifactDTO pluginArtifact) {
		this.pluginArtifact = pluginArtifact;
	}

	public boolean isBundledPlugin() {
		return bundledPlugin;
	}

	public void setBundledPlugin(boolean bundledPlugin) {
		this.bundledPlugin = bundledPlugin;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return PluginUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return Plugin.class;
	}

}
