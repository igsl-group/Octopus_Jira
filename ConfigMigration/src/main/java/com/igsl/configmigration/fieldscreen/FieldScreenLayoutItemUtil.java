package com.igsl.configmigration.fieldscreen;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.field.FieldDTO;
import com.igsl.configmigration.field.FieldUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldScreenLayoutItemUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(FieldScreenLayoutItemUtil.class);
	private static FieldScreenManager MANAGER = ComponentAccessor.getFieldScreenManager();
	private static FieldManager FIELD_MANAGER = ComponentAccessor.getFieldManager();
	
	@Override
	public String getName() {
		return "Screen Tab Layout Item";
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		FieldScreenTab tab = (FieldScreenTab) params[0];		
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (FieldScreenLayoutItem it : MANAGER.getFieldScreenLayoutItems(tab)) {
			FieldScreenLayoutItemDTO item = new FieldScreenLayoutItemDTO();
			item.setJiraObject(it, tab);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		FieldScreenTab tab = (FieldScreenTab) params[0];	
		for (FieldScreenLayoutItem it : MANAGER.getFieldScreenLayoutItems(tab)) {
			if (Long.toString(it.getId()).equals(id)) {
				FieldScreenLayoutItemDTO item = new FieldScreenLayoutItemDTO();
				item.setJiraObject(it, tab);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		return findByInternalId(uniqueKey, params);
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		FieldScreenLayoutItemDTO original = null;
		if (oldItem != null) {
			original = (FieldScreenLayoutItemDTO) oldItem;
		} else {
			original = (FieldScreenLayoutItemDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		FieldScreenLayoutItem originalJira = (original != null)? (FieldScreenLayoutItem) original.getJiraObject(): null;
		FieldScreenLayoutItemDTO src = (FieldScreenLayoutItemDTO) newItem;
		FieldScreenTab tab = (FieldScreenTab) src.getObjectParameters()[0];
		FieldScreenLayoutItem createdJira = null;
		FieldUtil fieldUtil = (FieldUtil) JiraConfigTypeRegistry.getConfigUtil(FieldUtil.class);
		FieldDTO field = (FieldDTO) fieldUtil.findByDTO(src.getField());
		if (field != null) {
			if (original != null) {
				// Update
				originalJira.setFieldId(field.getId());
				originalJira.setFieldScreenTab(tab);
				originalJira.setPosition(src.getPosition());
				MANAGER.updateFieldScreenLayoutItem(originalJira);
				createdJira = originalJira;
			} else {
				// Create
				createdJira = new FieldScreenLayoutItemImpl(MANAGER, FIELD_MANAGER);
				createdJira.setFieldId(field.getId());
				createdJira.setFieldScreenTab(tab);
				createdJira.setPosition(src.getPosition());
				MANAGER.createFieldScreenLayoutItem(createdJira);
			}
		} else {
			LOGGER.error("Field " + src.getField().getUniqueKey() + " cannot be found, excluded from tab " + tab.getName());
		}
		FieldScreenLayoutItemDTO created = new FieldScreenLayoutItemDTO();
		created.setJiraObject(createdJira, tab);
		return created;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return FieldScreenLayoutItemDTO.class;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

}
