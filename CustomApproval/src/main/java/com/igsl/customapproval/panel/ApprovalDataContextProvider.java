package com.igsl.customapproval.panel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.igsl.customapproval.CustomApprovalUtil;

public class ApprovalDataContextProvider extends AbstractJiraContextProvider {

	private static final String VELOCITY_DATA = "data";
	
	private static final String PARAM_ISSUE = "issue";
	
	@SuppressWarnings("rawtypes")
	@Override
	public Map getContextMap(ApplicationUser user, JiraHelper helper) {
		Map<String, Object> result = new HashMap<>();
		Issue issue = (Issue) helper.getContextParams().get(PARAM_ISSUE);
		Collection<ApprovalPanelData> data = CustomApprovalUtil.getPanelData(issue);
		if (data != null) {
			result.put(VELOCITY_DATA, data);
		}
		return result;
	}

}
