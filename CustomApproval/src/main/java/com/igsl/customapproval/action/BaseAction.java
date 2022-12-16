package com.igsl.customapproval.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.TransitionOptions;
import com.atlassian.jira.workflow.TransitionOptions.Builder;
import com.atlassian.jira.workflow.WorkflowManager;
import com.igsl.customapproval.CustomApprovalSetup;
import com.igsl.customapproval.CustomApprovalUtil;
import com.igsl.customapproval.data.ApprovalData;
import com.igsl.customapproval.data.ApprovalHistory;
import com.igsl.customapproval.data.ApprovalSettings;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

public abstract class BaseAction extends JiraWebActionSupport {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(BaseAction.class);
	protected static final String PARAM_ISSUE_ID = "id";

	protected static final IssueManager ISSUE_MANAGER = ComponentAccessor.getIssueManager();
	
	protected MutableIssue issue;
	protected ApprovalData approvalData;
	/**
	 * ApprovalSettings for current status, null if not applicable
	 */
	protected ApprovalSettings settings;
	protected String approvalName;
	protected Integer approveAction;
	protected Integer rejectAction;
	protected String issueURL;
	protected CustomField approvalDataCustomField;
	protected Map<String, ApplicationUser> approverList;
	
	public String getApprovalName() {
		return this.approvalName;
	}
	
	public String getIssueURL() {
		return this.issueURL;
	}
	
	/**
	 * Get value for fields.
	 */
	protected boolean getData() {
		this.approvalDataCustomField = CustomApprovalSetup.getApprovalDataCustomField();		
		String issueId = getHttpRequest().getParameter(PARAM_ISSUE_ID);
		long issueIdAsLong = Long.parseLong(issueId);
		this.issue = ComponentAccessor.getIssueManager().getIssueObject(issueIdAsLong);
		if (this.issue != null) {
			this.issueURL = ComponentAccessor.getApplicationProperties().getJiraBaseUrl() + 
					"/browse/" + this.issue.getKey();	// TODO Handle embedded pages like search issue
			this.approvalData = CustomApprovalUtil.getApprovalData(issue);
			if (this.approvalData == null) {
				this.addErrorMessage("Unable to read approval data from issue");
			}
			this.settings = CustomApprovalUtil.getApprovalSettings(issue);
			if (this.settings != null) {
				this.approvalName = this.settings.getApprovalName();
				this.approverList = CustomApprovalUtil.getApproverList(issue, settings);
				if (this.approverList == null || this.approverList.size() == 0) {
					this.addErrorMessage("No approvers found");
				}
				WorkflowManager wfMan = ComponentAccessor.getWorkflowManager();
				JiraWorkflow wf = wfMan.getWorkflow(issue);
				if (wf != null) {
					List<?> actions = wf.getLinkedStep(issue.getStatus()).getActions();
					for (Object a : actions) {
						ActionDescriptor desc = (ActionDescriptor) a;
						String targetStatus = getActionTarget(wf, desc);
						if (this.settings.getApprovedStatus().equals(targetStatus)) {
							this.approveAction = desc.getId();
							LOGGER.debug("Approve action found: " + this.approveAction);
						} else if (this.settings.getRejectedStatus().equals(targetStatus)) {
							this.rejectAction = desc.getId();
							LOGGER.debug("Reject action found: " + this.rejectAction);
						}
					}
				} else {
					this.addErrorMessage("Workflow cannot be found for issue " + issueId);
				}
				if (this.approveAction == null) {
					this.addErrorMessage("Approve action cannot be found in workflow");
				}
				if (this.rejectAction == null) {
					this.addErrorMessage("Reject action cannot be found in workflow");
				}				
			} else {
				this.addErrorMessage("No approval found for current issue status");
			}
		} else {
			this.addErrorMessage("Unable to read issue " + issueId);
		}
		return (this.getErrorMessages().size() == 0);
	}
	
