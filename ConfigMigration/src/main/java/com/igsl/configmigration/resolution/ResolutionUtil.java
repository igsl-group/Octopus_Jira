package com.igsl.configmigration.resolution;

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
		return "Resolution (sequence not included)";
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (Resolution r : RESOLUTION_MANAGER.getResolutions()) {
			ResolutionDTO item = new ResolutionDTO();
			item.setJiraObject(r);
			result.put(item.getUniqueKey(), item);
		}
		return result;
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
			return findByInternalId(res.getId());
		} else {
			// Create
			Resolution createdJira = RESOLUTION_MANAGER.createResolution(src.getName(), src.getDescription());
			ResolutionDTO created = new ResolutionDTO();
			created.setJiraObject(createdJira);
			return created;
		}
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ResolutionDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

}
