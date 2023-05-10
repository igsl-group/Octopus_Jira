package com.igsl.customapproval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * Multiple instances that run at delegation start/end times to update approver list in issues
 * 
 * Setup:
 * - On delegation settings change, setup job at start and end time.
 * - If start time is in the past, run immediately.
 * 
 * Process:
 * - Search for issues that has custom approval enabled (ApprovalData not empty).
 * - Check approver list if it matches delegaton setting.
 * - If so, modify request participants based on delegation target and manual request participant.
 * 
 */
public class CustomApprovalDelegationJob implements JobRunner {

	public static final String PARAMETER_IS_START = "isStart";
	public static final String PARAMETER_DELEGATOR = "delegator";
	public static final String PARAMETER_TARGET = "target";
	
	private static final ObjectMapper OM = new ObjectMapper();
	private static final Logger LOGGER = Logger.getLogger(CustomApprovalDelegationJob.class);
	private static final UserManager USER_MANAGER = ComponentAccessor.getUserManager();
	private static final IssueManager ISSUE_MANAGER = ComponentAccessor.getIssueManager();
	private static final SearchService SEARCH_SERVICE = ComponentAccessor.getComponent(SearchService.class);
	private static final JqlQueryParser JQL_PARSER = ComponentAccessor.getComponent(JqlQueryParser.class);
	
	@Override
	public JobRunnerResponse runJob(JobRunnerRequest request) {
		LOGGER.debug("Adhoc job executes: " + request.getJobId());
		CustomField manualRequestParticipantField = CustomApprovalSetup.getManualRequestParticipantCustomField();
		CustomField requestParticipantField = CustomApprovalSetup.getRequestParticipantField();
		if (manualRequestParticipantField != null && requestParticipantField != null) {
			ObjectReader or = OM.readerFor(String.class);
			String msg = "";
			Map<String, Serializable> params = request.getJobConfig().getParameters();
			String delegatorKey = (String) params.get(PARAMETER_DELEGATOR);
			boolean isStart = (Boolean) params.get(PARAMETER_IS_START);
			String target = (String) params.get(PARAMETER_TARGET);
			LOGGER.debug("isStart: " + isStart + ", from: " + delegatorKey + ", to: " + target);
			SearchResults<Issue> list = null;
			String filter = CustomApprovalUtil.getDelegationFilter();
			try {
				Query q = JQL_PARSER.parseQuery(filter);
				LOGGER.debug("Issue filter: " + q.toString());
				list = SEARCH_SERVICE.search(CustomApprovalUtil.getAdminUser(), q, PagerFilter.getUnlimitedFilter());
				LOGGER.debug("List size: " + list.getResults().size());
				if (list.getResults().size() != 0) {
					int totalCount = 0;
					int successCount = 0;
					int failureCount = 0;
					for (Issue issue : list.getResults()) {
						MutableIssue mi = ISSUE_MANAGER.getIssueObject(issue.getKey());
						Map<String, ApplicationUser> approverList = CustomApprovalUtil.getApproverList(mi);
						if (approverList == null || !approverList.containsKey(delegatorKey)) {
							continue;
						}
						LOGGER.debug("Processing issue: " + issue.getKey());
						if (mi == null) {
							LOGGER.error("Unable to retrieve mutable issue: " + issue.getKey());
							failureCount++;
							continue;
						}
						List<ApplicationUser> requestParticipant = new ArrayList<>();
						requestParticipant.addAll(
								(List<ApplicationUser>) mi.getCustomFieldValue(requestParticipantField));
						if (requestParticipant != null) {
							for (ApplicationUser participant : requestParticipant) {
								LOGGER.debug("Original Request participant: " + participant);
							}
						}
						Map<String, ApplicationUser> approvers = CustomApprovalUtil.getApproverList(mi);
						if (approvers != null && approvers.containsKey(delegatorKey)) {
							totalCount++;
							ApplicationUser user = USER_MANAGER.getUserByKey(target);
							if (user == null) {
								failureCount++;
								LOGGER.error("Unable to find user by key: " + target);
								continue;
							}
							if (isStart) {
								// Add delegation targets as request participants
								requestParticipant.add(user);
								LOGGER.debug("Adding: " + target);
							} else {
								// Remove targets as request participants unless in manual request participants
								List<ApplicationUser> manualRequestParticipant = (List<ApplicationUser>) 
										mi.getCustomFieldValue(manualRequestParticipantField);
								boolean isManualAdded = false;
								if (manualRequestParticipant != null) {
									for (ApplicationUser u : manualRequestParticipant) {
										if (u.getKey().equals(user.getKey())) {
											isManualAdded = true;
											break;
										}
									}
								}
								if (!isManualAdded) {
									for (ApplicationUser u : requestParticipant) {
										if (u.getKey().equals(user.getKey())) {
											requestParticipant.remove(u);
											LOGGER.debug("Removing: " + target);
											break;
										}
									}
								}
							}
							// Update issue
							for (ApplicationUser u : requestParticipant) {
								LOGGER.debug("New Request participant: " + u.getKey());
							}
							mi.setCustomFieldValue(requestParticipantField, requestParticipant);
							ISSUE_MANAGER.updateIssue(
									CustomApprovalUtil.getAdminUser(), 
									mi, 
									EventDispatchOption.DO_NOT_DISPATCH, 
									false);
							LOGGER.debug("Issue updated: " + mi.getKey());
							successCount++;
						}
					}
					msg = "Success: " + successCount + ", Failure: " + failureCount + "/" + totalCount;
				} else {
					msg = "No issues found";
				}
			} catch (Exception e) {
				LOGGER.error("Adhoc job failed: " + request.getJobId(), e);
				return JobRunnerResponse.failed(e);
			}
			LOGGER.debug("Result: " + msg);
			return JobRunnerResponse.success(msg);
		} else {
			return JobRunnerResponse.failed(
					"Custom fields " + CustomApprovalUtil.REUQEST_PARTICIPANT_FIELD_NAME + 
					" and " + CustomApprovalUtil.MANUAL_REUQEST_PARTICIPANT_FIELD_NAME + 
					" not found");
		}
	}

}
