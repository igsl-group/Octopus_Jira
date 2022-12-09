package com.igsl.customapproval.data;

public class ApprovalSettings {	
	
	/**
	 * Approver user list from a custom field
	 */
	private String approverUserField;
	/**
	 * Approver group list from a custom field
	 */
	private String approverGroupField;	
	/**
	 * Start approval at this status
	 */
	private String startingStatus;
	/**
	 * If approved, transit to this status
	 */
	private String approvedStatus;
	/**
	 * If rejected, transit to this status
	 */
	private String rejectedStatus;
	/**
	 * Required no. of approves to approve
	 * 
	 * If 0, all approvers.
	 * If between 0 and 1, percentage size of approver list.
	 * Otherwise value is rounded up. 
	 * If larger than approver list size, cap to approver list size.
	 */
	private float approveCount = 1;
	/**
	 * Required no. of rejects to reject
	 * 
	 * If 0, all approvers.
	 * If between 0 and 1, percentage size of approver list.
	 * Otherwise value is rounded up. 
	 * If larger than approver list size, cap to approver list size.
	 */
	private float rejectCount = 1;
	/**
	 * Allow approvers to change decision. 
	 * This is used to resolve ties.
	 */
	private boolean allowChangeDecision = false;
	
	public String getStartingStatus() {
		return startingStatus;
	}
	public void setStartingStatus(String startingStatus) {
		this.startingStatus = startingStatus;
	}
	public String getApprovedStatus() {
		return approvedStatus;
	}
	public void setApprovedStatus(String approvedStatus) {
		this.approvedStatus = approvedStatus;
	}
	public String getRejectedStatus() {
		return rejectedStatus;
	}
	public void setRejectedStatus(String rejectedStatus) {
		this.rejectedStatus = rejectedStatus;
	}
	public float getApproveCount() {
		return approveCount;
	}
	public void setApproveCount(float approveCount) {
		this.approveCount = approveCount;
	}
	public float getRejectCount() {
		return rejectCount;
	}
	public void setRejectCount(float rejectCount) {
		this.rejectCount = rejectCount;
	}
	public boolean isAllowChangeDecision() {
		return allowChangeDecision;
	}
	public void setAllowChangeDecision(boolean allowChangeDecision) {
		this.allowChangeDecision = allowChangeDecision;
	}
	public String getApproverUserField() {
		return approverUserField;
	}
	public void setApproverUserField(String approverUserField) {
		this.approverUserField = approverUserField;
	}
	public String getApproverGroupField() {
		return approverGroupField;
	}
	public void setApproverGroupField(String approverGroupField) {
		this.approverGroupField = approverGroupField;
	}
}
