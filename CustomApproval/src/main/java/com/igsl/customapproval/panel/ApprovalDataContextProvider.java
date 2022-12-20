package com.igsl.customapproval.panel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
	private static final String VELOCITY_CURRENT = "current";
	
	private static final String PARAM_ISSUE = "issue";
	
	@SuppressWarnings("rawtypes")
	@Override
	public Map getContextMap(ApplicationUser user, JiraHelper helper) {
		Map<String, Object> result = new HashMap<>();
		Issue issue = (Issue) helper.getContextParams().get(PARAM_ISSUE);
		ApprovalSettings currentSettings = CustomApprovalUtil.getApprovalSettings(issue);
		if (issue != null) {
			CustomField cf = CustomApprovalSetup.getApprovalDataCustomField();
			if (cf != null) {
				ApprovalData ad = ApprovalData.parse(String.valueOf(issue.getCustomFieldValue(cf)));
				if (ad != null) {
					Map<String, ApprovalPanelSettings> displaySettings = new HashMap<>();
					Map<String, List<ApprovalPanelHistory>> history = new LinkedHashMap<>();
					ApprovalPanelSettings currentApproval = null;
					for (Map.Entry<String, Map<String, ApprovalHistory>> entry : ad.getHistory().entrySet()) {
						ApprovalSettings settings = ad.getSettings().get(entry.getKey());
						ApprovalPanelSettings displaySetting = new ApprovalPanelSettings(settings);
						Map<String, ApplicationUser> approverList = 
								CustomApprovalUtil.getApproverList(issue, settings);
						if (!settings.isCompleted()) {
							displaySetting.setApproveCountTarget(CustomApprovalUtil.getApproveCountTarget(settings, approverList));
							displaySetting.setRejectCountTarget(CustomApprovalUtil.getRejectCountTarget(settings, approverList));
						}
						int aCount = 0;
						int rCount = 0;
						List<ApprovalPanelHistory> list = new ArrayList<>();
						for (ApprovalHistory historyItem : entry.getValue().values()) {
							ApprovalPanelHistory displayHistory = new ApprovalPanelHistory(historyItem, issue, settings);
							list.add(displayHistory);
							if (displayHistory.isValid() && !settings.isCompleted()) {
								if (displayHistory.getApproved()) {
									aCount++;
								} else {
									rCount++;
								}
							}
						}
						if (!settings.isCompleted()) {
							displaySetting.setApproveCount(aCount);
							displaySetting.setRejectCount(rCount);
						}
						history.put(entry.getKey(), list);
						displaySettings.put(entry.getKey(), displaySetting);
					}
					ApprovalSettings currentApprovalSettings = CustomApprovalUtil.getApprovalSettings(issue);
					if (currentApprovalSettings != null) {
						if (!ad.getHistory().containsKey(currentApprovalSettings.getApprovalName())) {
							Map<String, ApplicationUser> approverList = 
									CustomApprovalUtil.getApproverList(issue, currentApprovalSettings);
							currentApproval = new ApprovalPanelSettings(currentApprovalSettings);
							currentApproval.setApprovalName(currentApprovalSettings.getApprovalName());
							currentApproval.setApproveCount(0);
							currentApproval.setRejectCount(0);
							currentApproval.setApproveCountTarget(
									CustomApprovalUtil.getApproveCountTarget(currentApprovalSettings, approverList));
							currentApproval.setRejectCountTarget(
									CustomApprovalUtil.getRejectCountTarget(currentApprovalSettings, approverList));
						}
					}
					result.put(VELOCITY_SETTINGS, displaySettings);
					result.put(VELOCITY_HISTORY, history);
					result.put(VELOCITY_CURRENT, currentApproval);
				}
			}
		}
		return result;
	}

}
