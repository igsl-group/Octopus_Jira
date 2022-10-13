package com.igsl.ldapuserattributes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.admin.customfields.AbstractEditConfigurationItemAction;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import com.atlassian.scheduler.status.JobDetails;

@Named
public class LDAPUserAttributesAction extends AbstractEditConfigurationItemAction {

	private static final long serialVersionUID = 1L;
	
	private static Logger LOGGER = LoggerFactory.getLogger(LDAPUserAttributesAction.class);
	
	// Custom field configuration URL
	private static final String PAGE_URL = "/secure/admin/plugins/handler/LDAPUserAttributesAction.jspa";

	private static ObjectMapper OM = new ObjectMapper();
	private static ObjectReader READER = OM.reader(LDAPUserAttributesConfigData.class);
	private static ObjectWriter WRITER = OM.writerWithType(LDAPUserAttributesConfigData.class);
	
	// Web action key words
	private static final String DATA_KEY = "LDAPUserAttributesConfig";
	private static final String SAVE = "Save";
	
	private static final String PARAM_PROVIDER_URL = "providerURL";
	private static final String PARAM_BASEDN = "baseDN";
	private static final String PARAM_SCOPE = "scope";
	private static final String PARAM_FILTER = "filter";
	private static final String PARAM_FREQUENCY = "frequency";
	private static final String PARAM_FREQUENCY_MULTIPLIER = "frequencyMultiplier";
	private static final String PARAM_USERNAME = "userName";
	private static final String PARAM_PASSWORD = "password";
	private static final String PARAM_SOURCE = "source";
	private static final String PARAM_TARGET = "target";
	private static final String PARAM_USERNAME_ATTRIBUTE = "userNameAttribute";
	private static final String PARAM_PAGE_SIZE = "pageSize";
	private static final String PARAM_REFERRAL = "referral";
	private static final String PARAM_HOUR = "hour";
	private static final String PARAM_MINUTE = "minute";
	private static final String PARAM_SECOND = "second";
	private static final String PARAM_IGNORE_EXPIRED_USER = "ignoreExpiredUser";
	private static final String PARAM_EXPIRES_ATTRIBUTE = "expiresAttribute";
	
	// Magic word to stay on same page
	private static final String INPUT = "input";
	
	protected ManagedConfigurationItemService service;
	
	@Inject
	protected LDAPUserAttributesAction(@ComponentImport ManagedConfigurationItemService managedConfigurationItemService) {
		super(managedConfigurationItemService);
		LOGGER.debug("constructor: " + managedConfigurationItemService);
		this.service = managedConfigurationItemService;
	}
	
