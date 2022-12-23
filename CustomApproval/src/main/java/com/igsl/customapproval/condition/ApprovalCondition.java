package com.igsl.customapproval.condition;

import java.util.List;
import java.util.Map;

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
		return CustomApprovalUtil.hasButton(issue, user, approve);
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
