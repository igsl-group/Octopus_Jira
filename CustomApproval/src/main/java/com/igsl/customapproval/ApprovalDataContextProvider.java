package com.igsl.customapproval;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.igsl.customapproval.data.ApprovalData;

public class ApprovalDataContextProvider extends AbstractJiraContextProvider {

	private static final String VELOCITY_HISTORY = "history";
	private static final String PARAM_ISSUE = "issue";
	
	@SuppressWarnings("rawtypes")
	@Override
	public Map getContextMap(ApplicationUser user, JiraHelper helper) {
		Map<String, Object> result = new HashMap<>();
		Issue issue = (Issue) helper.getContextParams().get(PARAM_ISSUE);
		if (issue != null) {
			CustomField cf = PluginSetup.findCustomField();
			if (cf != null) {
				ApprovalData ad = ApprovalData.parse(String.valueOf(issue.getCustomFieldValue(cf)));
				if (ad != null) {
					result.put(VELOCITY_HISTORY, ad.getHistory());
				}
			}
		}
		return result;
	}

}
