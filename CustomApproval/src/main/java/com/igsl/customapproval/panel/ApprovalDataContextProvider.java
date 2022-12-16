package com.igsl.customapproval.panel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.igsl.customapproval.CustomApprovalSetup;
import com.igsl.customapproval.CustomApprovalUtil;
import com.igsl.customapproval.data.ApprovalData;
import com.igsl.customapproval.data.ApprovalHistory;
import com.igsl.customapproval.data.ApprovalSettings;

public class ApprovalDataContextProvider extends AbstractJiraContextProvider {

	private static final String VELOCITY_HISTORY = "history";
	private static final String VELOCITY_SETTINGS = "settings";
	
	private static final String PARAM_ISSUE = "issue";
	
	@SuppressWarnings("rawtypes")
	@Override
	public Map getContextMap(ApplicationUser user, JiraHelper helper) {
		Map<String, Object> result = new HashMap<>();
		Issue issue = (Issue) helper.getContextParams().get(PARAM_ISSUE);
		if (issue != null) {
			CustomField cf = CustomApprovalSetup.getApprovalDataCustomField();
			if (cf != null) {
				ApprovalData ad = ApprovalData.parse(String.valueOf(issue.getCustomFieldValue(cf)));
				if (ad != null) {
					Map<String, ApprovalPanelSettings> displaySettings = new HashMap<>();
					Map<String, List<ApprovalPanelHistory>> history = new HashMap<>();
					for (Map.Entry<String, Map<String, ApprovalHistory>> entry : ad.getHistory().entrySet()) {
						ApprovalSettings settings = ad.getSettings().get(entry.getKey());
						ApprovalPanelSettings displaySetting = new ApprovalPanelSettings();
						Map<String, ApplicationUser> approverList = 
								CustomApprovalUtil.getApproverList(issue, settings);
						displaySetting.setApproveCountTarget(CustomApprovalUtil.getApproveCountTarget(settings, approverList));
						displaySetting.setRejectCountTarget(CustomApprovalUtil.getRejectCountTarget(settings, approverList));
						List<ApprovalPanelHistory> list = new ArrayList<>();
						for (ApprovalHistory historyItem : entry.getValue().values()) {
							ApprovalPanelHistory displayHistory = new ApprovalPanelHistory(historyItem, issue, settings);
							list.add(displayHistory);
							if (displayHistory.isValid()) {
								if (displayHistory.getApproved()) {
									displaySetting.setApproveCount(displaySetting.getApproveCount() + 1);
								} else {
									displaySetting.setRejectCount(displaySetting.getRejectCount() + 1);
								}
							}
						}
						history.put(entry.getKey(), list);
						displaySettings.put(entry.getKey(), displaySetting);
					}
					result.put(VELOCITY_SETTINGS, displaySettings);
					result.put(VELOCITY_HISTORY, history);
				}
			}
		}
		return result;
	}

}
