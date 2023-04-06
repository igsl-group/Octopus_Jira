package com.igsl.configmigration.resolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ResolutionManager;
import com.atlassian.jira.issue.resolution.Resolution;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ResolutionUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ResolutionUtil.class);
	private static ResolutionManager RESOLUTION_MANAGER = ComponentAccessor.getComponent(ResolutionManager.class);
	
	@Override
	public String getName() {
		return "Resolution";
	}
	
	@SuppressWarnings("rawtypes")
	public Comparator getComparator() {
		return new ResolutionComparator();
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
			ResolutionDTO dto = (ResolutionDTO) item;
			ResolutionDTO jiraItem = (ResolutionDTO) findByDTO(dto);
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
						RESOLUTION_MANAGER.moveResolutionDown(jiraItem.getId());
					} else if (compare == 1) {
						LOGGER.debug("Moving up");
						RESOLUTION_MANAGER.moveResolutionUp(jiraItem.getId());
					}
					// Check new value
					int size = RESOLUTION_MANAGER.getResolutions().size();
					LOGGER.debug("Size: " + size);
					jiraItem = (ResolutionDTO) findByDTO(dto);
					Long newSeq = jiraItem.getSequence();
					LOGGER.debug("NewSeq: " + newSeq);
					if (newSeq != targetSeq) {
						// Break if not possible
						if (compare == 1 && (newSeq <= 1 || newSeq < targetSeq)) {
							// Move up and reached the top
							LOGGER.debug("Reached top or is impossible");
							break;
						} else if (compare == -1 && (newSeq >= size || newSeq > targetSeq)) {
							// Move down and reached the bottom
							LOGGER.debug("Reached bottom or is impossible");
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
		for (Resolution r : RESOLUTION_MANAGER.getResolutions()) {
			if (r.getId().equals(id)) {
				ResolutionDTO dto = new ResolutionDTO();
				dto.setJiraObject(r);
				return dto;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String searchTerm, Object... params) throws Exception {
		for (Resolution r : RESOLUTION_MANAGER.getResolutions()) {
			if (r.getName().equals(searchTerm)) {
				ResolutionDTO dto = new ResolutionDTO();
				dto.setJiraObject(r);
				return dto;
			}
		}
		return null;
	}
	
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		ResolutionDTO result = null;
		ResolutionDTO original = null;
		if (oldItem != null) {
			original = (ResolutionDTO) oldItem;
		} else {
			original = (ResolutionDTO) findByUniqueKey(newItem.getUniqueKey());
		}
		ResolutionDTO src = (ResolutionDTO) newItem;
		if (original != null) {
			// Update
			Resolution res = (Resolution) original.getJiraObject();
			RESOLUTION_MANAGER.editResolution(res, src.getName(), src.getDescription());
			result = (ResolutionDTO) findByInternalId(res.getId());
		} else {
			// Create
			Resolution createdJira = RESOLUTION_MANAGER.createResolution(src.getName(), src.getDescription());
			ResolutionDTO created = new ResolutionDTO();
			created.setJiraObject(createdJira);
			result = created;
		}
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ResolutionDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new LinkedHashMap<>();
		List<ResolutionDTO> list = new ArrayList<>();
		for (Resolution r : RESOLUTION_MANAGER.getResolutions()) {
			ResolutionDTO item = new ResolutionDTO();
			item.setJiraObject(r);
			if (!matchFilter(item, filter)) {
				continue;
			}
			list.add(item);
		}
		list.sort(new ResolutionComparator());
		LOGGER.debug("Resolution list: ");
		for (ResolutionDTO dto : list) {
			LOGGER.debug("Resolution: " + dto.getUniqueKey());
			result.put(dto.getUniqueKey(), dto);
		}
		return result;
	}

}
