package com.igsl.customapproval.condition;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

public class ApproveButtonWithConfirmCondition extends ApprovalCondition {

	@Override
	public boolean shouldDisplay(ApplicationUser user, JiraHelper helper) {
		if (isUserApprover(user, helper, true)) {
			return isConfirmationEnabled(helper);
		}
		return false;
	}

}
