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
import com.atlassian.jira.issue.fields.screen.FieldScreenTabImpl;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;

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
		FieldScreenDTO fs = (FieldScreenDTO) params[0];
		for (FieldScreenTab it : MANAGER.getFieldScreenTabs((FieldScreen) fs.getJiraObject())) {
			String key = it.getFieldScreen().getName() + "." + it.getName();
			if (uniqueKey.equals(key)) {
				FieldScreenTabDTO item = new FieldScreenTabDTO();
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
			LOGGER.debug("Updating FieldScreenTab");
			originalJira.setName(src.getName());
			originalJira.setPosition(src.getPosition());
			MANAGER.updateFieldScreenTab(originalJira);
			// TODO Update items
			createdJira = originalJira;
		} else {
			// Create
			LOGGER.debug("Creating FieldScreenTab");
			createdJira = new FieldScreenTabImpl(MANAGER);
			createdJira.setName(src.getName());
			createdJira.setPosition(src.getPosition());
			FieldScreenDTO fs = (FieldScreenDTO) src.getObjectParameters()[0];
			createdJira.setFieldScreen((FieldScreen) fs.getJiraObject());
			MANAGER.createFieldScreenTab(createdJira);
		}
		FieldScreenTabDTO created = new FieldScreenTabDTO();
		created.setJiraObject(createdJira, src.getObjectParameters());
		result.setNewDTO(created);
		LOGGER.debug("Merging fields");
		FieldScreenLayoutItemUtil itemUtil = (FieldScreenLayoutItemUtil)
				JiraConfigTypeRegistry.getConfigUtil(FieldScreenLayoutItemUtil.class);
		// Create Field Screen Layout items
		int position = 0;
		for (FieldScreenLayoutItemDTO item : src.getFieldScreenLayoutItems()) {
			item.setJiraObject(null, created);
			LOGGER.debug("Creating Field: " + item.getFieldId() + ", Pos: " + position);
			item.setPosition(position);
			if (itemUtil.merge(exportStore, null, importStore, item) != null) {
				position++;
			}
		}
		return result;
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
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		// Filter is ignored
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		if (params != null && params.length == 1) {
			FieldScreenDTO fs = (FieldScreenDTO) params[0];
			for (FieldScreenTab it : MANAGER.getFieldScreenTabs((FieldScreen) fs.getJiraObject())) {
				FieldScreenTabDTO item = new FieldScreenTabDTO();
				item.setJiraObject(it, params);
				result.put(item.getUniqueKey(), item);
			}
		}
		return result;
	}

}
