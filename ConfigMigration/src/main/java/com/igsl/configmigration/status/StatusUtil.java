package com.igsl.configmigration.status;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.priority.PriorityDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class StatusUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(StatusUtil.class);
	private static StatusManager MANAGER = ComponentAccessor.getComponent(StatusManager.class);
	private static StatusCategoryManager CATEGORY_MANAGER = ComponentAccessor.getComponent(StatusCategoryManager.class);
	
	@Override
	public String getName() {
		return "Status";
	}
	
	@SuppressWarnings("rawtypes")
	public Comparator getComparator() {
		return new StatusComparator();
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
			StatusDTO dto = (StatusDTO) item;
			StatusDTO jiraItem = (StatusDTO) findByDTO(dto);
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
						MANAGER.moveStatusDown(jiraItem.getId());
					} else if (compare == 1) {
						LOGGER.debug("Moving up");
						MANAGER.moveStatusUp(jiraItem.getId());
					}
					// Check new value
					int size = MANAGER.getStatuses().size();
					LOGGER.debug("Size: " + size);
					jiraItem = (StatusDTO) findByDTO(dto);
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
		Status s = MANAGER.getStatus(id);
		if (s != null) {
			StatusDTO item = new StatusDTO();
			item.setJiraObject(s);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (Status it : MANAGER.getStatuses()) {
			if (uniqueKey.equals(it.getName())) {
				StatusDTO item = new StatusDTO();
				item.setJiraObject(it);
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
		StatusDTO original = null;
		if (oldItem != null) {
			original = (StatusDTO) oldItem;
		} else {
			original = (StatusDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		Status originalJira = (original != null)? (Status) original.getJiraObject(): null;
		StatusDTO src = (StatusDTO) newItem;
		String name = src.getName();
		String description = src.getDescription();
		StatusCategoryDTO category = src.getStatusCategoryConfigItem();
		StatusCategory cat = CATEGORY_MANAGER.getStatusCategoryByKey(category.getKey());
		final String DUMMY_ICON_URL = ".";
		if (original != null) {
			// Update
			MANAGER.editStatus(originalJira, name, description, DUMMY_ICON_URL, cat);
			result.setNewDTO(findByInternalId(originalJira.getId()));
		} else {
			// Create
			Status createdJira = MANAGER.createStatus(name, description, DUMMY_ICON_URL, cat);
			StatusDTO created = new StatusDTO();
			created.setJiraObject(createdJira);
			result.setNewDTO(created);
		}
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return StatusDTO.class;
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
		LOGGER.debug("Filter: [" + filter + "]");
		Map<String, JiraConfigDTO> result = new LinkedHashMap<>();
		for (Status it : MANAGER.getStatuses()) {
			StatusDTO item = new StatusDTO();
			item.setJiraObject(it);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
