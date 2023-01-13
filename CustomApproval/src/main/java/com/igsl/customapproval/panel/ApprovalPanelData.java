package com.igsl.customapproval.panel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.igsl.customapproval.data.ApprovalSettings;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ApprovalPanelData {

	private boolean completed;
	private boolean approved;
	private String approvalName;
	private int approveCountTarget;
	private int rejectCountTarget;
	private int approveCount = 0;
	private int rejectCount = 0;
	
	private Map<String, ApprovalPanelHistory> history = new HashMap<>();
	private List<ApprovalPanelHistory> sortedHistory = new ArrayList<>();
	
	public ApprovalPanelData(ApprovalSettings settings) {
		this.approvalName = settings.getApprovalName();
		if (settings.isCompleted()) {
			this.completed = true;
			this.approved = settings.isApproved();
			this.approveCountTarget = (int) settings.getFinalApproveCountTarget();
			this.approveCount = (int) settings.getFinalApproveCount();
			this.rejectCountTarget = (int) settings.getFinalRejectCountTarget();
			this.rejectCount = (int) settings.getFinalRejectCount();
		} else {
			this.completed = false;
			this.approved = false;
			this.approveCountTarget = 0;
			this.rejectCountTarget = 0;
			this.approveCount = 0;
			this.rejectCount = 0;
		}
	}
	
	public Collection<ApprovalPanelHistory> getSortedHistory() {
		this.sortedHistory.clear();
		for (ApprovalPanelHistory item : history.values()) {
			this.sortedHistory.add(item);
		}
		Collections.sort(this.sortedHistory, new ApprovalPanelHistoryComparator());
		return this.sortedHistory;
	}
	
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
	public String getApprovalName() {
		return approvalName;
	}
	public void setApprovalName(String approvalName) {
		this.approvalName = approvalName;
	}
	public boolean isCompleted() {
		return completed;
	}
	public boolean isApproved() {
		return approved;
	}
	public Map<String, ApprovalPanelHistory> getHistory() {
		return history;
	}
}
