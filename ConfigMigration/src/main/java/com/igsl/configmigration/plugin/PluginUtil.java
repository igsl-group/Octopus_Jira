package com.igsl.configmigration.plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.JiraPluginManager;
import com.atlassian.jira.startup.PluginInfo;
import com.atlassian.jira.startup.PluginInfoProvider;
import com.atlassian.jira.startup.PluginInfos;
import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.metadata.PluginMetadataManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.ConfigUtil;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;

@ConfigUtil
@JsonDeserialize(using = JsonDeserializer.None.class)
public class PluginUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(PluginUtil.class);
	private static final JiraPluginManager PLUGIN_MANAGER = 
			ComponentAccessor.getComponent(JiraPluginManager.class);
	private static final PluginInfoProvider PLUGIN_INFO_PROVIDER = 
			ComponentAccessor.getComponent(PluginInfoProvider.class);
	private static final PluginAccessor PLUGIN_ACCESSOR = ComponentAccessor.getPluginAccessor();
	private static final PluginMetadataManager PLUGIN_METADATA_MANAGER = 
			ComponentAccessor.getComponent(PluginMetadataManager.class);
	
	
	// APIs provided by Jira is unable to retrieve only User Installed plugins.
	// Looking at Jira code, they seem to be hardcoding package names.
	// As a workaround, we will ignore specific vendors.
	private static final List<String> IGNORED_VENDORS = Arrays.asList(
			"Atlassian",
			"Atlassian Software Systems Pty Ltd",
			"Atlassian Community",
			"Atlassian Software Systems",
			"The Apache Software Foundation", 
			"OSGi Alliance");
	
	@Override
	public String getName() {
		return "Plugin";
	}
	
	@Override
	public TypeReference<?> getTypeReference() {
		return new TypeReference<Map<String, PluginDTO>>() {};
	}
	
	@Override
	public Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigItem> result = new TreeMap<>();
		PluginInfos infos = PLUGIN_INFO_PROVIDER.getUserPlugins();
		Iterator<PluginInfo> it = infos.iterator();
		while (it.hasNext()) {
			PluginInfo pi = it.next();
			if (!IGNORED_VENDORS.contains(pi.getPluginInformation().getVendorName())) {
				Plugin p = PLUGIN_MANAGER.getPlugin(pi.getKey());
				PluginDTO item = new PluginDTO();
				item.setJiraObject(p);
				result.put(item.getUniqueKey(), item);
			}
		}
//		Iterator<Plugin> pluginList = PLUGIN_ACCESSOR
//			.getPlugins()
//			.parallelStream()
//			.filter(PLUGIN_METADATA_MANAGER::isUserInstalled).iterator();
//		while (pluginList.hasNext()) {
//			Plugin p = pluginList.next();
//			PluginDTO item = new PluginDTO();
//			item.setJiraObject(p);
//			result.put(item.getUniqueKey(), item);
//		}		
		return result;
	}

	/**
	 * params[0]: name
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		String identifier = (String) params[0];
		for (Plugin p : PLUGIN_MANAGER.getPlugins()) {
			if (p.getName().equals(identifier)) {
				return p;
			}
		}
		return null;
	}
	
	public Object merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception {
		PluginDTO src = (PluginDTO) newItem;
		byte[] data = src.getPluginArtifact().getArtifactDataBytes();
		Path tempFile = Files.createTempFile(src.getName(), ".jar");
		try {
			Files.write(tempFile, data, StandardOpenOption.TRUNCATE_EXISTING);
			JarPluginArtifact artifact = new JarPluginArtifact(tempFile.toFile());
			Set<String> r = PLUGIN_MANAGER.installPlugins(artifact);
			LOGGER.debug("installPlugins: " + OM.writeValueAsString(r));
		} finally {
			Files.delete(tempFile);
		}
		return null;
	}
	
	@Override
	public void merge(Map<String, ImportData> items) throws Exception {
		for (ImportData data : items.values()) {
			try {
				merge(data.getServer(), data.getData());
				data.setImportResult("Updated");
			} catch (Exception ex) {
				data.setImportResult(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
				throw ex;
			}
		}
	}

}
