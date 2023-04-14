package com.igsl.customapproval.action;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.igsl.customapproval.CustomApprovalUtil;
import com.igsl.customapproval.data.ApprovalSettings;

public class ConfirmAction extends JiraWebActionSupport {

	private static final long serialVersionUID = 1L;

	private static final IssueManager ISSUE_MANAGER = ComponentAccessor.getIssueManager();
	
	private static final String SELF_LINK = "/secure/CustomApprovalConfirmAction.jspa?";
	private static final String EXIT_LINK = "/browse/";
	
	private static final String PARAM_ISSUE_ID = "id";
	private static final String PARAM_APPROVE = "approve";
	private static final String PARAM_SUBMIT = "submit";
	
	private String title;
	private String message;
	private String ok;
	private String cancel;
	private String formTarget;
	private String reloadPage;
	
	@Override
	protected String doExecute() throws Exception {
		this.reloadPage = null;
		String baseURL = this.getHttpRequest().getContextPath();
		String issueIdString = this.getHttpRequest().getParameter(PARAM_ISSUE_ID);
		long issueId = Long.parseLong(issueIdString);
		String approveString = this.getHttpRequest().getParameter(PARAM_APPROVE);
		boolean approve = Boolean.parseBoolean(approveString);
		String submitString = this.getHttpRequest().getParameter(PARAM_SUBMIT);
		boolean submit = Boolean.parseBoolean(submitString);
		MutableIssue issue = ISSUE_MANAGER.getIssueObject(issueId);
		if (issue != null) {
			// Retrieve data for the page
			this.formTarget = SELF_LINK + 
					PARAM_ISSUE_ID + "=" + issue.getId() + "&" + 
					PARAM_APPROVE + "=" + approve + "&" + 
					PARAM_SUBMIT + "=true";
			ApprovalSettings settings = CustomApprovalUtil.getApprovalSettings(issue);
			if (settings != null) {
				this.title = settings.getConfirmTitle();
				if (approve) {
					this.message = settings.getApproveMessage();
				} else {
					this.message = settings.getRejectMessage();
				}
				this.ok = settings.getConfirmOK();
				this.cancel = settings.getConfirmCancel();
			}
			if (submit) {
				// Perform submit and reload the issue
				try {
					CustomApprovalUtil.approve(issue, getLoggedInUser(), approve);
					// Send a signal to the form to reload instead
					this.reloadPage = baseURL + EXIT_LINK + issue.getKey();
					return JiraWebActionSupport.INPUT;
				} catch (Exception ex) {
					this.addErrorMessage(ex.getMessage());
					return JiraWebActionSupport.ERROR;
				}
			} else {
				return JiraWebActionSupport.INPUT;
			}
		}
		return JiraWebActionSupport.ERROR;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public String getOk() {
		return ok;
	}

	public String getCancel() {
		return cancel;
	}

	public String getFormTarget() {
		return formTarget;
	}

	public String getReloadPage() {
		return reloadPage;
	}
}
