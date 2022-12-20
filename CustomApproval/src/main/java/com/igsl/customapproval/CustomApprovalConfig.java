package com.igsl.customapproval;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.customfields.AbstractEditConfigurationItemAction;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Named
public class CustomApprovalConfig extends AbstractEditConfigurationItemAction {

	private static final long serialVersionUID = 1L;
	private static final ObjectMapper OM = new ObjectMapper();
	private static final Logger LOGGER = Logger.getLogger(CustomApprovalConfig.class);

	private static final String PARAM_RETAIN_DAYS = "retainDays";
	private static final String PARAM_ADMIN_GROUPS = "adminGroups";
	private static final String PARAM_JOB_FREQUENCY = "jobFrequency";
	private static final String PARAM_JOB_FILTER = "jobFilter";
	private static final String PARAM_SAVE = "Save";
	
	private long delegationHistoryRetainDays = CustomApprovalUtil.DEFAULT_RETAIN_DAYS;
	private List<String> adminGroups = Arrays.asList(CustomApprovalUtil.DEFAULT_ADMIN_GROUP);
	private long jobFrequency = CustomApprovalUtil.DEFAULT_JOB_FREQUENCY;
	private String jobFilter = CustomApprovalUtil.DEFAULT_JOB_FILTER;
	private String newFilter = null;
	private List<String> issuesFound = null;
	
	@Inject
	protected CustomApprovalConfig(@ComponentImport ManagedConfigurationItemService managedConfigurationItemService) {
		super(managedConfigurationItemService);
	}

	public long getDelegationHistoryRetainDays() {
		return delegationHistoryRetainDays;
	}
	
	public String getJobFilter() {
		return jobFilter;
	}

	public String getAdminGroupsAsJSON() {
		try {
			return OM.writeValueAsString(this.adminGroups);
		} catch (Exception ex) {
			return null;
		}
	}

	public long getJobFrequency() {
		return jobFrequency;
	}
	
	public String getDefaultAdminGroups() {
		return CustomApprovalUtil.DEFAULT_ADMIN_GROUP;
	}
	
	public long getDefaultDelegationHistoryRetainDays() {
		return CustomApprovalUtil.DEFAULT_RETAIN_DAYS;
	}
	
	public long getDefaultJobFrequency() {
		return CustomApprovalUtil.DEFAULT_JOB_FREQUENCY;
	}
	
	public String getDefaultJobFitler() {
		return CustomApprovalUtil.DEFAULT_JOB_FILTER;
	}
	
	public String getIssuesFound() {
		final String DELIMITER = ", ";
		StringBuilder result = new StringBuilder("");
		if (this.newFilter != null) {
			result.append("With filter: ").append(this.newFilter).append("\n");
		}
		if (this.issuesFound != null) {
			StringBuilder issueList = new StringBuilder();
			if (this.issuesFound.size() != 0) {
				for (String s : this.issuesFound) {
					issueList.append(DELIMITER).append(s);
				}
				issueList.delete(0, DELIMITER.length());
			} else {
				issueList.append("None");
			}
			result.append("Issues found: ").append(issueList);
		}
		return result.toString();
	}
	
	@Override
	protected String doExecute() throws Exception {
		// Load settings
		this.delegationHistoryRetainDays = CustomApprovalUtil.getDelegationHistoryRetainDays();
		this.adminGroups = CustomApprovalUtil.getDelegationAdminGroups();
		this.jobFrequency = CustomApprovalUtil.getJobFrequency();
		this.jobFilter = CustomApprovalUtil.getJobFilter();
		this.issuesFound = CustomApprovalUtil.setJobFilter(this.jobFilter);
		this.newFilter = null;
		HttpServletRequest req = getHttpRequest();
		if (req.getParameter(PARAM_SAVE) != null) {
			try {
				this.delegationHistoryRetainDays = Long.parseLong(req.getParameter(PARAM_RETAIN_DAYS));
				CustomApprovalUtil.setDelegationHistoryRetainDays(this.delegationHistoryRetainDays);
			} catch (Exception ex) {
				LOGGER.error(ex);
				this.delegationHistoryRetainDays = CustomApprovalUtil.getDelegationHistoryRetainDays();
				this.addErrorMessage("Unable to set Retention Period for Expired Delegation History");
				this.addErrorMessage(ex.getMessage());
			}
			try {
				this.adminGroups = OM.readValue(req.getParameter(PARAM_ADMIN_GROUPS), new TypeReference<List<String>>() {});
				CustomApprovalUtil.setDelegationAdminGroups(this.adminGroups);
			} catch (Exception ex) {
				LOGGER.error(ex);
				this.adminGroups = CustomApprovalUtil.getDelegationAdminGroups();
				this.addErrorMessage("Unable to set Delegation Administrator User Group");
				this.addErrorMessage(ex.getMessage());
			}
			try {
				this.jobFrequency = Long.parseLong(req.getParameter(PARAM_JOB_FREQUENCY));
				// Create scheduled job
				CustomApprovalUtil.setJobFrequency(this.jobFrequency);
			} catch (Exception ex) {
				LOGGER.error(ex);
				this.jobFrequency = CustomApprovalUtil.getJobFrequency();
				this.addErrorMessage("Unable to set Approval Check Scheduled Job Frequency");
				this.addErrorMessage(ex.getMessage());
			}
			try {
				this.newFilter = req.getParameter(PARAM_JOB_FILTER);
				this.issuesFound = CustomApprovalUtil.setJobFilter(this.newFilter);
				this.jobFilter = this.newFilter;
			} catch (Exception ex) {
				LOGGER.error(ex);
				this.jobFilter = CustomApprovalUtil.getJobFilter();
				this.addErrorMessage("Unable to set Approval Check Scheduled Job Filter");
				this.addErrorMessage(ex.getMessage());
			}
			CustomApprovalUtil.createScheduledJob(this.jobFrequency);
		}
		return JiraWebActionSupport.INPUT;
	}

}
