package com.igsl.configmigration.priority;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.issue.priority.Priority;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class PriorityUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(PriorityUtil.class);
	private static PriorityManager PRIORITY_MANAGER = ComponentAccessor.getComponent(PriorityManager.class);
	
	@Override
	public String getName() {
		return "Priority";
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new HashMap<>();
		for (Priority p : PRIORITY_MANAGER.getPriorities()) {
			PriorityDTO item = new PriorityDTO();
			item.setJiraObject(p);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Priority p = PRIORITY_MANAGER.getPriority(id);
		if (p != null) {
			PriorityDTO dto = new PriorityDTO();
			dto.setJiraObject(p);
			return dto;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (Priority p : PRIORITY_MANAGER.getPriorities()) {
			if (p.getName().equals(uniqueKey)) {
				PriorityDTO dto = new PriorityDTO();
				dto.setJiraObject(p);
				return dto;
			}
		}
		return null;
	}

	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		PriorityDTO original = null;
		if (oldItem != null) {
			original = (PriorityDTO) oldItem;
		} else {
			original = (PriorityDTO) findByUniqueKey(newItem.getUniqueKey());
		}
		PriorityDTO src = (PriorityDTO) newItem;
		if (original != null) {
			Priority p = (Priority) original.getJiraObject();
			// Update
			PRIORITY_MANAGER.editPriority(
					p, src.getName(), src.getDescription(), src.getIconUrl(), src.getStatusColor());
			return findByInternalId(p.getId());
		} else {
			// Create
			Priority p = PRIORITY_MANAGER.createPriority(
					src.getName(), src.getDescription(), src.getIconUrl(), src.getStatusColor());
			PriorityDTO dto = new PriorityDTO();
			dto.setJiraObject(p);
			return dto;
		}
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return PriorityDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

}
