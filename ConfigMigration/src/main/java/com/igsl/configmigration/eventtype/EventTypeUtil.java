package com.igsl.configmigration.eventtype;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class EventTypeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(EventTypeUtil.class);
	private static EventTypeManager MANAGER = ComponentAccessor.getEventTypeManager();
	
	@Override
	public String getName() {
		return "Event Type";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		EventType et = MANAGER.getEventType(idAsLong);
		if (et != null) {
			EventTypeDTO dto = new EventTypeDTO();
			dto.setJiraObject(et);
			return dto;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		Collection<EventType> list = MANAGER.getEventTypes();
		if (list != null) {
			for (EventType et : list) {
				if (et.getName().equals(uniqueKey)) {
					EventTypeDTO dto = new EventTypeDTO();
					dto.setJiraObject(et);
					return dto;
				}
			}
		}
		return null;
	}

	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		EventTypeDTO original;
		if (oldItem != null) {
			original = (EventTypeDTO) oldItem;
		} else {
			original = (EventTypeDTO) findByDTO(newItem);
		}
		EventTypeDTO src = (EventTypeDTO) newItem;
		if (original != null) {
			MANAGER.editEventType(original.getId(), src.getName(), src.getDescription(), src.getTemplateId());
			// Reload
			EventTypeDTO updated = (EventTypeDTO) findByDTO(src);
			result.setNewDTO(updated);
		} else {
			// Create event type
			EventType ev = new EventType(src.getName(), src.getDescription(), src.getTemplateId());
			MANAGER.addEventType(ev);
			// Reload
			EventTypeDTO created = (EventTypeDTO) findByDTO(src);
			result.setNewDTO(created);
		}
		return result;
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return EventTypeDTO.class;
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
		Map<String, JiraConfigDTO> result = new LinkedHashMap<>();
		Collection<EventType> list = MANAGER.getEventTypes();
		if (list != null) {
			for (EventType et : list) {
				EventTypeDTO dto = new EventTypeDTO();
				dto.setJiraObject(et);
				result.put(dto.getUniqueKey(), dto);
			}
		}
		return result;
	}

}
