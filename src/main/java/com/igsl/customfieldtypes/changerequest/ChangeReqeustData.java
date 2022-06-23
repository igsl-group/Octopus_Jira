package com.igsl.customfieldtypes.changerequest;

import java.util.Comparator;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.google.gson.Gson;

public class ChangeReqeustData implements Comparable<ChangeReqeustData> {

	private static Comparator<Long> nullSafeLongComparator = Comparator.nullsFirst(Long::compareTo); 

	// Only issueId is stored in database
	private Long issueId;
	
	// The other fields are calculated as issueId is updated
	private String issueKey;
	private String issueSummary;
	private String projectManager;

	public String getIssueKey() {
		return issueKey;
	}

	public String getIssueSummary() {
		return issueSummary;
	}

	public String getProjectManager() {
		return projectManager;
	}

	public Long getIssueId() {
		return issueId;
	}

	public void setIssueId(Long issueId) {
		this.issueId = issueId;
		this.projectManager = null;
		this.issueSummary = null;
		this.issueKey = null;
		// Update projectManager and issueSummary
		MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueId);
		if (issue != null) {
			this.projectManager = issue.getProjectObject().getLeadUserName();
			this.issueSummary = issue.getSummary();
			this.issueKey = issue.getKey();
		}
	}

	@Override
	public int compareTo(ChangeReqeustData o) {
		if (o == null) {
			return 1;
		}
		return nullSafeLongComparator.compare(this.issueId, o.issueId);
	}

	public static ChangeReqeustData fromString(String s) throws FieldValidationException {
		if (s == null || s.isEmpty()) {
			return null;
		}
		try {
			ChangeReqeustData data = new ChangeReqeustData();
			data.setIssueId(Long.parseLong(s));
			return data;
		} catch (Exception ex) {
			throw new FieldValidationException(ex.getMessage());
		}
	}
	
	@Override
	public String toString() {
		if (this.issueId != null) {
			return this.issueId.toString();
		}
		return null;
	}
	
	public String toReadableString() {
		if (this.issueId != null) {
			return this.issueId.toString();
		}
		return null;
	}
	
}
