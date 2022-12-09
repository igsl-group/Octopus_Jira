package com.igsl.customapproval.condition;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.igsl.customapproval.PluginSetup;
import com.igsl.customapproval.data.ApprovalData;

public abstract class Condition extends AbstractWebCondition {

	private static final Logger LOGGER = Logger.getLogger(Condition.class);
	
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
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check if user is approver or approver delegate
	 * @param user ApplicationUser to check
	 * @param helper JiraHelper
	 * @return boolean
	 */
	protected final boolean isUserApprover(ApplicationUser user, JiraHelper helper) {
		Issue issue = getIssue(helper);
		CustomField cf = PluginSetup.findCustomField();
		Object value = issue.getCustomFieldValue(cf);
		if (value != null) {
			ApprovalData data = ApprovalData.parse(String.valueOf(value));
			if (data != null) {
				// Check if status matches
				
				// Check if user is approver
				
				// Check if user is delegates

				// Check if allow changing decision
				
				return true;
			}
		}
		return false;
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
