package com.igsl.customapproval.panel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.igsl.customapproval.CustomApprovalUtil;
import com.igsl.customapproval.data.ApprovalHistory;
import com.igsl.customapproval.data.ApprovalSettings;

/**
 * Wrapper for ApprovalHistory
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ApprovalPanelHistory {
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger LOGGER = Logger.getLogger(ApprovalPanelHistory.class);
	
	private String approver;
	private String approverDisplayName;
	private String delegated;
	private String delegatedDisplayName;
	private boolean approved;
	private String decision;
	private Date approvedDate;
	private String approvedDateString;
	private boolean valid;
	
	public ApprovalPanelHistory(ApprovalHistory history, Issue issue, ApprovalSettings settings) {
		// history.getApprover() if null, means auto-approved/rejected
		this.approver = history.getApprover();
		this.delegated = history.getDelegated();
		this.approved = history.getApproved();
		this.approvedDate = history.getApprovedDate();
		if (ApprovalHistory.SYSTEM.equals(this.approver)) {
			this.approverDisplayName = 
					"Auto-" + (this.approved? "approved" : "rejected") + 
					" because no approver is defined";
			this.delegatedDisplayName = null;
		} else {
			// Translate
			ApplicationUser approverUser = CustomApprovalUtil.getUserByKey(this.approver);
			if (approverUser != null) {
				this.approverDisplayName = approverUser.getDisplayName();
			} else {
				this.approverDisplayName = this.approver;
			}
			if (this.delegated != null) {
				ApplicationUser delegatedUser = CustomApprovalUtil.getUserByKey(this.delegated);
				if (delegatedUser != null) {
					this.delegatedDisplayName = delegatedUser.getDisplayName();
				} else {
					this.delegatedDisplayName = this.delegated;
				}
			} else {
				this.delegatedDisplayName = null;
			}
		}
		if (this.approvedDate != null) {
			this.approvedDateString = SDF.format(this.approvedDate);
		} else {
			this.approvedDateString = "";
		}
		if (this.approvedDate != null) {
			this.decision = (this.approved)? "Approved" : "Rejected";
		} else {
			this.decision = "Pending";
		}
		Map<String, ApplicationUser> approverList = CustomApprovalUtil.getApproverList(issue, settings);
		for (String s : approverList.keySet()) {
			LOGGER.debug("Approver list: " + s);
		}
		LOGGER.debug("User: " + this.approver);
		if (settings.isCompleted()) {
			if (ApprovalHistory.SYSTEM.equals(this.approver)) {
				this.valid = true;
			} else {
				this.valid = CustomApprovalUtil.isApprover(this.approver, settings.getFinalApproverList());
			}
		} else {
			this.valid = CustomApprovalUtil.isApprover(this.approver, approverList);
		}
		LOGGER.debug("isValid: " + this.valid);
	}
	
	public String getApprover() {
		return approver;
	}
	public String getApproverDisplayName() {
		return approverDisplayName;
	}
	public boolean getApproved() {
		return approved;
	}
	public String getDecision() {
		return decision;
	}
	public Date getApprovedDate() {
		return approvedDate;
	}
	public String getApprovedDateString() {
		return approvedDateString;
	}
	public boolean isValid() {
		return valid;
	}
	public String getDelegated() {
		return delegated;
	}
	public String getDelegatedDisplayName() {
		return delegatedDisplayName;
	}
}
