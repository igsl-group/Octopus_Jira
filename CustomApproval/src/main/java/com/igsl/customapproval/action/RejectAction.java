package com.igsl.customapproval.action;

import org.apache.log4j.Logger;

import com.atlassian.jira.web.action.JiraWebActionSupport;

public class RejectAction extends BaseAction {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ApproveAction.class);
	
	@Override
	protected void doValidation() {
	}
	
	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("RejectAction doExecute()");
		
		if (!getData()) {
			return JiraWebActionSupport.ERROR;
		}
		
		LOGGER.debug("issue: " + this.issue);
		LOGGER.debug("approveAction: " + this.approveAction);
		LOGGER.debug("rejectAction: " + this.rejectAction);
		LOGGER.debug("approvalData: " + this.approvalData);
		LOGGER.debug("settings: " + this.settings);

		if (transitIssue(getLoggedInUser(), false)) {
			redirectToIssue();
			return JiraWebActionSupport.NONE;
		} else {
			return JiraWebActionSupport.ERROR;
		}	
	}
}
