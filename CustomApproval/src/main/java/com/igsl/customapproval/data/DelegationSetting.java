package com.igsl.customapproval.data;

import java.util.Date;

import com.atlassian.jira.user.ApplicationUser;

/**
 * Delegation settings. 
 */
public class DelegationSetting {

	private String fromUser;
	private String delegateToUser;
	private Date startDate;
	private Date endDate;
	private ApplicationUser lastModifiedBy;
	private Date lastModifiedDate;
	
	public String getFromUser() {
		return fromUser;
	}
	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}
	public String getDelegateToUser() {
		return delegateToUser;
	}
	public void setDelegateToUser(String delegateToUser) {
		this.delegateToUser = delegateToUser;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public ApplicationUser getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(ApplicationUser lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	
}
