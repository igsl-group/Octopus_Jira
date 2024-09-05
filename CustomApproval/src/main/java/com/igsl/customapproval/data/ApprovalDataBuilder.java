package com.igsl.customapproval.data;

import java.util.HashMap;
import java.util.Map;

import com.igsl.customapproval.CustomApprovalUtil;

public class ApprovalDataBuilder {

	private ApprovalData data;
	
	/**
	 * Create default ApprovalData 
	 */
	public ApprovalDataBuilder() {
		data = new ApprovalData();
	}
	
	/**
	 * Create deep clone of provided ApprovalData (except ApprovalHistory, which will be left empty)
	 * @param data ApprovalData to be cloned
	 */
	public ApprovalDataBuilder(ApprovalData data) {
		this.data = new ApprovalData();
		// Deep clone data, except ApprovalHistory
		for (Map.Entry<String, ApprovalSettings> entry : data.getSettings().entrySet()) {
			ApprovalSettings settings = new ApprovalSettings();
			ApprovalSettings src = entry.getValue();
			settings.setApprovalName(src.getApprovalName());
			settings.setAllowChangeDecision(src.isAllowChangeDecision());
			settings.setApproveCount(src.getApproveCount());
			settings.setApproverGroupField(src.getApproverGroupField());
			settings.setApproverUserField(src.getApproverUserField());
			settings.setApprovedStatus(src.getApprovedStatus());
			settings.setApproveTransition(src.getApproveTransition());
			settings.setRejectCount(src.getRejectCount());
			settings.setRejectedStatus(src.getRejectedStatus());
			settings.setRejectTransition(src.getRejectTransition());
			settings.setStartingStatus(src.getStartingStatus());
			settings.setConfirmDecision(src.isConfirmDecision());
			settings.setConfirmTitle(src.getConfirmTitle());
			settings.setApproveMessage(src.getApproveMessage());
			settings.setRejectMessage(src.getRejectMessage());
			settings.setConfirmOK(src.getConfirmOK());
			settings.setConfirmCancel(src.getConfirmCancel());
			settings.setNoApproverAction(src.getNoApproverAction());
			this.data.getSettings().put(entry.getKey(), settings);
			this.data.getHistory().put(entry.getKey(), new HashMap<String, ApprovalHistory>());
		}
	}
	
	/**
	 * Add a new approval
	 * @param approvalName Name of approval
	 * @param startingStatus Starting status key
	 * @param approveStatus Approve status key
	 * @param approveTransition Approve transition ID
	 * @param rejectStatus Reject status key
	 * @param rejectTransition Reject transition ID
	 * @return ApprovalDataBuilder
	 * @throws Exception If approval already exists or status is not valid
	 */
	public ApprovalDataBuilder addApproval(
			String approvalName, String startingStatus, String approveTransition,
			String approveStatus, String rejectStatus, String rejectTransition
			) throws Exception {
		if (data.getSettings().containsKey(approvalName)) {
			throw new Exception("Approval \"" + approvalName + "\" already exists");
		} else {
			if (CustomApprovalUtil.checkStatus(startingStatus) == null) {
				throw new Exception("Starting status \"" + startingStatus + "\" is not valid");
			}
			if (CustomApprovalUtil.checkStatus(approveStatus) == null) {
				throw new Exception("Approve status \"" + approveStatus + "\" is not valid");
			}
			if (CustomApprovalUtil.checkStatus(rejectStatus) == null) {
				throw new Exception("Reject status \"" + rejectStatus + "\" is not valid");
			}
			ApprovalSettings settings = new ApprovalSettings();
			settings.setApprovalName(approvalName);
			settings.setStartingStatus(startingStatus);
			settings.setApprovedStatus(approveStatus);
			settings.setApproveTransition(approveTransition);
			settings.setRejectedStatus(rejectStatus);
			settings.setRejectTransition(rejectTransition);
			settings.setNoApproverAction(NoApproverAction.NO_ACTION);
			data.getSettings().put(approvalName, settings);
		}
		return this;
	}
	
	public ApprovalDataBuilder setConfirmation(
			String approvalName,
			boolean confirmDecision, 
			String confirmTitle, String approveMessage, String rejectMessage, String confirmOK, String confirmCancel) 
			throws Exception {
		if (!data.getSettings().containsKey(approvalName)) {
			throw new Exception("Approval \"" + approvalName + "\" does not exist");
		} else {
			data.getSettings().get(approvalName).setConfirmDecision(confirmDecision);
			data.getSettings().get(approvalName).setConfirmTitle(confirmTitle);
			data.getSettings().get(approvalName).setApproveMessage(approveMessage);
			data.getSettings().get(approvalName).setRejectMessage(rejectMessage);
			data.getSettings().get(approvalName).setConfirmOK(confirmOK);
			data.getSettings().get(approvalName).setConfirmCancel(confirmCancel);
		}
		return this;
	}
	
