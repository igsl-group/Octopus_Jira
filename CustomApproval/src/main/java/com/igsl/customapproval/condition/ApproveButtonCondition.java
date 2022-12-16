package com.igsl.customapproval.condition;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

public class ApproveButtonCondition extends ApprovalCondition {

	@Override
	public boolean shouldDisplay(ApplicationUser user, JiraHelper helper) {
		return isUserApprover(user, helper, true);
	}

}