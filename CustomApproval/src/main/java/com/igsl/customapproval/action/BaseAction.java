package com.igsl.customapproval.action;

import java.io.IOException;

import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.igsl.customapproval.CustomApprovalUtil;
import com.igsl.customapproval.data.ApprovalSettings;

public abstract class BaseAction extends JiraWebActionSupport {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(BaseAction.class);
	protected static final String PARAM_ISSUE_ID = "id";

	protected static final IssueManager ISSUE_MANAGER = ComponentAccessor.getIssueManager();
	
	protected MutableIssue issue;
	protected String issueURL;

	public String getIssueURL() {
		return this.issueURL;
	}
	
	/**
	 * Transit issue
	 * @param user Transit using this user. Should be current user.
	 * @param approve Boolean. Determines if approveAction or rejectAction is used.
	 * @return boolean. If false, return JiraWebActionSupport.ERROR from doXXX() method. 
	 */
	protected boolean transitIssue(ApplicationUser user, boolean approve) {
		String issueId = getHttpRequest().getParameter(PARAM_ISSUE_ID);
		long issueIdAsLong = Long.parseLong(issueId);
		this.issue = ComponentAccessor.getIssueManager().getIssueObject(issueIdAsLong);
		if (this.issue != null) {
			String baseURL = ComponentAccessor.getApplicationProperties().getJiraBaseUrl();
			String referrer = getHttpRequest().getHeader(HttpHeaders.REFERER);
			LOGGER.debug("Referrer: " + referrer);
			if (referrer != null && referrer.startsWith(baseURL)) {
				this.issueURL = referrer;
			} else {
				this.issueURL = baseURL + 
						"/browse/" + this.issue.getKey();	// TODO Handle embedded pages like search issue
			}
			ApprovalSettings settings = CustomApprovalUtil.getApprovalSettings(issue);
			try {
				CustomApprovalUtil.approve(issue, settings, user, approve);
				return true;
			} catch (Exception ex) {
				LOGGER.error("Failed to approve/reject issue", ex);
				this.addErrorMessage(ex.getMessage());
			}
		}
		return false;
	}
	
	/**
	 * Return to view issue page.
	 * Return JiraWebActionSupport.NONE in doXXX() method after calling this.
	 * @throws IOException
	 */
	protected void redirectToIssue() throws IOException {
		if (this.issue != null) {
			getHttpResponse().sendRedirect(this.issueURL);
		}
	}
}