	public static void setData(LDAPUserAttributesConfigData data, String key) {
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		try {
			if (data == null) {
				settings.remove(key);
			} else {
				String s = WRITER.writeValueAsString(data);
				LOGGER.debug("Save JSON to " + key + ": <" + s + ">");
				settings.put(key, s);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to convert LDAPUserAttributesConfigData to JSON", e);
		}
	}
	
	public static LDAPUserAttributesConfigData getData() {
		LDAPUserAttributesConfigData data = null;
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		Object o = settings.get(DATA_KEY);
		if (o != null) {
			try {
				data = READER.readValue(o.toString());
			} catch (IOException e) {
				LOGGER.error("Failed to convert LDAPUserAttributesConfigData from JSON", e);
			}
		}
		if (data == null) {
			data = new LDAPUserAttributesConfigData();
		}
		return data;
	}
	
	private List<String> parseData(HttpServletRequest req, LDAPUserAttributesConfigData data) {
		if (this.getHttpRequest().getParameter(SAVE) == null) {
			return null;
		}
		List<String> errors = new ArrayList<String>();
		String[] providerURL = this.getHttpRequest().getParameterValues(PARAM_PROVIDER_URL);
		if (providerURL != null && providerURL.length == 1) {
			data.setProviderURL(providerURL[0]);
		} else {
			errors.add("Please specify provider URL");
		}
		String[] baseDN = this.getHttpRequest().getParameterValues(PARAM_BASEDN);
		if (baseDN != null && baseDN.length == 1) {
			data.setBaseDN(baseDN[0]);
		} else {
			errors.add("Please specify base DN");
		}
		String[] scope = this.getHttpRequest().getParameterValues(PARAM_SCOPE);
		if (scope != null && scope.length == 1) {
			try {
				int sc = Integer.parseInt(scope[0]);
				if (sc >= 0 && sc <= 2) {
					data.setScope(sc);
				} else {
					errors.add("Please specify a valid scope");
				}
			} catch (NumberFormatException nfex) {
				errors.add("Please specify a valid scope");
			}
		} else {
			errors.add("Please specify scope");
		}
		String[] userNameAttribute = this.getHttpRequest().getParameterValues(PARAM_USERNAME_ATTRIBUTE);
		if (userNameAttribute != null && userNameAttribute.length == 1) {
			data.setUserNameAttribute(userNameAttribute[0]);
		} else {
			errors.add("Please specify username attribute");
		}
		String[] filter = this.getHttpRequest().getParameterValues(PARAM_FILTER);
		if (filter != null && filter.length == 1) {
			data.setFilter(filter[0]);
		} else {
			errors.add("Please specify filter");
		}
		String[] pageSize = this.getHttpRequest().getParameterValues(PARAM_PAGE_SIZE);
		if (pageSize != null && pageSize.length == 1) {
			try {
				int ps = Integer.parseInt(pageSize[0]);
				if (ps >= 0) {
					data.setFrequency(ps);
				} else {
					errors.add("Please specify a valid page size");
				}
			} catch (NumberFormatException nfex) {
				errors.add("Please specify a valid page size");
			}
		} else {
			errors.add("Please specify page size");
		}
		String[] ignoreExpiredUser = this.getHttpRequest().getParameterValues(PARAM_IGNORE_EXPIRED_USER);
		if (ignoreExpiredUser != null && ignoreExpiredUser.length == 1) {
			boolean ref = Boolean.parseBoolean(ignoreExpiredUser[0]);
			data.setIgnoreExpiredUser(ref);
		} else {
			errors.add("Please specify ignore expired user");
		}
		String[] expiresAttribute = this.getHttpRequest().getParameterValues(PARAM_EXPIRES_ATTRIBUTE);
		if (expiresAttribute != null && expiresAttribute.length == 1) {
			data.setExpiresAttribute(expiresAttribute[0]);
		} else {
			errors.add("Please specify ignore expired user");
		}
		String[] referral = this.getHttpRequest().getParameterValues(PARAM_REFERRAL);
		if (referral != null && referral.length == 1) {
			boolean ref = Boolean.parseBoolean(referral[0]);
			data.setReferral(ref);
		} else {
			errors.add("Please specify referral");
		}
		String[] frequency = this.getHttpRequest().getParameterValues(PARAM_FREQUENCY);
		if (frequency != null && frequency.length == 1) {
			try {
				long fre = Long.parseLong(frequency[0]);
				if (fre >= 0) {
					data.setFrequency(fre);
				} else {
					errors.add("Please specify a valid frequency");
				}
			} catch (NumberFormatException nfex) {
				errors.add("Please specify a valid frequency");
			}
		} else {
			errors.add("Please specify frequency");
		}
		String[] hour = this.getHttpRequest().getParameterValues(PARAM_HOUR);
		if (hour != null && hour.length == 1) {
			try {
				int h = Integer.parseInt(hour[0]);
				if (h >= 0 && h <= 23) {
					data.setHour(hour[0]);
				} else {
					errors.add("Please specify a valid hour");
				}
			} catch (NumberFormatException nfex) {
				errors.add("Please specify a valid hour");
			}
		} else {
			errors.add("Please specify hour");
		}
		String[] minute = this.getHttpRequest().getParameterValues(PARAM_MINUTE);
		if (minute != null && minute.length == 1) {
			try {
				int m = Integer.parseInt(minute[0]);
				if (m >= 0 && m <= 59) {
					data.setMinute(minute[0]);
				} else {
					errors.add("Please specify a valid minute");
				}
			} catch (NumberFormatException nfex) {
				errors.add("Please specify a valid minute");
			}
		} else {
			errors.add("Please specify minute");
		}
		String[] second = this.getHttpRequest().getParameterValues(PARAM_SECOND);
		if (second != null && second.length == 1) {
			try {
				int s = Integer.parseInt(second[0]);
				if (s >= 0 && s <= 59) {
					data.setSecond(second[0]);
				} else {
					errors.add("Please specify a valid second");
				}
			} catch (NumberFormatException nfex) {
				errors.add("Please specify a valid second");
			}
		} else {
			errors.add("Please specify second");
		}
		String[] frequencyMultiplier = this.getHttpRequest().getParameterValues(PARAM_FREQUENCY_MULTIPLIER);
		if (frequencyMultiplier != null && frequencyMultiplier.length == 1) {
			try {
				long fre = Long.parseLong(frequencyMultiplier[0]);
				if (fre >= 0) {
					data.setFrequencyMultiplier(fre);
				} else {
					errors.add("Please specify a valid frequency multiplier");
				}
			} catch (NumberFormatException nfex) {
				errors.add("Please specify a valid frequency multiplier");
			}
		} else {
			errors.add("Please specify frequency multiplier");
		}
		String[] userName = this.getHttpRequest().getParameterValues(PARAM_USERNAME);
		if (userName != null && userName.length == 1) {
			data.setUserName(userName[0]);
		} else {
			errors.add("Please specify user name");
		}
		String[] password = this.getHttpRequest().getParameterValues(PARAM_PASSWORD);
		if (password != null && password.length == 1) {
			String originalEncryptedPassword = getData().getEncryptedPassword();
			if (!password[0].equals(originalEncryptedPassword)) {
				String encryptedPassword = null;
				try {
					encryptedPassword = PasswordScrambler.scramble(password[0]);
				} catch (Exception ex) {
					errors.add("Unable to encrypt password");
				}
				data.setEncryptedPassword(encryptedPassword);
			} else {
				data.setEncryptedPassword(originalEncryptedPassword);
			}
		} else {
			errors.add("Please specify password");
		}
		data.getAttributeMap().clear();
		String[] sourceList = this.getHttpRequest().getParameterValues(PARAM_SOURCE);
		String[] targetList = this.getHttpRequest().getParameterValues(PARAM_TARGET);
		if (sourceList != null) {
			for (int i = 0; i < sourceList.length; i++) {
				if (targetList != null && targetList.length > i) {
					data.getAttributeMap().put(sourceList[i], targetList[i]);
				} else {
					data.getAttributeMap().put(sourceList[i], sourceList[i]);
				}
			}
		} else {
			errors.add("Please specify source attributes");
		}
		String s = null;
		try {
			s = OM.writeValueAsString(data);
		} catch (Exception ex) {
			LOGGER.error("parseData failed to convert to JSON", ex);
		}
		LOGGER.debug("parseData JSON <" + s + ">");
		return errors;
	}
	
	private void reportStackTrace(Throwable t) {
		if (t != null) {
			addErrorMessage(t.getClass().getCanonicalName() + ": " + t.getMessage());
			StackTraceElement[] stack = t.getStackTrace();
			for (StackTraceElement e : stack) {
				addErrorMessage(e.getClassName() + "." + e.getMethodName() + "() [" + e.getFileName() + "@" + e.getLineNumber() + "]");
			}
			if (t.getCause() != null) {
				reportStackTrace(t.getCause());
			}
		}
	}
	
	private void testConnection(LDAPUserAttributesConfigData data) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String pwd = "";
		try {
			pwd = PasswordScrambler.unscramble(data.getEncryptedPassword());
			String[] attrs = data.getAttributeMap().keySet().toArray(new String[0]);
			try {
				data.getLastTestResults().clear();
				data.getLastTestResults().add("Test Connection executed on " + sdf.format(new Date()));
				Map<String, Map<String, List<String>>> list = LDAPUserAttributes.getLDAPUsers(data);
				data.getLastTestResults().add("LDAP users found: " + list.size());
//				data.getLastTestResults().add("User List: ");
//				for (Map.Entry<String, Map<String, List<String>>> user : list.entrySet()) {
//					StringBuilder sb = new StringBuilder();
//					sb.append(user.getKey()).append(": ");
//					for (Map.Entry<String, List<String>> attr : user.getValue().entrySet()) {
//						String v = OM.writeValueAsString(attr.getValue());
//						sb.append(attr.getKey()).append(" = ").append(v).append("; ");
//					}
//					data.getLastTestResults().add(sb.toString());
//				}
				// Find matching Jira user
				int jiraUserFound = 0;
				UserSearchService uss = ComponentAccessor.getUserSearchService();
				for (Map.Entry<String, Map<String, List<String>>> entry : list.entrySet()) {
					Map<String, List<String>> attributes = entry.getValue();
					List<String> userName = attributes.get(data.getUserNameAttribute());
					if (userName != null && userName.size() == 1) {
						ApplicationUser jiraUser = uss.getUserByName(ComponentAccessor.getComponent(JiraServiceContext.class), userName.get(0));
						if (jiraUser == null) {
							data.getLastTestResults().add("Cannot find Jira user [" + userName.get(0) + "]");
						} else {
							jiraUserFound++;
						}
					}
				}
				data.getLastTestResults().add("Matching Jira users found: " + jiraUserFound);
				setData(data, DATA_KEY);
			} catch (Exception ex) {
				reportStackTrace(ex);
			}
		} catch (Exception e) {
			reportStackTrace(e);
		}
	}
	
