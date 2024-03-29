package com.igsl.configmigration.fieldscreen;

import java.util.HashMap;
import java.util.List;
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
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.customfield.CustomFieldDTO;
import com.igsl.configmigration.customfield.CustomFieldUtil;
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
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		FieldScreenTabDTO tab = (FieldScreenTabDTO) params[0];	
		for (FieldScreenLayoutItem it : MANAGER.getFieldScreenLayoutItems((FieldScreenTab) tab.getJiraObject())) {
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
		FieldScreenTabDTO tab = (FieldScreenTabDTO) params[0];
		for (FieldScreenLayoutItem it : MANAGER.getFieldScreenLayoutItems((FieldScreenTab) tab.getJiraObject())) {
			FieldScreenLayoutItemDTO item = new FieldScreenLayoutItemDTO();
			item.setJiraObject(it, tab);
			if (item.getUniqueKey().equals(uniqueKey)) {
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
		FieldScreenLayoutItemDTO original = null;
		if (oldItem != null) {
			original = (FieldScreenLayoutItemDTO) oldItem;
		} else {
			original = (FieldScreenLayoutItemDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		FieldScreenLayoutItem originalJira = (original != null)? (FieldScreenLayoutItem) original.getJiraObject(): null;
		FieldScreenLayoutItemDTO src = (FieldScreenLayoutItemDTO) newItem;
		FieldScreenTabDTO tab = (FieldScreenTabDTO) src.getObjectParameters()[0];
		FieldScreenLayoutItem createdJira = null;
		FieldUtil fieldUtil = (FieldUtil) JiraConfigTypeRegistry.getConfigUtil(FieldUtil.class);
		// Check importStore for CustomField mapping
		CustomFieldUtil cfUtil = (CustomFieldUtil) JiraConfigTypeRegistry.getConfigUtil((CustomFieldUtil.class));
		String fieldId = cfUtil.resolveFieldId(exportStore, importStore, src.getSystemField(), src.getCustomField());
		if (fieldId != null) {
			if (original != null) {
				// Update
				originalJira.setFieldId(fieldId);
				originalJira.setFieldScreenTab((FieldScreenTab) tab.getJiraObject());
				originalJira.setPosition(src.getPosition());
				LOGGER.debug("Update Field Id: " + fieldId + ", Tab: " + tab.getId() + ", Pos: " + src.getPosition());
				MANAGER.updateFieldScreenLayoutItem(originalJira);
				createdJira = originalJira;
			} else {
				// Create
				createdJira = new FieldScreenLayoutItemImpl(MANAGER, FIELD_MANAGER);
				createdJira.setFieldId(fieldId);
				createdJira.setFieldScreenTab((FieldScreenTab) tab.getJiraObject());
				createdJira.setPosition(src.getPosition());
				LOGGER.debug("Create Field Id: " + fieldId + ", Tab: " + tab.getId() + ", Pos: " + src.getPosition());
				MANAGER.createFieldScreenLayoutItem(createdJira);
			}
			FieldScreenLayoutItemDTO created = new FieldScreenLayoutItemDTO();
			created.setJiraObject(createdJira, tab);
			result.setNewDTO(created);
		} else {
			String target = (src.getCustomField() == null)? 
								src.getSystemField().getUniqueKey() : 
								src.getCustomField().getUniqueKey();
			LOGGER.error("Field " + target + " cannot be found, excluded from tab " + tab.getName());
			result.addWarning(
					"Field \"" + src.getConfigName() + 
					"\" cannot be found, excluded from tab \"" + tab.getConfigName() + "\"");
		}
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return FieldScreenLayoutItemDTO.class;
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
			FieldScreenTabDTO tab = (FieldScreenTabDTO) params[0];		
			for (FieldScreenLayoutItem it : MANAGER.getFieldScreenLayoutItems((FieldScreenTab) tab.getJiraObject())) {
				FieldScreenLayoutItemDTO item = new FieldScreenLayoutItemDTO();
				item.setJiraObject(it, tab);
				result.put(item.getUniqueKey(), item);
			}
		}
		return result;
	}

}
