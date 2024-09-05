package com.igsl.customapproval.workflow.postfunction;

import java.util.Map;

import javax.inject.Named;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.igsl.customapproval.CustomApprovalSetup;
import com.igsl.customapproval.CustomApprovalUtil;
import com.igsl.customapproval.data.ApprovalData;
import com.igsl.customapproval.data.ApprovalDataBuilder;
import com.igsl.customapproval.data.NoApproverAction;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

@Named
public class InitializeApprovalPostFunction extends AbstractJiraFunctionProvider {

	private static final Logger LOGGER = Logger.getLogger(InitializeApprovalPostFunction.class);
	
	private static String getData(Map<String, String[]> data, String name, int index, String defaultValue) {
		String[] list = data.get(name);
		if (list != null && list.length > index) {
			return list[index];
		}
		return defaultValue;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
		// transientVars contains the issue object
		// Post function configuration is in args
		CustomField cf = CustomApprovalSetup.getApprovalDataCustomField();
		ApplicationUser adminUser = CustomApprovalUtil.getAdminUser();
		MutableIssue issue = getIssue(transientVars);
		
		ApprovalDataBuilder builder = new ApprovalDataBuilder();
		if (args != null) {
			try {
				Map<String, String[]> data = InitializeApprovalPostFunctionFactory.parseArguments(args);
				for (int i = 0; i < data.get(InitializeApprovalPostFunctionFactory.PARAM_APPROVAL_NAME).length; i++) {
					// Add approval
					String approvalName = getData(data, InitializeApprovalPostFunctionFactory.PARAM_APPROVAL_NAME, i, null);
					String startingStatus = getData(data, InitializeApprovalPostFunctionFactory.PARAM_STATUS_STARING, i, null);
					String approvedStatus = getData(data, InitializeApprovalPostFunctionFactory.PARAM_STATUS_APPROVED, i, null);
					String approveTransition =  getData(data, InitializeApprovalPostFunctionFactory.PARAM_APPROVE_TRANSITION, i, null);
					String rejectedStatus =  getData(data, InitializeApprovalPostFunctionFactory.PARAM_STATUS_REJECTED, i, null);
					String rejectTransition =  getData(data, InitializeApprovalPostFunctionFactory.PARAM_REJECT_TRANSITION, i, null);
					String confirmDecisionString = getData(data, InitializeApprovalPostFunctionFactory.PARAM_CONFIRM_DECISION, i, "");
					boolean confirmDecision = Boolean.parseBoolean(confirmDecisionString);
					String confirmTitle = getData(data, InitializeApprovalPostFunctionFactory.PARAM_CONFIRM_TITLE, i, "Confirm Approval Decision");
					String approveMessage = getData(data, InitializeApprovalPostFunctionFactory.PARAM_APPROVE_MESSAGE, i, "Approve issue?");
					String rejectMessage = getData(data, InitializeApprovalPostFunctionFactory.PARAM_REJECT_MESSAGE, i, "Reject issue?");
					String confirmOK = getData(data, InitializeApprovalPostFunctionFactory.PARAM_CONFIRM_OK, i, "OK");
					String confirmCancel = getData(data, InitializeApprovalPostFunctionFactory.PARAM_CONFIRM_CANCEL, i, "Cancel");
					builder.addApproval(
							approvalName, startingStatus, approveTransition, 
							approvedStatus, rejectedStatus, rejectTransition);
					// Confirmation dialog
					builder.setConfirmation(
							approvalName, confirmDecision, 
							confirmTitle, approveMessage, rejectMessage, confirmOK, confirmCancel);
					// Approve count
					String approveCount = data.get(InitializeApprovalPostFunctionFactory.PARAM_APPROVE_COUNT)[i];
					builder.setApproveCount(approvalName, Float.parseFloat(approveCount));
					// Reject count
					String rejectCount = data.get(InitializeApprovalPostFunctionFactory.PARAM_REJECT_COUNT)[i];
					builder.setRejectCount(approvalName, Float.parseFloat(rejectCount));
					// Allow change decision
					String allowChangeDecision = data.get(InitializeApprovalPostFunctionFactory.PARAM_ALLOW_CHANGE_DECISION)[i];
					builder.setAllowChangeDecision(approvalName, 
							(InitializeApprovalPostFunctionFactory.VALUE_ALLOW.equals(allowChangeDecision))? true : false);
					// Approvers
					String userField = null;
					if (data.get(InitializeApprovalPostFunctionFactory.PARAM_USERS_FIELD) != null) {
						userField = data.get(InitializeApprovalPostFunctionFactory.PARAM_USERS_FIELD)[i];
					}
					String groupField = null; 
					if (data.get(InitializeApprovalPostFunctionFactory.PARAM_GROUPS_FIELD) != null) {
						groupField = data.get(InitializeApprovalPostFunctionFactory.PARAM_GROUPS_FIELD)[i];
					}
					builder.setApproverUserField(approvalName, userField);
					builder.setApproverGroupField(approvalName, groupField);
					// No approver action
					NoApproverAction noApproverAction = null;
					if (data.get(InitializeApprovalPostFunctionFactory.PARAM_NO_APPROVER_ACTION) != null) {
						noApproverAction = NoApproverAction.parse(
								data.get(InitializeApprovalPostFunctionFactory.PARAM_NO_APPROVER_ACTION)[i]);
					}
					builder.setNoApproverAction(approvalName, noApproverAction);
				}
			} catch (Exception ex) {
				throw new WorkflowException("Failed to construct approval data", ex);
			}
		}
		
		// Update custom field
		ApprovalData ad = builder.build();
		LOGGER.debug("Saving ApprovalData: " + ad.toString());
		issue.setCustomFieldValue(cf, ad.toString());
		if (issue.isCreated()) {
			IssueManager iMan = ComponentAccessor.getIssueManager();
			iMan.updateIssue(adminUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false);
		}
	}

}
