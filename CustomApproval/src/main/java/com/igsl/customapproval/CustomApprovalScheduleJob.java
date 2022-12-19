package com.igsl.customapproval;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.query.Query;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.igsl.customapproval.exception.InvalidWorkflowException;
import com.igsl.customapproval.exception.LockException;

public class CustomApprovalScheduleJob implements JobRunner {

	private static final Logger LOGGER = Logger.getLogger(CustomApprovalScheduleJob.class);
	private static final IssueManager ISSUE_MANAGER = ComponentAccessor.getIssueManager();
	private static final SearchService SEARCH_SERVICE = ComponentAccessor.getComponent(SearchService.class);
	
	@Override
	public JobRunnerResponse runJob(JobRunnerRequest request) {
		int successCount = 0;
		int errorCount = 0;
		SearchResults<Issue> list = null;
		JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
		JqlClauseBuilder search = 
				JqlQueryBuilder.newClauseBuilder()
					.not()
					.addEmptyCondition(CustomApprovalUtil.CUSTOM_FIELD_NAME);
		builder.where().addClause(search.buildClause());
		Query q = builder.buildQuery();
		try {
			list = SEARCH_SERVICE.search(CustomApprovalUtil.getAdminUser(), q, PagerFilter.getUnlimitedFilter());
			for (Issue issue : list.getResults()) {
				MutableIssue mi = ISSUE_MANAGER.getIssueObject(issue.getKey());
				try {
					if (CustomApprovalUtil.transitIssue(mi, CustomApprovalUtil.getAdminUser())) {
						LOGGER.debug("Transited issue: " + mi.getKey());
						successCount++;
					}
				} catch (WorkflowException | LockException | InvalidWorkflowException e) {
					LOGGER.error("Failed to transit issue " + mi.getKey(), e);
					errorCount++;
				}
			}
		} catch (SearchException e) {
			return JobRunnerResponse.failed(e);
		}
		String msg;
		if (list != null && list.getResults().size() != 0) {
			msg = "Issues found: " + list.getResults().size() + ", transited: " + successCount;
			if (errorCount != 0) {
				msg += ", errors: " + errorCount + ", please check server log for details.";
			}
		} else {
			msg = "No issue found";
		}
		return JobRunnerResponse.success(msg);
	}

}
