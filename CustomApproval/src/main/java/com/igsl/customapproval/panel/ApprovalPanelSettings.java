package com.igsl.customapproval.panel;

public class ApprovalPanelSettings {
	
	private int approveCountTarget;
	private int rejectCountTarget;
	private int approveCount = 0;
	private int rejectCount = 0;
	
	public int getApproveCountTarget() {
		return approveCountTarget;
	}
	public void setApproveCountTarget(double approveCountTarget) {
		this.approveCountTarget = (int) approveCountTarget;
	}
	public int getRejectCountTarget() {
		return rejectCountTarget;
	}
	public void setRejectCountTarget(double rejectCountTarget) {
		this.rejectCountTarget = (int) rejectCountTarget;
	}
	public int getApproveCount() {
		return approveCount;
	}
	public void setApproveCount(double approveCount) {
		this.approveCount = (int) approveCount;
	}
	public int getRejectCount() {
		return rejectCount;
	}
	public void setRejectCount(double rejectCount) {
		this.rejectCount = (int) rejectCount;
	}
}
