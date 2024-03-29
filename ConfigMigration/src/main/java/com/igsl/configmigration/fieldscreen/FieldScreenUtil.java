package com.igsl.configmigration.fieldscreen;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldScreenUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(FieldScreenUtil.class);
	private static FieldScreenManager MANAGER = ComponentAccessor.getFieldScreenManager();
	
	@Override
	public String getName() {
		return "Screen";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		FieldScreen s = MANAGER.getFieldScreen(idAsLong);
		if (s != null) {
			FieldScreenDTO item = new FieldScreenDTO();
			item.setJiraObject(s, params);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (FieldScreen it : MANAGER.getFieldScreens()) {
			if (uniqueKey.equals(it.getName())) {
				FieldScreenDTO item = new FieldScreenDTO();
				item.setJiraObject(it, params);
				return item;
			}
		}
		return null;
	}

	@Override
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		FieldScreenDTO original = null;
		if (oldItem != null) {
			original = (FieldScreenDTO) oldItem;
		} else {
			original = (FieldScreenDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		FieldScreen originalJira = (original != null)? (FieldScreen) original.getJiraObject(): null;
		FieldScreenDTO src = (FieldScreenDTO) newItem;
		FieldScreen createdJira = null;
		if (original != null) {
			// Update
			originalJira.setDescription(src.getDescription());
			originalJira.setName(src.getName());
			MANAGER.updateFieldScreen(originalJira);
			createdJira = originalJira;
		} else {
			// Create
			createdJira = new FieldScreenImpl(MANAGER);
			createdJira.setDescription(src.getDescription());
			createdJira.setName(src.getName());
			MANAGER.createFieldScreen(createdJira);
		}
		FieldScreenDTO created = new FieldScreenDTO();
		created.setJiraObject(createdJira);
		result.setNewDTO(created);
		LOGGER.debug("Removing existing tabs");
		// Delete existing tabs and items, then recreate them
		for (FieldScreenTab tab : createdJira.getTabs()) {
			for (FieldScreenLayoutItem item : tab.getFieldScreenLayoutItems()) {
				MANAGER.removeFieldScreenLayoutItem(item);
			}
			MANAGER.removeFieldScreenTab(tab.getId());
		}
		LOGGER.debug("Recreating tabs");
		// Tabs
		FieldScreenTabUtil tabUtil = (FieldScreenTabUtil)
				JiraConfigTypeRegistry.getConfigUtil(FieldScreenTabUtil.class);
		for (FieldScreenTabDTO tab : src.getTabs()) {
			tab.setJiraObject(null, created);
			tabUtil.merge(exportStore, null, importStore, tab);
		}
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return FieldScreenDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (FieldScreen it : MANAGER.getFieldScreens()) {
			FieldScreenDTO item = new FieldScreenDTO();
			item.setJiraObject(it, params);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
