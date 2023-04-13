package com.igsl.customapproval.condition;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

public class RejectButtonWithConfirmCondition extends ApprovalCondition {

	@Override
	public boolean shouldDisplay(ApplicationUser user, JiraHelper helper) {
		if (isUserApprover(user, helper, false)) {
			return isConfirmationEnabled(helper);
		}
		return false;
	}

}
