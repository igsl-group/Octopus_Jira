package com.igsl.configmigration.permissionscheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.plugin.schema.spi.Schema;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.applicationrole.ApplicationRoleDTO;
import com.igsl.configmigration.applicationuser.ApplicationUserDTO;
import com.igsl.configmigration.customfield.CustomFieldDTO;
import com.igsl.configmigration.customfield.CustomFieldUtil;
import com.igsl.configmigration.eventtype.EventTypeDTO;
import com.igsl.configmigration.general.GeneralDTO;
import com.igsl.configmigration.group.GroupDTO;
import com.igsl.configmigration.notificationscheme.NotificationSchemeDTO;
import com.igsl.configmigration.notificationscheme.NotificationSchemeEntityDTO;
import com.igsl.configmigration.project.ProjectDTO;
import com.igsl.configmigration.project.ProjectUtil;
import com.igsl.configmigration.projectrole.ProjectRoleDTO;
import com.igsl.configmigration.projectrole.ProjectRoleUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class PermissionSchemeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(PermissionSchemeUtil.class);
	private static final PermissionSchemeManager MANAGER = ComponentAccessor.getPermissionSchemeManager();
	
	@Override
	public String getName() {
		return "Permission Scheme";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		Scheme scheme = MANAGER.getSchemeObject(idAsLong);
		if (scheme != null) {
			PermissionSchemeDTO dto = new PermissionSchemeDTO();
			dto.setJiraObject(scheme);
			return dto;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		Scheme scheme = MANAGER.getSchemeObject(uniqueKey);
		if (scheme != null) {
			PermissionSchemeDTO dto = new PermissionSchemeDTO();
			dto.setJiraObject(scheme);
			return dto;
		}
		return null;
	}

	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		ProjectUtil projectUtil = (ProjectUtil) JiraConfigTypeRegistry.getConfigUtil(ProjectUtil.class);
		CustomFieldUtil cfUtil = (CustomFieldUtil) JiraConfigTypeRegistry.getConfigUtil(CustomFieldUtil.class);
		ProjectRoleUtil projectRoleUtil = 
				(ProjectRoleUtil) JiraConfigTypeRegistry.getConfigUtil(ProjectRoleUtil.class);
		PermissionSchemeDTO original = null;
		if (oldItem != null) {
			original = (PermissionSchemeDTO) oldItem;
		} else {
			original = (PermissionSchemeDTO) findByUniqueKey(newItem.getUniqueKey());
		}
		PermissionSchemeDTO src = (PermissionSchemeDTO) newItem;
		// Create entities
		Collection<SchemeEntity> entities = new ArrayList<>();
		for (PermissionSchemeEntityDTO e : src.getEntities()) {
			SchemeEntity ent = new SchemeEntity(e.getType(), e.getEntityTypeId().getUniqueKey());
			// Remap parameter based on type
			String param = null;
			GeneralDTO parameter = e.getParameter();
			if (parameter != null) {
				Object value = parameter.getValue();
				if (value != null) {
					if (value instanceof ApplicationRoleDTO) {
						param = ((ApplicationRoleDTO) value).getUniqueKey();
					} else if (value instanceof ApplicationUserDTO) {
						param = ((ApplicationUserDTO) value).getUniqueKey();
					} else if (value instanceof CustomFieldDTO) {
						CustomFieldDTO cf = (CustomFieldDTO) value;
						CustomFieldDTO dto = cfUtil.resovleCustomField(exportStore, importStore, cf.getId());
						if (dto != null) {
							param = dto.getId();
						}
					} else if (value instanceof GroupDTO) {
						param = ((GroupDTO) value).getUniqueKey();
					} else if (value instanceof ProjectRoleDTO) {
						ProjectRoleDTO dto = (ProjectRoleDTO) 
								projectRoleUtil.findByUniqueKey(((ProjectRoleDTO) value).getUniqueKey());
						if (dto != null) {
							param = dto.getInternalId();
						}
					} else {
						param = String.valueOf(value);
					}
				}
			}
			ent.setParameter(param);
			entities.add(ent);
		}
		Scheme createdJira = null;
		if (original != null) {
			// Delete entities
			List<Long> oldEntities = new ArrayList<>();
			for (PermissionSchemeEntityDTO dto : original.getEntities()) {
				oldEntities.add(dto.getId());
			}
			MANAGER.deleteEntities(oldEntities);
			// Update scheme. entities is ignored in MANAGER.updateScheme
			createdJira = new Scheme(original.getId(), src.getType(), src.getName(), src.getDescription(), entities);	
			MANAGER.updateScheme(createdJira);
			// Recreate entities
			for (SchemeEntity entity : entities) {
				entity.setSchemeId(createdJira.getId()); 
				@SuppressWarnings("deprecation")
				GenericValue gv = MANAGER.getScheme(createdJira.getId());
				GenericValue createdEntity = MANAGER.createSchemeEntity(gv, entity);
				if (createdEntity == null) {
					result.addWarning("Unable to create entity, type: " + entity.getType() + ", parameter: " + entity.getParameter());
				}
			}
		} else {
			// Create
			createdJira = new Scheme(null, src.getType(), src.getName(), src.getDescription(), entities);
			createdJira = MANAGER.createSchemeAndEntities(createdJira);
		}
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
		PermissionSchemeDTO dto = new PermissionSchemeDTO();
		dto.setJiraObject(createdJira);
		result.setNewDTO(dto);
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return PermissionSchemeDTO.class;
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
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (Scheme scheme : MANAGER.getSchemeObjects()) {
			PermissionSchemeDTO dto = new PermissionSchemeDTO();
			dto.setJiraObject(scheme);
			result.put(dto.getUniqueKey(), dto);
		}
		return result;
	}

}
