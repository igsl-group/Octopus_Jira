package com.igsl.configmigration.notificationscheme;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.applicationuser.ApplicationUserDTO;
import com.igsl.configmigration.customfield.CustomFieldDTO;
import com.igsl.configmigration.customfield.CustomFieldUtil;
import com.igsl.configmigration.general.GeneralDTO;
import com.igsl.configmigration.group.GroupDTO;
import com.igsl.configmigration.project.ProjectDTO;
import com.igsl.configmigration.project.ProjectUtil;
import com.igsl.configmigration.projectrole.ProjectRoleDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class NotificationSchemeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(NotificationSchemeUtil.class);
	private static NotificationSchemeManager MANAGER = ComponentAccessor.getNotificationSchemeManager();
	
	@Override
	public String getName() {
		return "Notification Scheme";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		List<Scheme> list = MANAGER.getSchemeObjects();
		if (list != null) {
			for (Scheme scheme : list) {
				if (scheme.getId().equals(idAsLong)) {
					NotificationSchemeDTO dto = new NotificationSchemeDTO();
					dto.setJiraObject(scheme, params);
					return dto;
				}
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		List<Scheme> list = MANAGER.getSchemeObjects();
		if (list != null) {
			for (Scheme scheme : list) {
				if (scheme.getName().equals(uniqueKey)) {
					NotificationSchemeDTO dto = new NotificationSchemeDTO();
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
		MergeResult result = new MergeResult();
		ProjectUtil projectUtil = (ProjectUtil) JiraConfigTypeRegistry.getConfigUtil(ProjectUtil.class);
		EventTypeUtil eventUtil = (EventTypeUtil) JiraConfigTypeRegistry.getConfigUtil(EventTypeUtil.class);
		CustomFieldUtil cfUtil = (CustomFieldUtil) JiraConfigTypeRegistry.getConfigUtil(CustomFieldUtil.class);
		NotificationSchemeDTO original = null;
		if (oldItem != null) {
			original = (NotificationSchemeDTO) oldItem;
		} else {
			original = (NotificationSchemeDTO) findByUniqueKey(newItem.getUniqueKey());
		}
		NotificationSchemeDTO src = (NotificationSchemeDTO) newItem;
		if (original != null) {
			// Delete and recreate
			MANAGER.deleteScheme(original.getId());
		} 
		// Create
		// Create entities
		Collection<SchemeEntity> list = Collections.emptyList();
		Scheme createdJira = new Scheme(null, src.getType(), src.getName(), src.getDescription(), list);
		Collection<SchemeEntity> entities = createdJira.getEntities();
		for (NotificationSchemeEntityDTO e : src.getEntities()) {
			// Map event type
			EventTypeDTO event = (EventTypeDTO) eventUtil.findByDTO(e.getEventType());
			LOGGER.debug("Creating notification scheme entity, type: " + e.getType().getType() + ", event: " + event.getId());
			SchemeEntity ent = new SchemeEntity(e.getType().getType(), event.getId());
			LOGGER.debug("Create notification scheme entity, type: " + ent.getType() + ", event: " + ent.getEntityTypeId());
			String param = null;
			GeneralDTO parameter = e.getParameter();
			if (parameter != null) {
				Object value = parameter.getValue();
				if (value != null) {
					if (value instanceof ApplicationUserDTO) {
						param = ((ApplicationUserDTO) value).getName();
					} else if (value instanceof CustomFieldDTO) {
						CustomFieldDTO cf = (CustomFieldDTO) value;
						CustomFieldDTO dto = cfUtil.resovleCustomField(exportStore, importStore, cf.getId());
						if (dto != null) {
							param = dto.getId();
						}
					} else if (value instanceof GroupDTO) {
						param = ((GroupDTO) value).getName();
					} else if (value instanceof ProjectRoleDTO) {
						param = ((ProjectRoleDTO) value).getName();
					} else {
						param = String.valueOf(value);
					}
				}
			}
			ent.setParameter(param);
			LOGGER.debug("Create notification scheme entity, param: " + ent.getParameter());
			entities.add(ent);
		}
		createdJira = MANAGER.createSchemeAndEntities(createdJira);
		// Associate with project
		for (ProjectDTO p : src.getProjects()) {
			ProjectDTO proj = (ProjectDTO) projectUtil.findByDTO(p);
			if (proj != null) {
				Project pj = (Project) proj.getJiraObject();
				MANAGER.removeSchemesFromProject(pj);
				MANAGER.addSchemeToProject(pj, createdJira);
			}
		}
		// Reload scheme
		createdJira = MANAGER.getSchemeObject(createdJira.getId());
		NotificationSchemeDTO dto = new NotificationSchemeDTO();
		dto.setJiraObject(createdJira);
		result.setNewDTO(dto);
		return result;
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return NotificationSchemeDTO.class;
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
		List<Scheme> list = MANAGER.getSchemeObjects();
		if (list != null) {
			for (Scheme scheme : list) {
				NotificationSchemeDTO dto = new NotificationSchemeDTO();
				dto.setJiraObject(scheme, params);
				result.put(dto.getUniqueKey(), dto);
			}
		}
		return result;
	}

}
