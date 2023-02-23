package com.igsl.configmigration.priority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.resolution.ResolutionDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class PriorityUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(PriorityUtil.class);
	private static PriorityManager PRIORITY_MANAGER = ComponentAccessor.getComponent(PriorityManager.class);
	
	@Override
	public String getName() {
		return "Priority";
	}
	
	@Override
	public boolean isPostSequenced() {
		return true;
	}
	
	@Override
	public void updateSequence(List<JiraConfigDTO> items) throws Exception {
		// Move each item up and down
		for (JiraConfigDTO item : items) {
			LOGGER.debug("Reordering item: " + item.getUniqueKey());
			PriorityDTO dto = (PriorityDTO) item;
			PriorityDTO jiraItem = (PriorityDTO) findByDTO(dto);
			if (jiraItem != null) {
				Long startSeq = jiraItem.getSequence();
				Long targetSeq = dto.getSequence();
				Long currentSeq = startSeq;
				LOGGER.debug("From " + currentSeq + " to " + targetSeq);
				// Move up or down
				int compare = startSeq.compareTo(targetSeq);
				LOGGER.debug("Compare: " + compare);
				// Until targetSeq is reached
				while (!currentSeq.equals(targetSeq)) {
					LOGGER.debug(currentSeq + " vs " + targetSeq);
					if (compare == -1) {
						LOGGER.debug("Moving down");
						PRIORITY_MANAGER.movePriorityDown(jiraItem.getId());
					} else if (compare == 1) {
						LOGGER.debug("Moving up");
						PRIORITY_MANAGER.movePriorityUp(jiraItem.getId());
					}
					// Check new value
					int size = PRIORITY_MANAGER.getPriorities().size();
					LOGGER.debug("Size: " + size);
					jiraItem = (PriorityDTO) findByDTO(dto);
					Long newSeq = jiraItem.getSequence();
					LOGGER.debug("NewSeq: " + newSeq);
					if (newSeq != targetSeq) {
						// Break if not possible
						if (compare == 1 && newSeq <= 1) {
							// Move up and reached the top
							LOGGER.debug("Reached top");
							break;
						} else if (compare == -1 && newSeq >= size) {
							// Move down and reached the bottom
							LOGGER.debug("Reached bottom");
							break;
						}
					}
					currentSeq = newSeq;
				}
			}
		}
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Priority p = PRIORITY_MANAGER.getPriority(id);
		if (p != null) {
			PriorityDTO dto = new PriorityDTO();
			dto.setJiraObject(p, params);
			return dto;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (Priority p : PRIORITY_MANAGER.getPriorities()) {
			if (p.getName().equals(uniqueKey)) {
				PriorityDTO dto = new PriorityDTO();
				dto.setJiraObject(p, params);
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

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		if (filter != null) {
			filter = filter.toLowerCase();
		}
		Map<String, JiraConfigDTO> result = new LinkedHashMap<>();
		List<PriorityDTO> list = new ArrayList<>();
		for (Priority p : PRIORITY_MANAGER.getPriorities()) {
			String name = p.getName().toLowerCase();
			String desc = (p.getDescription() == null)? "" : p.getDescription().toLowerCase();
			if (filter != null) {
				if (!name.contains(filter) && 
					!desc.contains(filter)) {
					continue;
				}
			}
			PriorityDTO item = new PriorityDTO();
			item.setJiraObject(p, params);
			list.add(item);
		}
		list.sort(new PriorityComparator());
		for (PriorityDTO p : list) {
			result.put(p.getUniqueKey(), p);
		}
		return result;
	}

	@Override
	public String getSearchHints() {
		return "Case-insensitive wildcard search on name and description";
	}

}
