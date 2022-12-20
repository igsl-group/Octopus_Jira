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
	private static final String PARAM_SAVE = "Save";
	
	private long delegationHistoryRetainDays = CustomApprovalUtil.DEFAULT_RETAIN_DAYS;
	private List<String> adminGroups = Arrays.asList(CustomApprovalUtil.DEFAULT_ADMIN_GROUP);
	private long jobFrequency = CustomApprovalUtil.DEFAULT_JOB_FREQUENCY;
	
	@Inject
	protected CustomApprovalConfig(@ComponentImport ManagedConfigurationItemService managedConfigurationItemService) {
		super(managedConfigurationItemService);
	}

	public long getDelegationHistoryRetainDays() {
		return delegationHistoryRetainDays;
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
	
	@Override
	protected String doExecute() throws Exception {
		// Load settings
		this.delegationHistoryRetainDays = CustomApprovalUtil.getDelegationHistoryRetainDays();
		this.adminGroups = CustomApprovalUtil.getDelegationAdminGroups();
		this.jobFrequency = CustomApprovalUtil.getJobFrequency();
		HttpServletRequest req = getHttpRequest();
		if (req.getParameter(PARAM_SAVE) != null) {
			try {
				this.delegationHistoryRetainDays = Long.parseLong(req.getParameter(PARAM_RETAIN_DAYS));
				CustomApprovalUtil.setDelegationHistoryRetainDays(this.delegationHistoryRetainDays);
			} catch (Exception ex) {
				LOGGER.error("Failed to parse parameters", ex);
			}
			try {
				this.adminGroups = OM.readValue(req.getParameter(PARAM_ADMIN_GROUPS), new TypeReference<List<String>>() {});
				CustomApprovalUtil.setDelegationAdminGroups(this.adminGroups);
			} catch (Exception ex) {
				LOGGER.error("Failed to parse parameters", ex);
			}
			try {
				this.jobFrequency = Long.parseLong(req.getParameter(PARAM_JOB_FREQUENCY));
				// Create scheduled job
				CustomApprovalUtil.setJobFrequency(this.jobFrequency);
				CustomApprovalUtil.createScheduledJob(this.jobFrequency);				
			} catch (Exception ex) {
				LOGGER.error("Failed to parse parameters", ex);
			}
		}
		return JiraWebActionSupport.INPUT;
	}

}
