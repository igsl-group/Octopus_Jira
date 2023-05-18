package com.igsl.configmigration.priorityscheme;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.PrioritySchemeManager;
import com.atlassian.jira.project.Project;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.priority.PriorityDTO;
import com.igsl.configmigration.priority.PriorityUtil;
import com.igsl.configmigration.project.ProjectDTO;
import com.igsl.configmigration.project.ProjectUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class PrioritySchemeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(PrioritySchemeUtil.class);
	private static PrioritySchemeManager MANAGER = ComponentAccessor.getComponent(PrioritySchemeManager.class);
	
	@Override
	public String getName() {
		return "Priority Scheme";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		List<FieldConfigScheme> list = MANAGER.getAllSchemes();
		if (list != null) {
			for (FieldConfigScheme scheme : list) {
				if (scheme.getId().equals(idAsLong)) {
					PrioritySchemeDTO dto = new PrioritySchemeDTO();
					dto.setJiraObject(scheme, params);
					return dto;
				}
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		List<FieldConfigScheme> list = MANAGER.getAllSchemes();
		if (list != null) {
			for (FieldConfigScheme scheme : list) {
				if (scheme.getName().equals(uniqueKey)) {
					PrioritySchemeDTO dto = new PrioritySchemeDTO();
					dto.setJiraObject(scheme, params);
					return dto;
				}
			}
		}
		return null;
	}

	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		PriorityUtil priorityUtil = (PriorityUtil) JiraConfigTypeRegistry.getConfigUtil(PriorityUtil.class);
		ProjectUtil projectUtil = (ProjectUtil) JiraConfigTypeRegistry.getConfigUtil(ProjectUtil.class);
		MergeResult result = new MergeResult();
		PrioritySchemeDTO original = null;
		if (oldItem != null) {
			original = (PrioritySchemeDTO) oldItem;
		} else {
			original = (PrioritySchemeDTO) findByUniqueKey(newItem.getUniqueKey());
		}
		PrioritySchemeDTO src = (PrioritySchemeDTO) newItem;
		List<String> optionIds = new ArrayList<>();
		for (PriorityDTO dto : src.getPriorities()) {
			PriorityDTO item = (PriorityDTO) priorityUtil.findByDTO(dto);
			if (item != null) {
				optionIds.add(item.getId());
			}
		}
		FieldConfigScheme createdJira = null;
		if (original != null) {
			// Update
			createdJira = (FieldConfigScheme) original.getJiraObject();
			MANAGER.updateWithDefaultMapping(createdJira, optionIds);
		} else { 
			// Create
			createdJira = MANAGER.createWithDefaultMapping(
					src.getScheme().getName(), src.getScheme().getDescription(), optionIds);
		}
		// Set default
		String defaultId = null;
		if (src.getDefaultPriority() != null) {
			PriorityDTO item = (PriorityDTO) priorityUtil.findByDTO(src.getDefaultPriority());
			if (item != null) {
				defaultId = item.getId();
			}
		}
		MANAGER.setDefaultOption(createdJira.getOneAndOnlyConfig(), defaultId);
		// Associate with project
		if (src.getScheme().getAssocatedProjectObjects() != null) {
			for (ProjectDTO proj : src.getScheme().getAssocatedProjectObjects()) {
				ProjectDTO p = (ProjectDTO) projectUtil.findByDTO(proj);
				if (p != null) {
					MANAGER.assignProject(createdJira, (Project) p.getJiraObject());
				}
			}
		}
		PrioritySchemeDTO dto = new PrioritySchemeDTO();
		dto.setJiraObject(createdJira);
		result.setNewDTO(dto);
		return result;
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return PrioritySchemeDTO.class;
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
		List<FieldConfigScheme> list = MANAGER.getAllSchemes();
		if (list != null) {
			for (FieldConfigScheme scheme : list) {
				PrioritySchemeDTO dto = new PrioritySchemeDTO();
				dto.setJiraObject(scheme, params);
				result.put(dto.getUniqueKey(), dto);
			}
		}
		return result;
	}

}
