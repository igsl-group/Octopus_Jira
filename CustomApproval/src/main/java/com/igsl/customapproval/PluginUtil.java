package com.igsl.customapproval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.igsl.customapproval.data.ApprovalData;
import com.igsl.customapproval.data.ApprovalSettings;

public class PluginUtil {
	
	private static final Logger LOGGER = Logger.getLogger(PluginUtil.class);
	private static final ObjectMapper OM = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	private static final UserManager USER_MANAGER = ComponentAccessor.getUserManager();
	private static final GroupManager GROUP_MANAGER = ComponentAccessor.getGroupManager();
	private static final StatusManager STATUS_MANAGER = ComponentAccessor.getComponent(StatusManager.class);
	private static final CustomFieldManager CUSTOM_FIELD_MANAGER = ComponentAccessor.getCustomFieldManager();

	// System custom field types
	public static final String SYSTEM_CUSTOM_FIELD_TYPE = "com.atlassian.jira.plugin.system.customfieldtypes:";
	public static final String CUSTOM_FIELD_READ_ONLY_TEXT = SYSTEM_CUSTOM_FIELD_TYPE + "readonlyfield";
	public static final String CUSTOM_FIELD_USER_PICKER = SYSTEM_CUSTOM_FIELD_TYPE + "userpicker";
	public static final String CUSTOM_FIELD_USER_PICKER_MULTI = SYSTEM_CUSTOM_FIELD_TYPE + "multiuserpicker";
	public static final String CUSTOM_FIELD_GROUP_PICKER = SYSTEM_CUSTOM_FIELD_TYPE + "grouppicker";
	public static final String CUSTOM_FIELD_GROUP_PICKER_MULTI = SYSTEM_CUSTOM_FIELD_TYPE + "multigrouppicker";
	
	// Plugin Key
	// Must match ${groupId}.${artifactId} in pom.xml
	public static final String PLUGIN_KEY = "com.igsl.CustomApproval";
	
	public static final String VALUE_ALLOW = "Allow";
	public static final String VALUE_DENY = "Deny";
	
	public static final String ADMIN_USER_KEY = "admin";

	public static final String CUSTOM_FIELD_NAME = "Approval Data";
	public static final String CUSTOM_FIELD_DESCRIPTION = "[Custom Approval] Approval data as JSON string";
	
	public static ApplicationUser getAdminUser() {
		return USER_MANAGER.getUserByKey(ADMIN_USER_KEY);
	}
	
	public static ApplicationUser getUserByKey(String userKey) {
		return USER_MANAGER.getUserByKey(userKey);
	}
	
	public static ApplicationUser checkUserKey(String key) {
		return USER_MANAGER.getUserByKey(key);
	}

	public static Group checkGroupName(String groupName) {
		return GROUP_MANAGER.getGroup(groupName);
	}
	
	public static Status checkStatus(String statusKey) {
		return STATUS_MANAGER.getStatus(statusKey);
	}
	
	public static CustomField checkUserField(String fieldName) {
		CustomFieldType<?, ?> userPicker = CUSTOM_FIELD_MANAGER.getCustomFieldType(CUSTOM_FIELD_USER_PICKER);
		CustomFieldType<?, ?> userPickerMulti = CUSTOM_FIELD_MANAGER.getCustomFieldType(CUSTOM_FIELD_USER_PICKER_MULTI);
		CustomField cf = CUSTOM_FIELD_MANAGER.getCustomFieldObject(fieldName);
		if (cf != null && 
			(	userPicker.equals(cf.getCustomFieldType()) || 
				userPickerMulti.equals(cf.getCustomFieldType()))) {
			return cf;
		}
		return null;
	}
	
	public static CustomField checkGroupField(String fieldName) {
		CustomFieldType<?, ?> groupPicker = CUSTOM_FIELD_MANAGER.getCustomFieldType(CUSTOM_FIELD_GROUP_PICKER);
		CustomFieldType<?, ?> groupPickerMulti = CUSTOM_FIELD_MANAGER.getCustomFieldType(CUSTOM_FIELD_GROUP_PICKER_MULTI);
		CustomField cf = CUSTOM_FIELD_MANAGER.getCustomFieldObject(fieldName); 
		if (cf != null && 
			(	groupPicker.equals(cf.getCustomFieldType()) || 
				groupPickerMulti.equals(cf.getCustomFieldType()))) {
			return cf;
		}
		return null;
	}
	
	public static Map<String, CustomField> getGroupFieldList() {
		Map<String, CustomField> result = new TreeMap<>();
		CustomFieldType<?, ?> groupPicker = CUSTOM_FIELD_MANAGER.getCustomFieldType(CUSTOM_FIELD_GROUP_PICKER);
		CustomFieldType<?, ?> groupPickerMulti = CUSTOM_FIELD_MANAGER.getCustomFieldType(CUSTOM_FIELD_GROUP_PICKER_MULTI);
		for (CustomField cf : CUSTOM_FIELD_MANAGER.getCustomFieldObjects()) {
			if (groupPicker.equals(cf.getCustomFieldType()) || 
				groupPickerMulti.equals(cf.getCustomFieldType())) {
				result.put(cf.getName(), cf);
			}
		}
		return result;
	}
	