	/**
	 * Transit issue
	 * @param user Transit using this user. Should be current user.
	 * @param approve Boolean. Determines if approveAction or rejectAction is used.
	 * @return boolean. If false, return JiraWebActionSupport.ERROR from doXXX() method. 
	 */
	protected boolean transitIssue(ApplicationUser user, boolean approve) {
		if (user == null) {
			this.addErrorMessage("Approving user is not provided");
			return false;
		}
		String lockId = null;
		try {
			lockId = CustomApprovalUtil.lockApproval(this.issue);
			if (lockId == null) {
				this.addErrorMessage("Another user is approving this issue, please try again later.");
				return false;
			}
			List<ApplicationUser> onBehalfOf = null;
			List<String> onBehalfOfList = null;
			// Validate if user is approver
			if (!CustomApprovalUtil.isApprover(user.getKey(), this.approverList)) {
				// Check is anyone's delegate
				onBehalfOf = CustomApprovalUtil.isDelegate(user.getKey(), approverList);
				if (onBehalfOf.size() == 0) {
					this.addErrorMessage("User is not an approver");
					return false;
				}
				onBehalfOfList = new ArrayList<>();
				for (ApplicationUser u : onBehalfOf) {
					onBehalfOfList.add(u.getKey());
				}
			}	
			// Update ApprovalHistory
			Map<String, ApprovalHistory> historyList = this.approvalData.getHistory().get(this.approvalName);
			if (historyList.containsKey(user.getKey())) {
				// Already approved, update decision
				ApprovalHistory historyItem = historyList.get(user.getKey());
				historyItem.setApprovedDate(new Date());
				historyItem.setApproved(approve);
				if (onBehalfOfList != null) {
					historyItem.setOnBehalfOf(onBehalfOfList);
				}
			} else {
				// Add new record
				ApprovalHistory historyItem = new ApprovalHistory();
				historyItem.setApprover(user.getKey());
				historyItem.setApprovedDate(new Date());
				historyItem.setApproved(approve);
				historyList.put(user.getKey(), historyItem);
				if (onBehalfOfList != null) {
					historyItem.setOnBehalfOf(onBehalfOfList);
				}
			}
			// Save ApprovalData
			this.issue.setCustomFieldValue(this.approvalDataCustomField, this.approvalData.toString());
			ISSUE_MANAGER.updateIssue(user, issue, EventDispatchOption.DO_NOT_DISPATCH, false);
			
			// Check approval criteria, transit issue if met
			double approveCount = 0;
			double rejectCount = 0;
			// Find history where the user or on behalf of user is still an approver
			for (ApprovalHistory historyItem : historyList.values()) {
				boolean isApprover = CustomApprovalUtil.isApprover(historyItem.getApprover(), this.approverList);
				if (!isApprover) {
					isApprover = (CustomApprovalUtil.isDelegate(historyItem.getApprover(), this.approverList) != null);
				}
				if (isApprover) {
					if (historyItem.getApproved()) {
						approveCount++;
					} else {
						rejectCount++;
					}
				}
			}
			LOGGER.debug("Current count, approve: " + approveCount + " reject: " + rejectCount);
			// Get target counts
			double approveCountTarget = CustomApprovalUtil.getApproveCountTarget(this.settings, this.approverList);
			double rejectCountTarget = CustomApprovalUtil.getRejectCountTarget(this.settings, this.approverList);
			LOGGER.debug("Target count, approve: " + approveCountTarget + " reject: " + rejectCountTarget);
			Integer targetAction = null;
			if (rejectCountTarget <= rejectCount) {
				targetAction = this.rejectAction;
			} else if (approveCountTarget <= approveCount) {
				targetAction = this.approveAction;
			}
			if (targetAction != null) {
				TransitionOptions.Builder builder = new Builder();
				// There should be a hide from user condition on the transition, so need to skip condition
				builder.skipConditions();
				IssueService iService = ComponentAccessor.getIssueService();
				TransitionValidationResult tvr = iService.validateTransition(
						user, 
						this.issue.getId(), 
						targetAction, 
						iService.newIssueInputParameters(), 
						builder.build());
				if (tvr.isValid()) {
					IssueResult ir = iService.transition(getLoggedInUser(), tvr);
					if (ir.isValid()) {				
						return true;
					} else {
						for (String s : ir.getErrorCollection().getErrorMessages()) {
							this.addErrorMessage(s);
						}
						return false;
					}
				} else {
					for (String s : tvr.getErrorCollection().getErrorMessages()) {
						this.addErrorMessage(s);
					}
					return false;
				}		
			} else {
				return true;
			}
		} finally {
			if (lockId != null) {
				CustomApprovalUtil.unlockApproval(this.issue, lockId);
			}
		}
	}
	
	private String getActionTarget(JiraWorkflow wf, ActionDescriptor actionDesc) {
		int targetStepId = actionDesc.getUnconditionalResult().getStep();
		StepDescriptor targetStepDesc = wf.getDescriptor().getStep(targetStepId);
		Status linkedStatus = wf.getLinkedStatus(targetStepDesc);
		return linkedStatus.getId();
	}
	
	/**
	 * Return to view issue page.
	 * Return JiraWebActionSupport.NONE in doXXX() method after calling this.
	 * @throws IOException
	 */
	protected void redirectToIssue() throws IOException {
		if (this.issue != null) {
			// TODO URL has ?jql= at the end for search
			getHttpResponse().sendRedirect(this.issueURL);
		}
	}
}
