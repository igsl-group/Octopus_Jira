package com.igsl.configmigration.fieldscreen;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldScreenUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(FieldScreenUtil.class);
	private static FieldScreenManager MANAGER = ComponentAccessor.getFieldScreenManager();
	
	@Override
	public String getName() {
		return "Screen";
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (FieldScreen it : MANAGER.getFieldScreens()) {
			FieldScreenDTO item = new FieldScreenDTO();
			item.setJiraObject(it);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		FieldScreen s = MANAGER.getFieldScreen(idAsLong);
		if (s != null) {
			FieldScreenDTO item = new FieldScreenDTO();
			item.setJiraObject(s);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (FieldScreen it : MANAGER.getFieldScreens()) {
			if (uniqueKey.equals(it.getName())) {
				FieldScreenDTO item = new FieldScreenDTO();
				item.setJiraObject(it);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		FieldScreenDTO original = null;
		if (oldItem != null) {
			original = (FieldScreenDTO) oldItem;
		} else {
			original = (FieldScreenDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		FieldScreen originalJira = (original != null)? (FieldScreen) original.getJiraObject(): null;
		FieldScreenDTO src = (FieldScreenDTO) newItem;
		String name = src.getName();
		String description = src.getDescription();
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
		if (createdJira != null) {
			// Tabs
			FieldScreenTabUtil tabUtil = (FieldScreenTabUtil)
					JiraConfigTypeRegistry.getConfigUtil(FieldScreenTabUtil.class);
			for (FieldScreenTabDTO tab : src.getTabs()) {
				tab.setJiraObject(null, createdJira);
				tabUtil.merge(null, tab);
			}
			FieldScreenDTO created = new FieldScreenDTO();
			created.setJiraObject(createdJira);
			return created;
		}
		return null;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return FieldScreenDTO.class;
	}

	@Override
	public boolean isPublic() {
		return true;
	}

}
