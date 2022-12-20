package com.igsl.customapproval;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.parser.JqlQueryParser;
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
	private static final JqlQueryParser JQL_PARSER = ComponentAccessor.getComponent(JqlQueryParser.class);
	
	@Override
	public JobRunnerResponse runJob(JobRunnerRequest request) {
		int successCount = 0;
		int errorCount = 0;
		SearchResults<Issue> list = null;
		String filter = CustomApprovalUtil.getJobFilter();
		try {
			Query q = JQL_PARSER.parseQuery(filter);
			LOGGER.debug("Issue filter: " + q.toString());
			LOGGER.debug("User: " + CustomApprovalUtil.getAdminUser());
			list = SEARCH_SERVICE.search(CustomApprovalUtil.getAdminUser(), q, PagerFilter.getUnlimitedFilter());
			LOGGER.debug("List size: " + list.getResults().size());
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
		} catch (Exception e) {
			return JobRunnerResponse.failed(e);
		}
		String msg;
		if (list != null && list.getResults().size() != 0) {
			msg = "Filter: [" + filter + "], Issues found: " + list.getResults().size() + ", transited: " + successCount;
			if (errorCount != 0) {
				msg += ", errors: " + errorCount + ", please check server log for details.";
			}
		} else {
			msg = "Filter: [" + filter + "], No issue found";
		}
		return JobRunnerResponse.success(msg);
	}

}
