package com.igsl.customapproval;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.TransitionOptions;
import com.atlassian.jira.workflow.TransitionOptions.Builder;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.query.Query;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import com.atlassian.scheduler.status.JobDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.igsl.customapproval.data.ApprovalData;
import com.igsl.customapproval.data.ApprovalHistory;
import com.igsl.customapproval.data.ApprovalSettings;
import com.igsl.customapproval.data.DelegationSetting;
import com.igsl.customapproval.delegation.DelegationUtil;
import com.igsl.customapproval.exception.InvalidApprovalException;
import com.igsl.customapproval.exception.InvalidApproverException;
import com.igsl.customapproval.exception.InvalidWorkflowException;
import com.igsl.customapproval.exception.LockException;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

public class CustomApprovalUtil {
	
	private static final Logger LOGGER = Logger.getLogger(CustomApprovalUtil.class);
	private static final ObjectMapper OM = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	private static final UserManager USER_MANAGER = ComponentAccessor.getUserManager();
	private static final GroupManager GROUP_MANAGER = ComponentAccessor.getGroupManager();
	private static final StatusManager STATUS_MANAGER = ComponentAccessor.getComponent(StatusManager.class);
	private static final CustomFieldManager CUSTOM_FIELD_MANAGER = ComponentAccessor.getCustomFieldManager();
	private static final IssueManager ISSUE_MANAGER = ComponentAccessor.getIssueManager();
	private static final JqlQueryParser JQL_PARSER = ComponentAccessor.getComponent(JqlQueryParser.class);
	private static final SearchService SEARCH_SERVICE = ComponentAccessor.getComponent(SearchService.class);
	
	// Configuration
	private static final String CONFIG_KEY = "CustomApprovalConfigData:";
	private static final String KEY_RETAIN_DAYS = CONFIG_KEY + "retainDays";
	private static final String KEY_ADMIN_GROUPS = CONFIG_KEY + "adminGroups";
	private static final String KEY_JOB_FREQUENCY = CONFIG_KEY + "jobFrequency";
	private static final String KEY_JOB_FILTER = CONFIG_KEY + "jobFilter";
	public static final long DEFAULT_RETAIN_DAYS = 365;
	public static final long DEFAULT_JOB_FREQUENCY = 300000;
	public static final String DEFAULT_ADMIN_GROUP = "jira-administrators";
	public static final String DEFAULT_JOB_FILTER = 
			"statusCategory != Done and \"" + CustomApprovalUtil.CUSTOM_FIELD_NAME + "\" is not empty";
	
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
	
	public static final String ADMIN_USER_NAME = "admin";
	
	public static final String LOCK_FIELD_NAME = "Approval Lock";
	public static final String LOCK_FIELD_DESCRIPTION = "[Custom Approval] Approval lock";
	public static final String CUSTOM_FIELD_NAME = "Approval Data";
	public static final String CUSTOM_FIELD_DESCRIPTION = "[Custom Approval] Approval data as JSON string";
	
