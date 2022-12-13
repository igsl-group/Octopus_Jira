package com.igsl.customapproval.action;

import org.apache.log4j.Logger;

import com.atlassian.jira.web.action.JiraWebActionSupport;

public class ApproveAction extends BaseAction {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ApproveAction.class);
	
	// See source code of WorkflowUIDispatcher for workflow transition
	
	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("ApproveAction doExecute()");
		
		if (!getData()) {
			return JiraWebActionSupport.ERROR;
		}
		
		LOGGER.debug("issue: " + this.issue);
		LOGGER.debug("approveAction: " + this.approveAction);
		LOGGER.debug("rejectAction: " + this.rejectAction);
		LOGGER.debug("approvalData: " + this.approvalData);
		LOGGER.debug("settings: " + this.settings);

		if (transitIssue(getLoggedInUser(), true)) {
			redirectToIssue();
			return JiraWebActionSupport.NONE;
		} else {
			return JiraWebActionSupport.ERROR;
		}		
	}
}
