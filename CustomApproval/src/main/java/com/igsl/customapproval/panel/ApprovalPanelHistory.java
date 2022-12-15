package com.igsl.customapproval.panel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.igsl.customapproval.PluginUtil;
import com.igsl.customapproval.data.ApprovalHistory;
import com.igsl.customapproval.data.ApprovalSettings;

/**
 * Wrapper for ApprovalHistory
 */
public class ApprovalPanelHistory {
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger LOGGER = Logger.getLogger(ApprovalPanelHistory.class);
	
	private String approver;
	private String approverDisplayName;
	private List<String> onBehalfOf;
	private List<String> onBehalfOfDisplayName;
	private boolean approved;
	private String decision;
	private Date approvedDate;
	private String approvedDateString;
	private boolean valid;
	
	public ApprovalPanelHistory(ApprovalHistory history, Issue issue, ApprovalSettings settings) {
		this.approver = history.getApprover();
		this.onBehalfOf = history.getOnBehalfOf();
		this.approved = history.getApproved();
		this.approvedDate = history.getApprovedDate();
		// Translate
		ApplicationUser approverUser = PluginUtil.getUserByKey(this.approver);
		if (approverUser != null) {
			this.approverDisplayName = approverUser.getDisplayName();
		} else {
			this.approverDisplayName = this.approver;
		}
		if (this.onBehalfOf != null) {
			this.onBehalfOfDisplayName = new ArrayList<>();
			for (String key : this.onBehalfOf) {
				ApplicationUser onBehalfOfUser = PluginUtil.getUserByKey(key);
				if (onBehalfOfUser != null) {
					this.onBehalfOfDisplayName.add(onBehalfOfUser.getDisplayName());
				} else {
					this.onBehalfOfDisplayName.add(key);
				}
			}
		} else {
			this.onBehalfOfDisplayName = null;
		}
		if (this.approvedDate != null) {
			this.approvedDateString = SDF.format(this.approvedDate);
		}
		this.decision = (this.approved)? "Approved" : "Rejected";
		Map<String, ApplicationUser> approverList = PluginUtil.getApproverList(issue, settings);
		for (String s : approverList.keySet()) {
			LOGGER.debug("Approver list: " + s);
		}
		LOGGER.debug("User: " + this.approver);
		this.valid = PluginUtil.isApprover(this.approver, approverList);
		if (!this.valid) {
			this.valid = (PluginUtil.isDelegate(this.approver, approverList).size() != 0);
		}
		LOGGER.debug("isValid: " + this.valid);
	}
	
	public String getApprover() {
		return approver;
	}
	public String getApproverDisplayName() {
		return approverDisplayName;
	}
	public List<String> getOnBehalfOf() {
		return onBehalfOf;
	}
	public List<String> getOnBehalfOfDisplayName() {
		return onBehalfOfDisplayName;
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
}
