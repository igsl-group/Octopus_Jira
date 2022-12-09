package com.igsl.customapproval;

import java.util.Collections;
import java.util.Map;

import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

public class ApprovalDataContextProvider extends AbstractJiraContextProvider {

	@SuppressWarnings("rawtypes")
	@Override
	public Map getContextMap(ApplicationUser user, JiraHelper helper) {
		// TODO Get approval data and format it for approvalData.vm
		return Collections.emptyMap();
	}

}