	/**
	 * Get admin user.
	 * @return ApplicationUser
	 */
	public static ApplicationUser getAdminUser() {
		return USER_MANAGER.getUserByName(ADMIN_USER_NAME);
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
	 * @return Map. Key is field name, value is CustomField.
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
	 * @return Map. Key is field name, value is CustomField.
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
	 * @return Map. Key is status key (Long), value is Status.
	 */
	public static Map<Long, Status> getStatusList() {
		Map<Long, Status> result = new TreeMap<>();
		for (Status s : STATUS_MANAGER.getStatuses()) {
			result.put(s.getSequence(), s);
		}
		return result;
	}
	
	/**
	 * Lock issue for custom approval.
	 * Always use a try...finally block to unlock afterwards. 
	 * @param issue Issue
	 * @return Lock Id. Use this value to unlock. If null, locking failed (someone else is editing).
	 */
	public static String lockApproval(MutableIssue issue) {
		ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		UUID newId = UUID.randomUUID();
		CustomField cf = CustomApprovalSetup.getApprovalLockCustomField();
		Object o = issue.getCustomFieldValue(cf);
		if (o == null) {
			issue.setCustomFieldValue(cf, newId.toString());
			ISSUE_MANAGER.updateIssue(currentUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false);
			return newId.toString();
		}
		return null;
	}
	
	/**
	 * Unlock issue for custom approval.
	 * @param issue Issue
	 * @param lockId String, lock Id returned by lockApproval().
	 * @return boolean true if unlocking is successful.
	 */
	public static boolean unlockApproval(MutableIssue issue, String lockId) {
		ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		CustomField cf = CustomApprovalSetup.getApprovalLockCustomField();
		Object o = issue.getCustomFieldValue(cf);
		if (o != null && String.valueOf(o).equals(lockId)) {
			issue.setCustomFieldValue(cf, null);
			ISSUE_MANAGER.updateIssue(currentUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false);
			return true;
		}
		return false;
	}
	
	/**
	 * Retrieve ApprovalData custom field value from issue.
	 * @param issue Issue
	 * @return ApprovalData, null if not present or invalid.
	 */
	public static ApprovalData getApprovalData(Issue issue) {
		CustomField cf = CustomApprovalSetup.getApprovalDataCustomField();
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
	 * Update ApprovalData directly.
	 * Since approval history is stored in ApprovalData, you can alter them as well.
	 * You should update status accordingly. 
	 * Use with caution.
	 * 
	 * @param user Updating user.
	 * @param issue Issue
	 * @param data ApprovalData from getApprovalData().
	 */
	public static void setApprovalData(ApplicationUser user, MutableIssue issue, ApprovalData data) {
		CustomField cf = CustomApprovalSetup.getApprovalDataCustomField();
		issue.setCustomFieldValue(cf, data);
		ISSUE_MANAGER.updateIssue(user, issue, EventDispatchOption.DO_NOT_DISPATCH, false);
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
	 * @return Map. Key is user key, value is ApplicationUser. Null if no approval is found for current status.
	 */
	public static Map<String, ApplicationUser> getApproverList(Issue issue) {
		ApprovalSettings settings = getApprovalSettings(issue);
		if (settings != null) {
			return getApproverList(issue, settings);
		}
		return null;
	}
	
	/**
	 * Get list of delegates for provided approver list.
	 * @param approverList From getApproverList().
	 * @return Map. Key is user key, value is ApplicationUser.
	 */
	public static Map<String, ApplicationUser> getDelegates(Map<String, ApplicationUser> approverList) {
		Map<String, ApplicationUser> result = new HashMap<>();
		if (approverList != null) {
			for (String approverKey : approverList.keySet()) {
				List<DelegationSetting> delegateList = DelegationUtil.loadData(approverKey, false);
				for (DelegationSetting setting : delegateList) {
					ApplicationUser delegate = setting.getDelegateToUserObject();
					result.put(delegate.getKey(), delegate);
				}
			}
		}
		return result;
	}
	
	/**
	 * Checks both user/group settings and return a user list containing all approvers (not delegates).
	 * @param issue Issue
	 * @param settings ApprovalSettings to choose which approval
	 * @return Map. Key is user key, value is ApplicationUser.
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
	 * @param user ApplicationUser to check
	 * @param issue Issue
	 * @return boolean
	 */
	public static boolean isApprover(ApplicationUser user, Issue issue) {
		Map<String, ApplicationUser> approvers = getApproverList(issue);
		return isApprover(user.getKey(), approvers);
	}
	
	/**
	 * Check if user is an approver.
	 * @param userKey User key.
	 * @param approverList List of String (user key)
	 * @return boolean
	 */
	public static boolean isApprover(String userKey, List<String> approverList) {
		return approverList.contains(userKey);
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
	 * Check if user is currently a delegate of an approver for the issue's current status.
	 * @param user ApplicationUser to check
	 * @param issue Issue
	 * @return List of ApplicationUser. Delegating approver users found
	 */
	public static List<ApplicationUser> isDelegate(ApplicationUser user, Issue issue) {
		Map<String, ApplicationUser> approvers = getApproverList(issue);
		return isDelegate(user.getKey(), approvers);
	}
	
	/**
	 * Check if user is a delegate of an approver as of today.
	 * @param userKey User key to check
	 * @param approverList From getApproverList
	 * @return List of ApplicationUser. Delegating approver users found
	 */
	public static List<ApplicationUser> isDelegate(String userKey, Map<String, ApplicationUser> approverList) {
		return isDelegate(userKey, approverList, null);
	}
	/**
	 * Check if user is a delegate of an approver.
	 * @param userKey User key to check
	 * @param approverList From getApproverList
	 * @param approvalDate Approval date. If null, defaults to today
	 * @return List of ApplicationUser. Delegating approver users found
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
	
	private static String getActionTarget(JiraWorkflow wf, ActionDescriptor actionDesc) {
		int targetStepId = actionDesc.getUnconditionalResult().getStep();
		StepDescriptor targetStepDesc = wf.getDescriptor().getStep(targetStepId);
		Status linkedStatus = wf.getLinkedStatus(targetStepDesc);
		return linkedStatus.getId();
	}
	
	/**
	 * Approve/reject current approval. Transit status is condition is matched.
	 * @param issue Issue
	 * @param user ApplicationUser performing the action.
	 * @param approve boolean, true to approve, false to reject.
	 * @throws InvalidApprovalException when ApprovalSettings is not valid
	 * @throws LockException when unable to lock issue
	 * @throws InvalidApproverException when provided user is not approver/delegate
	 * @throws InvalidWorkflowException when unable to locate transition for approval
	 * @throws WorkflowException when unable to transit issue
	 */
	public static void approve(MutableIssue issue, ApplicationUser user, boolean approve) 
			throws 
				LockException, 
				InvalidApproverException, 
				InvalidApprovalException, 
				InvalidWorkflowException, 
				WorkflowException {
		ApprovalSettings settings = getApprovalSettings(issue);
		approve(issue, settings, user, approve);
	}
	
	/**
	 * Approve/reject a specific approval. Transit status is condition is matched.
	 * @param issue Issue
	 * @param settings ApprovalSettings of a specific approval.
	 * @param user ApplicationUser performing the action.
	 * @param approve boolean, true to approve, false to reject.
	 * @return boolean True if issue is transited.
	 * @throws InvalidApprovalException when ApprovalSettings is not valid
	 * @throws LockException when unable to lock issue
	 * @throws InvalidApproverException when provided user is not approver/delegate
	 * @throws InvalidWorkflowException when unable to locate transition for approval
	 * @throws WorkflowException when unable to transit issue
	 */
	public static boolean approve(MutableIssue issue, ApprovalSettings settings, ApplicationUser user, boolean approve) 
			throws 
				LockException, 
				InvalidApproverException, 
				InvalidApprovalException, 
				InvalidWorkflowException, 
				WorkflowException {
		if (user == null) {
			throw new InvalidApproverException();
		}
		if (settings == null) {
			throw new InvalidApprovalException();
		}
		CustomField approvalDataCustomField = CustomApprovalSetup.getApprovalDataCustomField();
		String lockId = null;
		try {
			lockId = CustomApprovalUtil.lockApproval(issue);
			if (lockId == null) {
				throw new LockException();
			}
			// Validate if user is approver
			Map<String, ApplicationUser> approverList = getApproverList(issue, settings);
			LOGGER.debug("Approvers: ");
			for (String s : approverList.keySet()) {
				LOGGER.debug(s);
			}
			LOGGER.debug("User: " + user.getKey());
			boolean isApprover = CustomApprovalUtil.isApprover(user.getKey(), approverList);
			// Check is anyone's delegate
			List<ApplicationUser> delegators = CustomApprovalUtil.isDelegate(user.getKey(), approverList);
			boolean isDelegated = (delegators != null && delegators.size() != 0);
			List<String> delegatorList = null;
			if (isDelegated) {
				delegatorList = new ArrayList<>();
				for (ApplicationUser u : delegators) {
					delegatorList.add(u.getKey());
				}
			}
			if (!isApprover && !isDelegated) {
				throw new InvalidApproverException();
			}
			ApprovalData approvalData = getApprovalData(issue); 
			// Update ApprovalHistory
			Map<String, ApprovalHistory> historyList;
			if (approvalData.getHistory().containsKey(settings.getApprovalName())) {
				historyList = approvalData.getHistory().get(settings.getApprovalName());
			} else {
				historyList = new LinkedHashMap<String, ApprovalHistory>();
				approvalData.getHistory().put(settings.getApprovalName(), historyList);
			}
			if (isApprover) {
				// For user
				if (historyList.containsKey(user.getKey())) {
					// Already approved, update decision
					ApprovalHistory historyItem = historyList.get(user.getKey());
					historyItem.setApprovedDate(new Date());
					historyItem.setApproved(approve);
					historyItem.setDelegated(null);
					// Remove and add to put item at the bottom of the list
					historyList.remove(user.getKey());
					historyList.put(user.getKey(), historyItem);
				} else {
					// Add new record
					ApprovalHistory historyItem = new ApprovalHistory();
					historyItem.setApprover(user.getKey());
					historyItem.setApprovedDate(new Date());
					historyItem.setApproved(approve);
					historyItem.setDelegated(null);
					historyList.put(user.getKey(), historyItem);
				}
			}
			if (delegatorList != null) {
				// Set decision for each delegator
				for (String delegator : delegatorList) {
					if (historyList.containsKey(delegator)) {
						// Already approved, update decision
						ApprovalHistory historyItem = historyList.get(delegator);
						historyItem.setApprovedDate(new Date());
						historyItem.setApproved(approve);
						historyItem.setDelegated(user.getKey());
						// Remove and add to put item at the bottom of the list
						historyList.remove(delegator);
						historyList.put(delegator, historyItem);
					} else {
						// Add new record
						ApprovalHistory historyItem = new ApprovalHistory();
						historyItem.setApprover(delegator);
						historyItem.setApprovedDate(new Date());
						historyItem.setApproved(approve);
						historyItem.setDelegated(user.getKey());
						historyList.put(delegator, historyItem);
					}
				}
			}
			// Save ApprovalData
			issue.setCustomFieldValue(approvalDataCustomField, approvalData.toString());
			ISSUE_MANAGER.updateIssue(user, issue, EventDispatchOption.DO_NOT_DISPATCH, false);
			return transitIssueWithoutLock(issue, user);
		} finally {
			if (lockId != null) {
				CustomApprovalUtil.unlockApproval(issue, lockId);
			}
		}
	}
	
	/**
	 * Check if approval condition has been completed and transit issue.
	 * @param issue Issue
	 * @param user User performing the action
	 * @return boolean True if issue has been transited
	 */
	public static boolean transitIssue(MutableIssue issue, ApplicationUser user)
			throws LockException, InvalidWorkflowException, WorkflowException {
//		String lockId = null;
//		try {
//			lockId = CustomApprovalUtil.lockApproval(issue);
//			if (lockId == null) {
//				throw new LockException();
//			}
			return transitIssueWithoutLock(issue, user);
//		} finally {
//			if (lockId != null) {
//				CustomApprovalUtil.unlockApproval(issue, lockId);
//			}
//		}
	}
	
	private static boolean transitIssueWithoutLock(MutableIssue issue, ApplicationUser user)
			throws LockException, InvalidWorkflowException, WorkflowException {
		ApprovalData approvalData = getApprovalData(issue);
		ApprovalSettings approvalSettings = getApprovalSettings(issue);
		if (approvalSettings != null) {
			Map<String, ApplicationUser> approverList = getApproverList(issue, approvalSettings);
			Map<String, ApprovalHistory> historyList = approvalData.getHistory().get(approvalSettings.getApprovalName());
			Integer approveAction = null;
			Integer rejectAction = null;
			WorkflowManager wfMan = ComponentAccessor.getWorkflowManager();
			JiraWorkflow wf = wfMan.getWorkflow(issue);
			if (wf != null) {
				List<?> actions = wf.getLinkedStep(issue.getStatus()).getActions();
				for (Object a : actions) {
					ActionDescriptor desc = (ActionDescriptor) a;
					String targetStatus = getActionTarget(wf, desc);
					if (approvalSettings.getApprovedStatus().equals(targetStatus)) {
						approveAction = desc.getId();
						LOGGER.debug("Approve action found: " + approveAction);
					} else if (approvalSettings.getRejectedStatus().equals(targetStatus)) {
						rejectAction = desc.getId();
						LOGGER.debug("Reject action found: " + rejectAction);
					}
				}
			} else {
				throw new InvalidWorkflowException("Workflow cannot be found for issue " + issue.getKey());
			}
			if (approveAction == null) {
				throw new InvalidWorkflowException("Approve action cannot be found in workflow");
			}
			if (rejectAction == null) {
				throw new InvalidWorkflowException("Reject action cannot be found in workflow");
			}
			// Check approval criteria, transit issue if met
			double approveCount = 0;
			double rejectCount = 0;
			// Find history where the user or on behalf of user is still an approver
			for (ApprovalHistory historyItem : historyList.values()) {
				boolean isApprover = CustomApprovalUtil.isApprover(historyItem.getApprover(), approverList);
				if (!isApprover) {
					isApprover = (CustomApprovalUtil.isDelegate(historyItem.getApprover(), approverList) != null);
				}
				if (isApprover) {
					if (historyItem.getApproved()) {
						approveCount++;
					} else {
						rejectCount++;
					}
				}
			}
			LOGGER.debug("Current count, approve: " + approveCount + " reject: " + rejectCount);
			// Get target counts
			double approveCountTarget = CustomApprovalUtil.getApproveCountTarget(approvalSettings, approverList);
			double rejectCountTarget = CustomApprovalUtil.getRejectCountTarget(approvalSettings, approverList);
			LOGGER.debug("Target count, approve: " + approveCountTarget + " reject: " + rejectCountTarget);
			Integer targetAction = null;
			boolean approved = false;
			if (rejectCountTarget <= rejectCount) {
				targetAction = rejectAction;
				approved = false;
			} else if (approveCountTarget <= approveCount) {
				targetAction = approveAction;
				approved = true;
			}
			if (targetAction != null) {
				// Update settings to mark approve as completed
				ApprovalSettings as = approvalData.getSettings().get(approvalSettings.getApprovalName());
				as.setCompleted(true);
				as.setApproved(approved);
				as.setFinalApproveCount(approveCount);
				as.setFinalRejectCount(rejectCount);
				as.setFinalApproveCountTarget(approveCountTarget);
				as.setFinalRejectCountTarget(rejectCountTarget);
				as.getFinalApproverList().addAll(approverList.keySet());
				issue.setCustomFieldValue(CustomApprovalSetup.getApprovalDataCustomField(), approvalData.toString());
				LOGGER.debug("Locking in approval: " + as.getApprovalName() + ": " + approvalData.toString());
				ISSUE_MANAGER.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, false);
				
				TransitionOptions.Builder builder = new Builder();
				// There should be a hide from user condition on the transition, so need to skip condition
				builder.skipConditions();
				IssueService iService = ComponentAccessor.getIssueService();
				TransitionValidationResult tvr = iService.validateTransition(
						user, 
						issue.getId(), 
						targetAction, 
						iService.newIssueInputParameters(), 
						builder.build());
				if (tvr.isValid()) {
					IssueResult ir = iService.transition(user, tvr);
					if (!ir.isValid()) {
						StringBuilder sb = new StringBuilder();
						for (String s : ir.getErrorCollection().getErrorMessages()) {
							sb.append(s).append("; ");
						}
						throw new WorkflowException(sb.toString());
					}
				} else {
					StringBuilder sb = new StringBuilder();
					for (String s : tvr.getErrorCollection().getErrorMessages()) {
						sb.append(s).append("; ");
					}
					throw new WorkflowException(sb.toString());
				}
				LOGGER.debug("Issue " + issue.getKey() + " transited");
				return true;
			}
		}
		return false;
	}
	
	public static long getDelegationHistoryRetainDays() {
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		try {
			Object o = settings.get(KEY_RETAIN_DAYS);
			if (o != null) {
				return Long.parseLong(String.valueOf(o));
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to read delegation history retain period", ex);
		}
		return DEFAULT_RETAIN_DAYS;
	}
	
	public static void setJobFrequency(long frequency) {
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		settings.put(KEY_JOB_FREQUENCY, Long.toString(frequency));
	}
	
	public static long getJobFrequency() {
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		try {
			Object o = settings.get(KEY_JOB_FREQUENCY);
			if (o != null) {
				return Long.parseLong(String.valueOf(o));
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to read delegation job frequency", ex);
		}
		return DEFAULT_JOB_FREQUENCY;
	}
	
	/**
	 * Update job filter JQL.
	 * @param filter JQL.
	 * @return List of String containing issue keys found.
	 * @throws Exception If filter is not valid JQL.
	 */
	public static List<String> setJobFilter(String filter) throws Exception {
		List<String> result = null;
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		try {
			Query q = JQL_PARSER.parseQuery(filter);
			settings.put(KEY_JOB_FILTER, filter);
			// Execute the query
			SearchResults<Issue> list = SEARCH_SERVICE.search(getAdminUser(), q, PagerFilter.getUnlimitedFilter());
			if (list.getResults() != null) {
				result = new ArrayList<>();
				for (Issue issue : list.getResults()) {
					result.add(issue.getKey());
				}
			}
		} catch (Exception e) {
			throw new Exception("Filter is not valid: " + filter, e);
		}
		return result;
	}
	
	public static String getJobFilter() {
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		Object o = settings.get(KEY_JOB_FILTER);
		if (o != null) {
			return String.valueOf(o);
		}
		return DEFAULT_JOB_FILTER;
	}
	
	public static void setDelegationHistoryRetainDays(long days) throws Exception {
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		settings.put(KEY_RETAIN_DAYS, Long.toString(days));
	}
	
	
	public static List<String> getDelegationAdminGroups() {
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		try {
			return OM.readValue(String.valueOf(settings.get(KEY_ADMIN_GROUPS)), 
					new TypeReference<List<String>>() {});
		} catch (Exception ex) {
			LOGGER.error("Failed to read delegation admin groups", ex);
		}
		List<String> list = new ArrayList<>();
		Group adminGroup = checkGroupName(DEFAULT_ADMIN_GROUP);
		if (adminGroup != null) {
			list.add(adminGroup.getName());
		}
		return list;
	}
	
	public static void setDelegationAdminGroups(List<String> groups) throws Exception {
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		settings.put(KEY_ADMIN_GROUPS, OM.writeValueAsString(groups));
	}
	
	/**
	 * Create/replace schedule job to scan issue for custom approval that hasn't transited
	 * Such issues are caused by changing approver definition.
	 * @param frequency Job frequency in milliseconds. 0 to disable.
	 * @return boolean True if successful
	 */
	public static boolean createScheduledJob(long frequency) {
		SchedulerService schedulerService = ComponentAccessor.getComponent(SchedulerService.class);
		JobRunnerKey key = JobRunnerKey.of(CustomApprovalScheduleJob.class.getCanonicalName());
		// Unregister
		List<JobDetails> jobList = schedulerService.getJobsByJobRunnerKey(key);
		if (jobList != null) { 
			for (JobDetails job : jobList) {
				schedulerService.unscheduleJob(job.getJobId());
			}
		}
		schedulerService.unregisterJobRunner(key);
		if (frequency > 0) {
			// Register
			schedulerService.registerJobRunner(key, new CustomApprovalScheduleJob());
			Schedule schedule = Schedule.forInterval(frequency, null);
			JobConfig jobConfig = JobConfig
					.forJobRunnerKey(key)
					.withSchedule(schedule)
					.withRunMode(RunMode.RUN_ONCE_PER_CLUSTER);
			JobId jobId = JobId.of(CustomApprovalScheduleJob.class.getCanonicalName());
			try {
				schedulerService.scheduleJob(jobId, jobConfig);
			} catch (SchedulerServiceException e) {
				LOGGER.error("Failed to create schedule job", e);
				return false;
			}
		}
		return true;
	}
}
