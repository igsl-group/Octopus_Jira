package com.igsl.customapproval.workflow.postfunction;

import java.util.Map;

import javax.inject.Named;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.igsl.customapproval.CustomApprovalUtil;
import com.igsl.customapproval.data.ApprovalData;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

@Named
public class ClearApprovalHistoryPostFunction extends AbstractJiraFunctionProvider {

	private static final Logger LOGGER = Logger.getLogger(ClearApprovalHistoryPostFunction.class);
	
	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
		// transientVars contains the issue object
		// Post function configuration is in args
		ApplicationUser adminUser = CustomApprovalUtil.getAdminUser();
		MutableIssue issue = getIssue(transientVars);
		if (args != null) {
			try {
				ApprovalData approvalData = CustomApprovalUtil.getApprovalData(issue);
				if (approvalData == null) {
					LOGGER.info("Issue " + issue.getKey() + " has no approval data to clear");
					return;
				}
				String[] names = ClearApprovalHistoryPostFunctionFactory.parseArguments(args);
				StringBuilder sb = new StringBuilder();
				if (names != null) {
					for (String n : names) {
						sb.append(n).append(",");
					}
				}
				String list = "";
				if (sb.length() > 1) {
					list = sb.substring(0, sb.length() - 1);
				} else {
					list = "all approvals";
				}
				String nameList = "[" + list + "]";
				CustomApprovalUtil.clearApprovalHistory(adminUser, issue, names);
				LOGGER.info("Issue " + issue.getKey() + " approval " + nameList + " cleared");
			} catch (Exception ex) {
				throw new WorkflowException("Failed to clear approval history", ex);
			}
		}
	}

}