	protected void doValidation() {
		LOGGER.debug("doValidation");
		LDAPUserAttributesConfigData data = getData();
		List<String> errors = parseData(this.getHttpRequest(), data);
		if (errors != null) {
			if (errors.size() == 0) {
				testConnection(data);
			} else {
				for (String e : errors) {
					addErrorMessage(e);
				}
			}
		}
	}

	private void setSchedule(LDAPUserAttributesConfigData data) throws Exception {
		SchedulerService schedulerService = ComponentAccessor.getComponent(SchedulerService.class);
		JobRunnerKey key = JobRunnerKey.of(LDAPUserAttributeSyncJob.class.getCanonicalName());
		// Unregister
		List<JobDetails> jobList = schedulerService.getJobsByJobRunnerKey(key);
		if (jobList != null) { 
			for (JobDetails job : jobList) {
				schedulerService.unscheduleJob(job.getJobId());
			}
		}
		schedulerService.unregisterJobRunner(key);
		if (data.getFrequency() != 0) {
			// Register
			schedulerService.registerJobRunner(key, new LDAPUserAttributeSyncJob());
			Calendar nextRun = Calendar.getInstance();
			nextRun.set(Calendar.HOUR_OF_DAY, Integer.parseInt(data.getHour()));
			nextRun.set(Calendar.MINUTE, Integer.parseInt(data.getMinute()));
			nextRun.set(Calendar.SECOND, Integer.parseInt(data.getSecond()));
			Schedule schedule = Schedule.forInterval(data.getFrequency() * data.getFrequencyMultiplier(), nextRun.getTime());
			JobConfig jobConfig = JobConfig
					.forJobRunnerKey(key)
					.withSchedule(schedule)
					.withRunMode(RunMode.RUN_ONCE_PER_CLUSTER);
					//.withParameters(ImmutableMap.<String, Serializable>of("SUBSCRIPTION_ID", subscriptionId));
			JobId jobId = JobId.of(LDAPUserAttributeSyncJob.class.getCanonicalName());
			schedulerService.scheduleJob(jobId, jobConfig);
		}
	}
	
	// Expected return value is name of associated view
	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		LDAPUserAttributesConfigData data = getData();
		parseData(getHttpRequest(), data);
		String save = getHttpRequest().getParameter(SAVE);
		if (save != null) {
			LOGGER.debug("doExecute - save");
			setData(data, DATA_KEY);
			try {
				setSchedule(data);
			} catch (Exception ex) {
				LOGGER.error("schedule failed", ex);
				throw ex;
			}
			setReturnUrl(PAGE_URL);
	    	return getRedirect(INPUT);
        }
    	return INPUT;
	}
}
