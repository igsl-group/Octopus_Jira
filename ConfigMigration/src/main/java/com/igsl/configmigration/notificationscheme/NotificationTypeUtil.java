package com.igsl.configmigration.notificationscheme;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.applicationuser.ApplicationUserUtil;
import com.igsl.configmigration.customfield.CustomFieldDTO;
import com.igsl.configmigration.customfield.CustomFieldUtil;
import com.igsl.configmigration.general.GeneralDTO;
import com.igsl.configmigration.group.GroupUtil;
import com.igsl.configmigration.projectrole.ProjectRoleUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class NotificationTypeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(NotificationTypeUtil.class);
	private static NotificationTypeManager MANAGER = ComponentAccessor.getComponent(NotificationTypeManager.class);
	
	@Override
	public String getName() {
		return "Notification Type";
	}
	
	/**
	 * Special API used by NotificationSchemeEntityDTO to convert a String parameter into a proper type based on NotificationTypeDTO.
	 */
	public GeneralDTO parseParameter(String parameter, NotificationTypeDTO type) throws Exception {
		Object o = null;
		if (type != null && type.getType() != null) {
			switch (type.getType()) {
			case "Project_Role":
				ProjectRoleUtil prUtil = (ProjectRoleUtil) JiraConfigTypeRegistry.getConfigUtil(ProjectRoleUtil.class);
				o = prUtil.findByUniqueKey(parameter);
				break;
			case "Single_Email_Address":
				// Store as string
				o = parameter;
				break;
			case "Group_Dropdown":
				GroupUtil groupUtil = (GroupUtil) JiraConfigTypeRegistry.getConfigUtil(GroupUtil.class);
				o = groupUtil.findByUniqueKey(parameter);
				break;
			case "Single_User":
				ApplicationUserUtil userUtil = (ApplicationUserUtil) JiraConfigTypeRegistry.getConfigUtil(ApplicationUserUtil.class);
				o = userUtil.findByUniqueKey(parameter);
				break;
			case "User_Custom_Field_Value": 
				CustomFieldUtil userCFUtil = (CustomFieldUtil) JiraConfigTypeRegistry.getConfigUtil(CustomFieldUtil.class);
				CustomFieldDTO userCF = (CustomFieldDTO) userCFUtil.findByInternalId(parameter);
				if (userCF != null) {
					o = userCF;
				}
				break;
			case "Group_Custom_Field_Value": 
				CustomFieldUtil groupCFUtil = (CustomFieldUtil) JiraConfigTypeRegistry.getConfigUtil(CustomFieldUtil.class);
				CustomFieldDTO groupCF = (CustomFieldDTO) groupCFUtil.findByInternalId(parameter);
				if (groupCF != null) {
					o = groupCF;
				}
				break;
			default: 
				o = parameter;
				break;
			}
		}
		if (o != null) {
			GeneralDTO dto = new GeneralDTO();
			dto.setJiraObject(o);
			return dto;
		}
		return null;
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		NotificationType et = MANAGER.getSchemeType(id);
		if (et != null) {
			NotificationTypeDTO dto = new NotificationTypeDTO();
			dto.setJiraObject(et);
			return dto;
		}
		return null;
	}

	public String makeUniqueKey(String displayName, String type) {
		return displayName + " (" + type + ")";
	}
	
	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		Map<String, NotificationType> map = MANAGER.getTypes();
		if (map != null) {
			for (NotificationType nt : map.values()) {
				String uk = makeUniqueKey(nt.getDisplayName(), nt.getType());
				if (uk.equals(uniqueKey)) {
					NotificationTypeDTO dto = new NotificationTypeDTO();
					dto.setJiraObject(nt);
					return dto;
				}
			}
		}
		return null;
	}

	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		throw new Exception("Notification Type is read only");
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return NotificationTypeDTO.class;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new LinkedHashMap<>();
		Map<String, NotificationType> map = MANAGER.getTypes();
		if (map != null) {
			for (NotificationType nt : map.values()) {
				NotificationTypeDTO dto = new NotificationTypeDTO();
				dto.setJiraObject(nt);
				result.put(dto.getUniqueKey(), dto);
			}
		}
		return result;
	}

}
