package com.igsl.customapproval.condition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igsl.customapproval.CustomApprovalSetup;
import com.igsl.customapproval.CustomApprovalUtil;
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
		CustomField cf = CustomApprovalSetup.getApprovalDataCustomField();
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
		ApprovalData data = CustomApprovalUtil.getApprovalData(issue);
		if (data == null) {
			LOGGER.debug("No approval data");
			return false;
		}
		ApprovalSettings settings = CustomApprovalUtil.getApprovalSettings(issue);
		if (settings == null) {
			LOGGER.debug("Status not match");
			return false;
		}
		// Check if user is approver
		Map<String, ApplicationUser> userList = CustomApprovalUtil.getApproverList(issue, settings);
		try {
			String s = OM.writeValueAsString(userList);
			LOGGER.debug("approver list: " + s);
		} catch (Exception ex) {
			LOGGER.debug("approver list serialization failed", ex);
		}
		boolean userIsApprover = CustomApprovalUtil.isApprover(user.getKey(), userList);
		LOGGER.debug(user.getKey() + " isApprover: " + userIsApprover);
		boolean userIsDelegated = false;
		List<ApplicationUser> delegators = CustomApprovalUtil.isDelegate(user.getKey(), userList);
		userIsDelegated = (delegators != null && delegators.size() != 0);
		LOGGER.debug(user.getKey() + " isDelegated: " + userIsDelegated);
		if (!userIsApprover && !userIsDelegated) {
			LOGGER.debug("User is not approver");
			return false;
		}
		// Check if user already approved
		Map<String, ApprovalHistory> historyList = data.getHistory().get(settings.getApprovalName());
		if (historyList != null) {
			if (userIsDelegated) {
				// Check as delegates
				// If any delegator cannot be found, allow both approve/reject buttons
				boolean alreadyApproved = true;
				for (ApplicationUser delegator : delegators) {
					if (!historyList.containsKey(delegator.getKey())) {
						alreadyApproved &= false;
					}
				}
				if (alreadyApproved) {
					if (!settings.isAllowChangeDecision()) {
						LOGGER.debug("Already approved as delegate, change decision not allowed");
						return false;
					} else {
						// We can assume all decisions are the same
						boolean approved = historyList.get(delegators.get(0).getKey()).getApproved();
						if (approved) {
							// Disallow approve button
							if (approve) {
								LOGGER.debug("Delegate approve is not allowed");
								return false;
							}
						} else {
							// Disallow reject button
							if (!approve) {
								LOGGER.debug("Delegate reject is not allowed");
								return false;
							}
						}
					}
				}
			}
			if (userIsApprover) {
				// Check as approver
				if (historyList.containsKey(user.getKey())) {
					if (!settings.isAllowChangeDecision()) {
						LOGGER.debug("Already approved, change decision not allowed");
						return false;
					} else {
						boolean approved = historyList.get(user.getKey()).getApproved();
						if (approved) {
							// Disallow approve button
							if (approve) {
								LOGGER.debug("Approve is not allowed");
								return false;
							}
						} else {
							// Disallow reject button
							if (!approve) {
								LOGGER.debug("Reject is not allowed");
								return false;
							}
						}
					}
				}
			}
		} else {
			// No history
			LOGGER.debug("No approval history, user is approver");
		}
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
