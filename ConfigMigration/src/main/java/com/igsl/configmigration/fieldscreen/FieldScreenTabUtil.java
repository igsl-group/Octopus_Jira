package com.igsl.configmigration.fieldscreen;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenTabImpl;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldScreenTabUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(FieldScreenTabUtil.class);
	private static FieldScreenManager MANAGER = ComponentAccessor.getFieldScreenManager();
	
	@Override
	public String getName() {
		return "Screen Tab";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		FieldScreenTab s = MANAGER.getFieldScreenTab(idAsLong);
		if (s != null) {
			FieldScreenTabDTO item = new FieldScreenTabDTO();
			item.setJiraObject(s, params);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		FieldScreen fs = (FieldScreen) params[0];
		for (FieldScreenTab it : MANAGER.getFieldScreenTabs(fs)) {
			if (uniqueKey.equals(it.getName())) {
				FieldScreenTabDTO item = new FieldScreenTabDTO();
				item.setJiraObject(it, params);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		FieldScreenTabDTO original = null;
		if (oldItem != null) {
			original = (FieldScreenTabDTO) oldItem;
		} else {
			original = (FieldScreenTabDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		FieldScreenTab originalJira = (original != null)? (FieldScreenTab) original.getJiraObject(): null;
		FieldScreenTabDTO src = (FieldScreenTabDTO) newItem;
		FieldScreenTab createdJira = null;
		if (original != null) {
			// Update
			originalJira.setName(src.getName());
			originalJira.setPosition(src.getPosition());
			MANAGER.updateFieldScreenTab(originalJira);
			createdJira = originalJira;
		} else {
			// Create
			createdJira = new FieldScreenTabImpl(MANAGER);
			createdJira.setName(src.getName());
			createdJira.setPosition(src.getPosition());
			createdJira.setFieldScreen((FieldScreen) src.getObjectParameters()[0]);
			MANAGER.createFieldScreenTab(createdJira);
		}
		FieldScreenTabDTO created = new FieldScreenTabDTO();
		created.setJiraObject(createdJira, src.getObjectParameters());
		LOGGER.debug("Merging fields");
		FieldScreenLayoutItemUtil itemUtil = (FieldScreenLayoutItemUtil)
				JiraConfigTypeRegistry.getConfigUtil(FieldScreenLayoutItemUtil.class);
		// Create Field Screen Layout items
		for (FieldScreenLayoutItemDTO item : src.getFieldScreenLayoutItems()) {
			LOGGER.debug("merging field from: " + item + ", " + item.getField().getName());
			item.setJiraObject(null, created);
			FieldScreenLayoutItemDTO originalItem = (FieldScreenLayoutItemDTO) itemUtil.findByDTO(item);
			LOGGER.debug("merging field to: " + originalItem + ", " + 
					((originalItem != null)? originalItem.getField().getName() : ""));
			if (originalItem != null) {
				originalItem.setJiraObject(null, created);
			}
			itemUtil.merge(originalItem, item);
		}
		return created;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return FieldScreenDTO.class;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		// Filter is ignored
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		if (params != null && params.length == 1) {
			FieldScreen fs = (FieldScreen) params[0];
			for (FieldScreenTab it : MANAGER.getFieldScreenTabs(fs)) {
				FieldScreenTabDTO item = new FieldScreenTabDTO();
				item.setJiraObject(it, params);
				result.put(item.getUniqueKey(), item);
			}
		}
		return result;
	}

}
