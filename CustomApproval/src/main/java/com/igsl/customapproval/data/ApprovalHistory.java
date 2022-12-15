package com.igsl.customapproval.data;

import java.util.Date;
import java.util.List;

public class ApprovalHistory {
	
	private String approver;
	private List<String> onBehalfOf;
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
	public List<String> getOnBehalfOf() {
		return onBehalfOf;
	}
	public void setOnBehalfOf(List<String> onBehalfOf) {
		this.onBehalfOf = onBehalfOf;
	}
}
