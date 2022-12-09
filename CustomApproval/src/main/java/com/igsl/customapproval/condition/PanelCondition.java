package com.igsl.customapproval.condition;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

public class PanelCondition extends Condition {

	@Override
	public boolean shouldDisplay(ApplicationUser user, JiraHelper helper) {
		return isApprovalEnabled(helper);
	}

}
