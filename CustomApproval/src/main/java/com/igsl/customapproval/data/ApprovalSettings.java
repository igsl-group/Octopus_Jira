package com.igsl.customapproval.data;

import java.util.ArrayList;
import java.util.List;

public class ApprovalSettings {	
	
	/**
	 * Approval name
	 */
	private String approvalName;
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
	 * Use this transition for approvedStatus
	 * If null, code will attempt to locate it automatically
	 */
	private String approveTransition;
	/**
	 * If rejected, transit to this status
	 */
	private String rejectedStatus;
	/**
	 * Use this transition for rejectedStatus.
	 * If null, code will attempt to locate it automatically
	 */
	private String rejectTransition;
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
	/**
	 * Flag to indicate approval has completed
	 */
	private boolean completed = false;
	/**
	 * Approved or not.
	 */
	private boolean approved;
	/**
	 * Approve count target, locked in when transitioning.
	 */
	private double finalApproveCountTarget;
	/**
	 * Reject count target, locked in when transitioning.
	 */
	private double finalRejectCountTarget;
	/**
	 * Approve count, locked in when transitioning.
	 */
	private double finalApproveCount;
	/**
	 * Reject count, locked in when transitioning.
	 */
	private double finalRejectCount;
	/**
	 * Approver user list, locked in when transitioning.
	 */
	private List<String> finalApproverList = new ArrayList<>();
	
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
	public String getApprovalName() {
		return approvalName;
	}
	public void setApprovalName(String approvalName) {
		this.approvalName = approvalName;
	}
	public double getFinalApproveCountTarget() {
		return finalApproveCountTarget;
	}
	public void setFinalApproveCountTarget(double finalApproveCountTarget) {
		this.finalApproveCountTarget = finalApproveCountTarget;
	}
	public double getFinalRejectCountTarget() {
		return finalRejectCountTarget;
	}
	public void setFinalRejectCountTarget(double finalRejectCountTarget) {
		this.finalRejectCountTarget = finalRejectCountTarget;
	}
	public double getFinalApproveCount() {
		return finalApproveCount;
	}
	public void setFinalApproveCount(double finalApproveCount) {
		this.finalApproveCount = finalApproveCount;
	}
	public double getFinalRejectCount() {
		return finalRejectCount;
	}
	public void setFinalRejectCount(double finalRejectCount) {
		this.finalRejectCount = finalRejectCount;
	}
	public boolean isCompleted() {
		return completed;
	}
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	public boolean isApproved() {
		return approved;
	}
	public void setApproved(boolean approved) {
		this.approved = approved;
	}
	public List<String> getFinalApproverList() {
		return finalApproverList;
	}
	public void setFinalApproverList(List<String> finalApproverList) {
		this.finalApproverList = finalApproverList;
	}
	public String getApproveTransition() {
		return approveTransition;
	}
	public void setApproveTransition(String approveTransition) {
		this.approveTransition = approveTransition;
	}
	public String getRejectTransition() {
		return rejectTransition;
	}
	public void setRejectTransition(String rejectTransition) {
		this.rejectTransition = rejectTransition;
	}
}
