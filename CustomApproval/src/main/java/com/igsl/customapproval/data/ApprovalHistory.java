package com.igsl.customapproval.data;

import java.util.Date;

public class ApprovalHistory {
	
	public static final String SYSTEM = "system";
	
	private String approver;	// SYSTEM means approved/rejected by noApproverAction
	private String delegated;
	private boolean approved;
	private Date approvedDate;
	
	public String getApprover() {
		return approver;
	}
	public void setApprover(String approver) {
		this.approver = approver;
	}
	public boolean getApproved() {
		return approved;
	}
	public void setApproved(boolean approved) {
		this.approved = approved;
	}
	public Date getApprovedDate() {
		return approvedDate;
	}
	public void setApprovedDate(Date approvedDate) {
		this.approvedDate = approvedDate;
	}
	public String getDelegated() {
		return delegated;
	}
	public void setDelegated(String delegated) {
		this.delegated = delegated;
	}
}
