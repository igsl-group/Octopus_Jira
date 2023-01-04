package com.igsl.ldapuserattributes;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import com.atlassian.scheduler.status.JobDetails;

@Component
public class LDAPUserAttributesSetup implements InitializingBean, DisposableBean {

	private static final Logger LOGGER = Logger.getLogger(LDAPUserAttributesSetup.class);
	private static final String DATA_KEY = "LDAPUserAttributesConfig";
	
	private static ObjectMapper OM = new ObjectMapper();
	private static ObjectReader READER = OM.reader(LDAPUserAttributesConfigData.class);
	private static ObjectWriter WRITER = OM.writerWithType(LDAPUserAttributesConfigData.class);

	private EventPublisher eventPublisher;
	
	@Autowired
	public LDAPUserAttributesSetup(@JiraImport EventPublisher eventPublisher) {
	    this.eventPublisher = eventPublisher;
	}
	
	public static void setData(LDAPUserAttributesConfigData data) {
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		try {
			if (data == null) {
				settings.remove(DATA_KEY);
			} else {
				String s = WRITER.writeValueAsString(data);
				LOGGER.debug("Save JSON to " + DATA_KEY + ": <" + s + ">");
				settings.put(DATA_KEY, s);
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
	
	public static void setSchedule(LDAPUserAttributesConfigData data) throws Exception {
		LOGGER.debug("setSchedule");
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
			LOGGER.debug("Registering");
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
	
	@EventListener
    public void onPluginEnabled(PluginEnabledEvent event) {
		LOGGER.debug("onPluginEnabled");
		LDAPUserAttributesConfigData data = getData();
		if (data != null) {
			try {
				LOGGER.debug("Setting schedule");
				setSchedule(data);
			} catch (Exception ex) {
				LOGGER.error("Failed to set schedule", ex);
			}
		}
	}
	
	@Override
	public void destroy() throws Exception {
		this.eventPublisher.unregister(this);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.eventPublisher.register(this);
	}

}
