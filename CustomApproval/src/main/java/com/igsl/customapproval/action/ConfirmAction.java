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
	
	private static final String APPROVE_TARGET = "/secure/CustomApprovalApproveAction.jspa?id=";
	private static final String REJECT_TARGET = "/secure/CustomApprovalRejectAction.jspa?id=";
	private static final String EXIT_LINK = "/browse/";
	
	private static final String PARAM_ISSUE_ID = "id";
	private static final String PARAM_APPROVE = "approve";
	
	private String title;
	private String message;
	private String ok;
	private String cancel;
	private String formTarget;
	private String exitLink;
	
	@Override
	protected String doExecute() throws Exception {
		String baseURL = this.getHttpRequest().getContextPath();
		String issueIdString = this.getHttpRequest().getParameter(PARAM_ISSUE_ID);
		long issueId = Long.parseLong(issueIdString);
		String approveString = this.getHttpRequest().getParameter(PARAM_APPROVE);
		boolean approve = Boolean.parseBoolean(approveString);
		MutableIssue issue = ISSUE_MANAGER.getIssueObject(issueId);
		if (issue != null) {
			ApprovalSettings settings = CustomApprovalUtil.getApprovalSettings(issue);
			if (settings != null) {
				this.title = settings.getConfirmTitle();
				if (approve) {
					this.message = settings.getApproveMessage();
					this.formTarget = APPROVE_TARGET + issueId;
				} else {
					this.message = settings.getRejectMessage();
					this.formTarget = REJECT_TARGET + issueId;
				}
				this.ok = settings.getConfirmOK();
				this.cancel = settings.getConfirmCancel();
				this.exitLink = baseURL + EXIT_LINK + issue.getKey();
			}
			return JiraWebActionSupport.INPUT;
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

	public String getExitLink() {
		return exitLink;
	}
}
