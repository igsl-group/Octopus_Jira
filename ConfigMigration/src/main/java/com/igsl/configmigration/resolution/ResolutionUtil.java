package com.igsl.configmigration.resolution;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ResolutionManager;
import com.atlassian.jira.issue.resolution.Resolution;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ResolutionUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ResolutionUtil.class);
	private static ResolutionManager RESOLUTION_MANAGER = ComponentAccessor.getComponent(ResolutionManager.class);
	
	@Override
	public String getName() {
		return "Resolution";
	}
	
	@Override
	public TypeReference<?> getTypeReference() {
		return new TypeReference<Map<String, ResolutionDTO>>() {};
	}
	
	@Override
	public Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigItem> result = new TreeMap<>();
		for (Resolution r : RESOLUTION_MANAGER.getResolutions()) {
			ResolutionDTO item = new ResolutionDTO();
			item.setJiraObject(r);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	/**
	 * params[0]: name
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		String identifier = (String) params[0];
		for (Resolution r : RESOLUTION_MANAGER.getResolutions()) {
			if (r.getName().equals(identifier)) {
				return r;
			}
		}
		return null;
	}
	
	public Object merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception {
		Resolution original = null;
		if (oldItem != null) {
			if (oldItem.getJiraObject() != null) {
				original = (Resolution) oldItem.getJiraObject();
			} else {
				original = (Resolution) findObject(oldItem.getUniqueKey());
			}
		} else {
			original = (Resolution) findObject(newItem.getUniqueKey());
		}
		ResolutionDTO src = (ResolutionDTO) newItem;
		if (original != null) {
			// Update
			RESOLUTION_MANAGER.editResolution(original, src.getName(), src.getDescription());
			return original;
		} else {
			// Create
			return RESOLUTION_MANAGER.createResolution(src.getName(), src.getDescription());
		}
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
