package com.igsl.configmigration.permissionscheme;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.scheme.SchemeEntity;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.applicationrole.ApplicationRoleDTO;
import com.igsl.configmigration.applicationrole.ApplicationRoleUtil;
import com.igsl.configmigration.applicationuser.ApplicationUserDTO;
import com.igsl.configmigration.applicationuser.ApplicationUserUtil;
import com.igsl.configmigration.customfield.CustomFieldDTO;
import com.igsl.configmigration.customfield.CustomFieldUtil;
import com.igsl.configmigration.general.GeneralDTO;
import com.igsl.configmigration.general.GeneralUtil;
import com.igsl.configmigration.group.GroupDTO;
import com.igsl.configmigration.group.GroupUtil;
import com.igsl.configmigration.projectrole.ProjectRoleDTO;
import com.igsl.configmigration.projectrole.ProjectRoleUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class PermissionSchemeEntityDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(PermissionSchemeEntityDTO.class);
	
	// TODO Types defined in permission-types.xml. Parse from file?
	public static final String TYPE_APPLICATION_ROLE = "applicationRole";
	public static final String TYPE_GROUP = "group";
	public static final String TYPE_USER = "user";
	public static final String TYPE_USER_CUSTOM_FIELD = "userCF";
	public static final String TYPE_PROJECT_ROLE = "projectrole";
	public static final String TYPE_GROUP_CUSTOM_FIELD = "groupCF";
	
	private Long id;
	private ProjectPermissionKeyDTO entityTypeId;
	private GeneralDTO templateId;
	private String type;
	private GeneralDTO parameter;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		SchemeEntity e = (SchemeEntity) o;
		this.id = e.getId();
		if (e.getEntityTypeId() != null) {
			this.entityTypeId = new ProjectPermissionKeyDTO();
			this.entityTypeId.setJiraObject(e.getEntityTypeId());
		}
		if (e.getTemplateId() != null) {
			this.templateId = new GeneralDTO();
			this.templateId.setJiraObject(e.getTemplateId());
		}
		this.type = e.getType();
		// Parse parameter based on type
		if (e.getParameter() != null) {
			LOGGER.debug("Parameter: " + e.getParameter());
			Object param = null;
			switch (this.type) {
			case TYPE_APPLICATION_ROLE: {
				ApplicationRoleUtil util = 
						(ApplicationRoleUtil) JiraConfigTypeRegistry.getConfigUtil(ApplicationRoleUtil.class);
				ApplicationRoleDTO dto = (ApplicationRoleDTO) util.findByUniqueKey(e.getParameter());
				LOGGER.debug("Application Role: " + dto);
				param = dto;
				break;
			}
			case TYPE_GROUP: {
				GroupUtil util = (GroupUtil) JiraConfigTypeRegistry.getConfigUtil(GroupUtil.class);
				GroupDTO dto = (GroupDTO) util.findByUniqueKey(e.getParameter());
				LOGGER.debug("Group: " + dto);
				param = dto;
				break;
			}
			case TYPE_USER: {
				ApplicationUserUtil util = (ApplicationUserUtil) 
						JiraConfigTypeRegistry.getConfigUtil(ApplicationUserUtil.class);
				ApplicationUserDTO dto = (ApplicationUserDTO) util.findByUniqueKey(e.getParameter());
				LOGGER.debug("User: " + dto);
				param = dto;
				break;
			}
			case TYPE_USER_CUSTOM_FIELD: {
				CustomFieldUtil util = (CustomFieldUtil) 
						JiraConfigTypeRegistry.getConfigUtil(CustomFieldUtil.class);
				CustomFieldDTO dto = (CustomFieldDTO) util.findByInternalId(e.getParameter());
				LOGGER.debug("User Custom Field: " + dto);
				param = dto;
				break;
			}
			case TYPE_PROJECT_ROLE: {
				ProjectRoleUtil util = (ProjectRoleUtil) 
						JiraConfigTypeRegistry.getConfigUtil(ProjectRoleUtil.class);
				ProjectRoleDTO dto = (ProjectRoleDTO) util.findByInternalId(e.getParameter());
				LOGGER.debug("Project Role: " + dto);
				param = dto;
				break;
			}
			case TYPE_GROUP_CUSTOM_FIELD: {
				CustomFieldUtil util = (CustomFieldUtil) 
						JiraConfigTypeRegistry.getConfigUtil(CustomFieldUtil.class);
				CustomFieldDTO dto = (CustomFieldDTO) util.findByInternalId(e.getParameter());
				LOGGER.debug("Group Custom Field: " + dto);
				param = dto;
				break;
			}
			default:
				// Treat as string
				param = e.getParameter();
				LOGGER.debug("String: " + param);
				break;
			}
			if (param != null) {
				this.parameter = new GeneralDTO();
				this.parameter.setJiraObject(param);
			}
		}
		this.uniqueKey = Long.toString(this.id);
	}

	@Override
	public String getConfigName() {
		StringBuilder sb = new StringBuilder();
		sb	.append(this.entityTypeId.getConfigName() + " (")
			.append(this.type);
		if (this.parameter != null) {
			sb.append(": ").append(this.parameter.getConfigName());
		}
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Entity Type", new JiraConfigProperty(ProjectPermissionKeyUtil.class, this.entityTypeId));
		r.put("Template ID", new JiraConfigProperty(GeneralUtil.class, this.templateId));
		r.put("Type", new JiraConfigProperty(this.type));
		r.put("Parameter", new JiraConfigProperty(GeneralUtil.class, this.parameter));
		return r;
	}
	
	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getKey",
				"getPluginVersion",
				"getPluginInformation",
				"getPluginState");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return PermissionSchemeEntityUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return SchemeEntity.class;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public GeneralDTO getParameter() {
		return parameter;
	}

	public void setParameter(GeneralDTO parameter) {
		this.parameter = parameter;
	}

	public ProjectPermissionKeyDTO getEntityTypeId() {
		return entityTypeId;
	}

	public void setEntityTypeId(ProjectPermissionKeyDTO entityTypeId) {
		this.entityTypeId = entityTypeId;
	}

	public GeneralDTO getTemplateId() {
		return templateId;
	}

	public void setTemplateId(GeneralDTO templateId) {
		this.templateId = templateId;
	}

}
