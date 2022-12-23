package com.igsl.customapproval.rest;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igsl.customapproval.CustomApprovalUtil;
import com.igsl.customapproval.data.ApprovalData;
import com.igsl.customapproval.data.ApprovalHistory;
import com.igsl.customapproval.data.ApprovalSettings;
import com.igsl.customapproval.panel.ApprovalPanelData;
import com.igsl.customapproval.panel.ApprovalPanelHistory;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomApprovalREST {

	private static final Logger LOGGER = Logger.getLogger(CustomApprovalREST.class);
	private static final IssueManager ISSUE_MANAGER = ComponentAccessor.getIssueManager();
	private static final ObjectMapper OM = new ObjectMapper();
	
	private static final String APPROVE_LINK = "/rest/igsl/latest/customApprove";
	private static final String REJECT_LINK = "/rest/igsl/latest/customReject";
	
	@POST
	@Path("/getApprovalData")
	public Response getApprovalData(String issueKey) {
		ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		Issue issue = ISSUE_MANAGER.getIssueObject(issueKey);
		if (issue != null) {
			CustomApprovalRESTData result = new com.igsl.customapproval.rest.CustomApprovalRESTData();
			Collection<ApprovalPanelData> data = CustomApprovalUtil.getPanelData(issue);
			result.setData(data);
			// Check if we need approve/reject buttons
			boolean showApprove = CustomApprovalUtil.hasButton(issue, user, true);
			boolean showReject = CustomApprovalUtil.hasButton(issue, user, false);
			if (showApprove) {
				result.setApproveLink(APPROVE_LINK);
			}
			if (showReject) {
				result.setRejectLink(REJECT_LINK);
			}
			return Response.ok().entity(result).build();
		}
		return Response.ok().build();
	}
	
	@POST
	@Path("/customApprove")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response approve(String issueKey) {
		ApplicationUser user = CustomApprovalUtil.getCurrentUser();
		MutableIssue issue = CustomApprovalUtil.getIssue(issueKey);
		LOGGER.debug("Approve issue " + issue + " by User: " + user);
		try {
			CustomApprovalUtil.approve(issue, user, true);
			return Response.ok().build();
		} catch (Exception ex) {
			LOGGER.error("Failed to approve issue " + issueKey + " by user " + user, ex);
			return Response.serverError().build();
		}
	}

	@POST
	@Path("/customReject")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response reject(String issueKey) {
		ApplicationUser user = CustomApprovalUtil.getCurrentUser();
		MutableIssue issue = CustomApprovalUtil.getIssue(issueKey);
		LOGGER.debug("Reject issue " + issue + " by User: " + user);
		try {
			CustomApprovalUtil.approve(issue, user, false);
			return Response.ok().build();
		} catch (Exception ex) {
			LOGGER.error("Failed to reject issue " + issueKey + " by user " + user, ex);
			return Response.serverError().build();
		}
	}
	
}