	/**
	 * Set approver user list to be from a custom field
	 * @param approvalName Name of approval
	 * @param fieldName Name of custom field
	 * @return ApprovalDataBuilder
	 * @throws Exception If approval does not exist
	 */
	public ApprovalDataBuilder setApproverUserField(String approvalName, String fieldName) throws Exception {
		if (!data.getSettings().containsKey(approvalName)) {
			throw new Exception("Approval \"" + approvalName + "\" does not exist");
		} else {
			if (fieldName != null && !fieldName.isEmpty()) {
				if (CustomApprovalUtil.checkUserField(fieldName) != null) {
					data.getSettings().get(approvalName).setApproverUserField(fieldName);
				} else {
					throw new Exception("Custom field \"" + fieldName + "\" is not a user picker");
				}
			} else {
				data.getSettings().get(approvalName).setApproverUserField("");
			}
		}
		return this;
	}
	
	/**
	 * Set approver group list to be from a custom field
	 * @param approvalName Name of approval
	 * @param fieldName Custom field name
	 * @return ApprovalDataBuilder
	 * @throws Exception If approval does not exist
	 */
	public ApprovalDataBuilder setApproverGroupField(String approvalName, String fieldName) throws Exception {
		if (!data.getSettings().containsKey(approvalName)) {
			throw new Exception("Approval \"" + approvalName + "\" does not exist");
		} else {
			if (fieldName != null && !fieldName.isEmpty()) {
				if (CustomApprovalUtil.checkGroupField(fieldName) != null) {
					data.getSettings().get(approvalName).setApproverGroupField(fieldName);	
				} else {
					throw new Exception("Custom field \"" + fieldName + "\" is not a group picker");
				}
			} else {
				data.getSettings().get(approvalName).setApproverGroupField("");	
			}
		}
		return this;
	}
	
	/**
	 * Set approve count
	 * @param approvalName Approval name
	 * @param count 0 for all approvers, # for approver count, decimals for percentage.
	 * @return ApprovalDataBuilder
	 * @throws Exception If approval does not exist, or count less than 0
	 */
	public ApprovalDataBuilder setApproveCount(String approvalName, float count) throws Exception {
		if (!data.getSettings().containsKey(approvalName)) {
			throw new Exception("Approval \"" + approvalName + "\" does not exist");
		} else {
			if (count < 0) {
				throw new Exception("Approve count " + count + " must be larger than or equal to 0");
			}
			ApprovalSettings setting = data.getSettings().get(approvalName);
			setting.setApproveCount(count);
		}
		return this;
	}
	
	/**
	 * Set reject count
	 * @param approvalName Approval name
	 * @param count 0 for all approvers, # for approver count, decimals for percentage.
	 * @return ApprovalDataBuilder
	 * @throws Exception If approval does not exist, or count less than 0
	 */
	public ApprovalDataBuilder setRejectCount(String approvalName, float count) throws Exception {
		if (!data.getSettings().containsKey(approvalName)) {
			throw new Exception("Approval \"" + approvalName + "\" does not exist");
		} else {
			if (count < 0) {
				throw new Exception("Reject count " + count + " must be larger than or equal to 0");
			}
			ApprovalSettings setting = data.getSettings().get(approvalName);
			setting.setRejectCount(count);
		}
		return this;
	}
	
	/**
	 * Set allow approvers to change decision.
	 * This is used as a tie-breaker.
	 * @param approvalName Approval name
	 * @param allowChangeDecision boolean
	 * @return ApprovalDataBuilder
	 * @throws Exception If approval does not exist
	 */
	public ApprovalDataBuilder setAllowChangeDecision(String approvalName, boolean allowChangeDecision) throws Exception {
		if (!data.getSettings().containsKey(approvalName)) {
			throw new Exception("Approval \"" + approvalName + "\" does not exist");
		} else {
			ApprovalSettings setting = data.getSettings().get(approvalName);
			setting.setAllowChangeDecision(allowChangeDecision);
		}
		return this;
	}
	
	/**
	 * Set noApproverAction
	 * @param approvalName
	 * @param noApproverAction
	 * @return
	 * @throws Exception
	 */
	public ApprovalDataBuilder setNoApproverAction(String approvalName, NoApproverAction noApproverAction) throws Exception {
		if (!data.getSettings().containsKey(approvalName)) {
			throw new Exception("Approval \"" + approvalName + "\" does not exist");
		} else {
			if (noApproverAction == null) {
				noApproverAction = NoApproverAction.NO_ACTION;
			}
			ApprovalSettings setting = data.getSettings().get(approvalName);
			setting.setNoApproverAction(noApproverAction);
		}
		return this;
	}
	
	/**
	 * Return constructed ApprovalData
	 * @return ApprovalData
	 */
	public ApprovalData build() {
		return data;
	}

}
