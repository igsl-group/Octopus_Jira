package com.igsl.customapproval.condition;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igsl.customapproval.PluginSetup;
import com.igsl.customapproval.PluginUtil;
import com.igsl.customapproval.data.ApprovalData;
import com.igsl.customapproval.data.ApprovalHistory;
import com.igsl.customapproval.data.ApprovalSettings;

public abstract class ApprovalCondition extends AbstractWebCondition {

	private static final Logger LOGGER = Logger.getLogger(ApprovalCondition.class);
	private static final ObjectMapper OM = new ObjectMapper();
	private static final String KEY_ISSUE = "issue";
	
	/** 
	 * Check if issue's Approval Data field has value
	 * @param helper JiraHelper
	 * @return boolean
	 */
	protected final boolean isApprovalEnabled(JiraHelper helper) {
		Issue issue = getIssue(helper);
		CustomField cf = PluginSetup.findCustomField();
		Object value = issue.getCustomFieldValue(cf);
		if (value != null) {
			ApprovalData data = ApprovalData.parse(String.valueOf(value));
			if (data != null) {
				LOGGER.debug("isApprovalEnabled: true, " + data.toString());
				return true;
			}
		}
		LOGGER.debug("isApprovalEnabled: false");
		return false;
	}
	
	/**
	 * Check if user is approver or approver delegate
	 * @param user ApplicationUser to check
	 * @param helper JiraHelper
	 * @return boolean
	 */
	protected final boolean isUserApprover(ApplicationUser user, JiraHelper helper, boolean approve) {
		Issue issue = getIssue(helper);
		ApprovalData data = PluginUtil.getApprovalData(issue);
		if (data == null) {
			LOGGER.debug("No approval data");
			return false;
		}
		ApprovalSettings settings = PluginUtil.getApprovalSettings(issue);
		if (settings == null) {
			LOGGER.debug("Status not match");
			return false;
		}
		// Check if user is approver
		Map<String, ApplicationUser> userList = PluginUtil.getApproverList(issue, settings);
		try {
			String s = OM.writeValueAsString(userList);
			LOGGER.debug("approver list: " + s);
		} catch (Exception ex) {
			LOGGER.debug("approver list serialization failed", ex);
		}
		boolean userIsApprover = PluginUtil.isApprover(user.getKey(), userList);
		if (!userIsApprover) {
			userIsApprover = (PluginUtil.isDelegate(user.getKey(), userList) != null);
		}
		if (!userIsApprover) {
			LOGGER.debug("User is not approver");
			return false;
		}
		// Check if user already approved
		Map<String, ApprovalHistory> historyList = data.getHistory().get(settings.getApprovalName());
		if (historyList.containsKey(user.getKey())) {
			// Check if allow changing decision
			LOGGER.debug("User already approved");
			if (!settings.isAllowChangeDecision()) {
				// Already approved
				LOGGER.debug("Change decision not allowed");
				return false;
			} else {
				LOGGER.debug("Change decision is allowed");
				boolean approved = historyList.get(user.getKey()).getApproved();
				if (approved) {
					// Disallow approve button
					if (approve) {
						return false;
					}
				} else {
					// Disallow reject button
					if (!approve) {
						return false;
					}
				}
			}
		}		
		LOGGER.debug("User is approver");
		return true;
	}
	
	/**
	 * Get Issue from JiraHelper.
	 * @param helper JiraHelper
	 * @return Issue
	 */
	protected final Issue getIssue(JiraHelper helper) {
		Map<String, Object> params = helper.getContextParams();
		Issue issue = null;
		if (params.containsKey(KEY_ISSUE)) {
			issue = (Issue) params.get(KEY_ISSUE);
		}
		return issue;
	}
}
