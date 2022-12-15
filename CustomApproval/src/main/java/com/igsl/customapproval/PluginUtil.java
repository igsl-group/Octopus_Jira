package com.igsl.customapproval;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.igsl.customapproval.delegation.DelegationUtil;

public class PluginUtil {
	
	private static final Logger LOGGER = Logger.getLogger(PluginUtil.class);
	private static final ObjectMapper OM = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	private static final UserManager USER_MANAGER = ComponentAccessor.getUserManager();
	private static final GroupManager GROUP_MANAGER = ComponentAccessor.getGroupManager();
	private static final StatusManager STATUS_MANAGER = ComponentAccessor.getComponent(StatusManager.class);
	private static final CustomFieldManager CUSTOM_FIELD_MANAGER = ComponentAccessor.getCustomFieldManager();

	// System custom field types
	public static final String SYSTEM_CUSTOM_FIELD_TYPE = "com.atlassian.jira.plugin.system.customfieldtypes:";
	public static final String CUSTOM_FIELD_TEXT_AREA = SYSTEM_CUSTOM_FIELD_TYPE + "textarea";
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
	
	/**
	 * Get admin user.
	 * @return ApplicationUser
	 */
	public static ApplicationUser getAdminUser() {
		return USER_MANAGER.getUserByKey(ADMIN_USER_KEY);
	}
	
	/**
	 * Get user object by key.
	 * @param userKey User key
	 * @return ApplicationUser
	 */
	public static ApplicationUser getUserByKey(String userKey) {
		return USER_MANAGER.getUserByKey(userKey);
	}
	
	/**
	 * Check if key is a valid user.
	 * @param key User key
	 * @return ApplicationUser, null if invalid
	 */
	public static ApplicationUser checkUserKey(String key) {
		return USER_MANAGER.getUserByKey(key);
	}

	/**
	 * Check if name is a valid group.
	 * @param groupName Group name
	 * @return Group, null if invalid
	 */
	public static Group checkGroupName(String groupName) {
		return GROUP_MANAGER.getGroup(groupName);
	}
	
	/**
	 * Check if key is a valid status.
	 * @param statusKey Status key
	 * @return Status, null if invalid
	 */
	public static Status checkStatus(String statusKey) {
		return STATUS_MANAGER.getStatus(statusKey);
	}
	
	/**
	 * Check if provided field name is a user picker field (single/multiple).
	 * @param fieldName Field name
	 * @return CustomField, null if invalid
	 */
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
	
	/**
	 * Check if provided field name is a group picker field (single/multiple).
	 * @param fieldName Field name
	 * @return CustomField, null if invalid
	 */
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
	
	/**
	 * Get list of group picker fields.
	 * @return Map<String, CustomField>. Key is field name.
	 */
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
	
	/**
	 * Get list of user picker fields.
	 * @return Map<String, CustomField>. Key is field name.
	 */
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
	
	/**
	 * Get list of Status.
	 * @return Map<Long, Status>. Key is status key.
	 */
	public static Map<Long, Status> getStatusList() {
		Map<Long, Status> result = new TreeMap<>();
		for (Status s : STATUS_MANAGER.getStatuses()) {
			result.put(s.getSequence(), s);
		}
		return result;
	}
	
	/**
	 * Retrieve ApprovalData custom field value from issue.
	 * @param issue Issue
	 * @return ApprovalData, null if not present or invalid.
	 */
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
	
	/**
	 * Get ApprovalSettings associated with issue's current status.
	 * @param issue Issue
	 * @return ApprovalSettings, null if not found
	 */
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
	 * Checks both user/group settings and return a user list containing all approvers (not delegates) for current status.
	 * @param issue Issue
	 * @return Map<String, ApplicationUser>. Key is user key. Null if no approval is found for current status.
	 */
	public static Map<String, ApplicationUser> getApproverList(Issue issue) {
		ApprovalSettings settings = getApprovalSettings(issue);
		if (settings != null) {
			return getApproverList(issue, settings);
		}
		return null;
	}
	
	/**
	 * Checks both user/group settings and return a user list containing all approvers (not delegates).
	 * @param issue Issue
	 * @param settings ApprovalSettings to choose which approval
	 * @return Map<String, ApplicationUser>. Key is user key.
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
	
	/**
	 * Calculate reject count target.
	 * @param settings ApprovalSettings
	 * @param approverList Approver list from getApproverList()
	 * @return double
	 */
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
	
	/**
	 * Calculate approve count target.
	 * @param settings ApprovalSettings
	 * @param approverList Approver list from getApproverList()
	 * @return double
	 */
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
	
	/**
	 * Check if user is an approver for the issue's current status.
	 * @param ApplicationUser User to check
	 * @param Issue issue
	 * @param approverList From getApproverList().
	 * @return boolean
	 */
	public static boolean isApprover(ApplicationUser user, Issue issue) {
		Map<String, ApplicationUser> approvers = getApproverList(issue);
		return isApprover(user.getKey(), approvers);
	}
	
	/**
	 * Check if user is an approver.
	 * @param userKey User key.
	 * @param approverList From getApproverList().
	 * @return boolean
	 */
	public static boolean isApprover(String userKey, Map<String, ApplicationUser> approverList) {
		return approverList.containsKey(userKey);
	}
	
	/**
	 * Check if user is a delegate of an approver for the issue's current status.
	 * @param ApplicationUser User to check
	 * @param Issue issue
	 * @param approverList From getApproverList
	 * @return List<ApplicationUser> Delegating approver users found
	 */
	public static List<ApplicationUser> isDelegate(ApplicationUser user, Issue issue) {
		Map<String, ApplicationUser> approvers = getApproverList(issue);
		return isDelegate(user.getKey(), approvers);
	}
	
	/**
	 * Check if user is a delegate of an approver as of today.
	 * @param userKey User key to check
	 * @param approverList From getApproverList
	 * @return List<ApplicationUser> Delegating approver users found
	 */
	public static List<ApplicationUser> isDelegate(String userKey, Map<String, ApplicationUser> approverList) {
		return isDelegate(userKey, approverList, null);
	}
	/**
	 * Check if user is a delegate of an approver.
	 * @param userKey User key to check
	 * @param approverList From getApproverList
	 * @param approvalDate Approval date. If null, defaults to today
	 * @return List<ApplicationUser> Delegating approver users found
	 */
	public static List<ApplicationUser> isDelegate(
			String userKey, Map<String, ApplicationUser> approverList, Date approvalDate) {
		List<ApplicationUser> result = new ArrayList<>();
		for (Map.Entry<String, ApplicationUser> approver : approverList.entrySet()) {
			if (DelegationUtil.isDelegate(userKey, approver.getKey(), approvalDate)) {
				result.add(approver.getValue());
			}
		}
		return result;
	}
}