	public static Map<String, CustomField> getUserFieldList() {
		Map<String, CustomField> result = new TreeMap<>();
		CustomFieldType<?, ?> userPicker = CUSTOM_FIELD_MANAGER.getCustomFieldType(CUSTOM_FIELD_USER_PICKER);
		CustomFieldType<?, ?> userPickerMulti = CUSTOM_FIELD_MANAGER.getCustomFieldType(CUSTOM_FIELD_USER_PICKER_MULTI);
		for (CustomField cf : CUSTOM_FIELD_MANAGER.getCustomFieldObjects()) {
			if (userPicker.equals(cf.getCustomFieldType()) || 
				userPickerMulti.equals(cf.getCustomFieldType())) {
				result.put(cf.getName(), cf);
			}
		}
		return result;
	}
	
	public static Map<Long, Status> getStatusList() {
		Map<Long, Status> result = new TreeMap<>();
		for (Status s : STATUS_MANAGER.getStatuses()) {
			result.put(s.getSequence(), s);
		}
		return result;
	}
	
	public static ApprovalData getApprovalData(Issue issue) {
		CustomField cf = PluginSetup.findCustomField();
		Object value = issue.getCustomFieldValue(cf);
		if (value == null) {
			LOGGER.debug("No approval data");
			return null;
		}
		ApprovalData data = ApprovalData.parse(String.valueOf(value));
		if (data == null) {
			LOGGER.debug("No valid approval data");
			return null;
		}
		try {
			LOGGER.debug("approval data: " + OM.writeValueAsString(data));
		} catch (Exception ex) {
			LOGGER.error("Failed to serialize approval data", ex);
		}
		return data;
	}
	
	public static ApprovalSettings getApprovalSettings(Issue issue) {
		// Check if status matches
		ApprovalData data = getApprovalData(issue);
		String currentStatusId = issue.getStatus().getId();
		LOGGER.debug("Current status: " + issue.getStatus().getName() + " = " + currentStatusId);
		ApprovalSettings settings = null;
		for (Map.Entry<String, ApprovalSettings> entry : data.getSettings().entrySet()) {
			if (entry.getValue().getStartingStatus().equals(currentStatusId)) {
				settings = entry.getValue();
				break;
			}
		}
		try {
			LOGGER.debug("Settings: " + OM.writeValueAsString(settings));
		} catch (Exception ex) {
			LOGGER.error("Failed to serialize settings", ex);
		}
		return settings;
	}
	
	/**
	 * Checks both user/group settings and return a user list containing everyone.
	 * @param issue
	 * @param settings
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, ApplicationUser> getApproverList(Issue issue, ApprovalSettings settings) {
		Map<String, ApplicationUser> result = new HashMap<>();
		if (issue != null && settings != null) {
			if (settings.getApproverUserField() != null && 
				!settings.getApproverUserField().isEmpty()) {
				CustomField cf = CUSTOM_FIELD_MANAGER.getCustomFieldObject(settings.getApproverUserField());
				if (cf != null) {
					Object o = issue.getCustomFieldValue(cf);
					if (o instanceof List) {
						for (ApplicationUser u : (List<ApplicationUser>) o) {
							result.put(u.getKey(), u);
						}
					} else if (o instanceof ApplicationUser) {
						ApplicationUser u = (ApplicationUser) o;
						result.put(u.getKey(), u);
					}
				}
			} else if (	settings.getApproverGroupField() != null && 
						!settings.getApproverGroupField().isEmpty()) {
				CustomField cf = CUSTOM_FIELD_MANAGER.getCustomFieldObject(settings.getApproverGroupField());
				if (cf != null) {
					Object o = issue.getCustomFieldValue(cf);
					if (o instanceof List) {
						List<Group> groupList = (List<Group>) o;
						for (Group grp : groupList) {
							for (ApplicationUser u : GROUP_MANAGER.getUsersInGroup(grp)) {
								result.put(u.getKey(), u);
							}
						}
					} else if (o instanceof ApplicationUser) {
						for (ApplicationUser u : GROUP_MANAGER.getUsersInGroup((Group) o)) {
							result.put(u.getKey(), u);
						}
					}
				}
			}
		}
		try {
			LOGGER.debug("Approver list: " + OM.writeValueAsString(result));
		} catch (Exception ex) {
			LOGGER.error("Failed to serialize approver list", ex);
		}
		return result;
	}
	
	public static double getRejectCountTarget(ApprovalSettings settings, Map<String, ApplicationUser> approverList) {
		double approverListSize = approverList.size();
		double rejectCountTarget = settings.getRejectCount();
		if (rejectCountTarget == 0) {
			// Everyone
			rejectCountTarget = approverListSize;
		} else if (rejectCountTarget > 0 && rejectCountTarget < 1) {
			// Percentage, round up
			rejectCountTarget = Math.ceil(rejectCountTarget * approverListSize);
		} else {
			// Cap it at approverCount
			rejectCountTarget = Math.min(approverListSize, rejectCountTarget);
		}
		return rejectCountTarget;
	}
	
	public static double getApproveCountTarget(ApprovalSettings settings, Map<String, ApplicationUser> approverList) {
		double approverListSize = approverList.size();
		double approveCountTarget = settings.getApproveCount();
		if (approveCountTarget == 0) {
			// Everyone
			approveCountTarget = approverListSize;
		} else if (approveCountTarget > 0 && approveCountTarget < 1) {
			// Percentage, round up
			approveCountTarget = Math.ceil(approveCountTarget * approverListSize);
		} else {
			// Cap it at approverCount
			approveCountTarget = Math.min(approverListSize, approveCountTarget);
		}
		return approveCountTarget;
	}
	
	public static boolean isApprover(String userKey, Map<String, ApplicationUser> approverList) {
		return approverList.containsKey(userKey);
	}
	
	public static ApplicationUser isDelegate(String userKey, Map<String, ApplicationUser> approverList) {
		// TODO Delegate
		return null;
	}
}
