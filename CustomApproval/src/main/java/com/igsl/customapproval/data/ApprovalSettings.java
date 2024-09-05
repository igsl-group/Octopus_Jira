package com.igsl.customapproval.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ApprovalSettings {	
	
	/**
	 * If true, a dialog will popup to confirm approve/reject decision.
	 */
	private boolean confirmDecision;
	/**
	 * Title of confirmation dialog
	 */
	private String confirmTitle;
	/**
	 * Approve message of confirmation dialog
	 */
	private String approveMessage;
	/**
	 * Reject message of confirmation dialog
	 */
	private String rejectMessage;
	/**
	 * Text of OK button in confirm dialog
	 */
	private String confirmOK;
	/**
	 * Text of cancen button in confirm dialog
	 */
	private String confirmCancel;
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
	 * Action if no approver is defined.
	 * Default is NO_ACTION.
	 */
	private NoApproverAction noApproverAction = NoApproverAction.NO_ACTION;
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
	public boolean isConfirmDecision() {
		return confirmDecision;
	}
	public void setConfirmDecision(boolean confirmDecision) {
		this.confirmDecision = confirmDecision;
	}
	public String getConfirmTitle() {
		return confirmTitle;
	}
	public void setConfirmTitle(String confirmTitle) {
		this.confirmTitle = confirmTitle;
	}
	public String getConfirmOK() {
		return confirmOK;
	}
	public void setConfirmOK(String confirmOK) {
		this.confirmOK = confirmOK;
	}
	public String getConfirmCancel() {
		return confirmCancel;
	}
	public void setConfirmCancel(String confirmCancel) {
		this.confirmCancel = confirmCancel;
	}
	public String getApproveMessage() {
		return approveMessage;
	}
	public void setApproveMessage(String approveMessage) {
		this.approveMessage = approveMessage;
	}
	public String getRejectMessage() {
		return rejectMessage;
	}
	public void setRejectMessage(String rejectMessage) {
		this.rejectMessage = rejectMessage;
	}
	public NoApproverAction getNoApproverAction() {
		return noApproverAction;
	}
	public void setNoApproverAction(NoApproverAction noApproverAction) {
		this.noApproverAction = noApproverAction;
	}
}
