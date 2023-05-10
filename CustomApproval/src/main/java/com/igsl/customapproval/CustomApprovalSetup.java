package com.igsl.customapproval;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemBuilder;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.igsl.customapproval.data.DelegationSetting;
import com.igsl.customapproval.delegation.DelegationUtil;

@Component
public class CustomApprovalSetup implements InitializingBean, DisposableBean {
	
	private static final Logger LOGGER = Logger.getLogger(CustomApprovalSetup.class);
	
	private static final String GENERIC_EVENT = "Generic Event";
	private static final String DELEGATOR_CHANGED_EVENT = "Custom Approval Delegator Changed";
	private static final String DELEGATOR_CHANGED_EVENT_DESC = "Raised when delegator settings have been updated";
	
	private EventPublisher eventPublisher;
	
	@Autowired
	public CustomApprovalSetup(@JiraImport EventPublisher eventPublisher) {
	    this.eventPublisher = eventPublisher;
	}

	/**
	 * Get CustomField for Request Participant.
	 * @return CustomField
	 */
	public static CustomField getRequestParticipantField() {
		CustomFieldManager cfMan = ComponentAccessor.getCustomFieldManager();
		Collection<CustomField> list = cfMan.getCustomFieldObjectsByName(CustomApprovalUtil.REUQEST_PARTICIPANT_FIELD_NAME);
		if (list != null && list.size() == 1) {
			return list.iterator().next();
		}
		return null;
	}
	
	/**
	 * Get CustomField for Manual Request Participant.
	 * @return CustomField
	 */
	public static CustomField getManualRequestParticipantCustomField() {
		CustomField result = null;
		CustomFieldManager cfMan = ComponentAccessor.getCustomFieldManager();
		CustomFieldType<?, ?> cfType = getCustomFieldTypeUsers();
		Collection<CustomField> list = cfMan.getCustomFieldObjectsByName(CustomApprovalUtil.MANUAL_REUQEST_PARTICIPANT_FIELD_NAME);
		if (list != null) {
			Iterator<CustomField> it = list.iterator();
			while (it.hasNext()) {
				CustomField cf = it.next();
				if (cfType.equals(cf.getCustomFieldType()) && 
					CustomApprovalUtil.MANUAL_REUQEST_PARTICIPANT_FIELD_DESCRIPTION.equals(cf.getDescription())) {
					result = cf;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Get CustomField for locking.
	 * @return CustomField
	 */
	public static CustomField getApprovalLockCustomField() {
		CustomField result = null;
		CustomFieldManager cfMan = ComponentAccessor.getCustomFieldManager();
		CustomFieldType<?, ?> cfType = getCustomFieldTypeTextArea();
		Collection<CustomField> list = cfMan.getCustomFieldObjectsByName(CustomApprovalUtil.LOCK_FIELD_NAME);
		if (list != null) {
			Iterator<CustomField> it = list.iterator();
			while (it.hasNext()) {
				CustomField cf = it.next();
				if (cfType.equals(cf.getCustomFieldType()) && 
					CustomApprovalUtil.LOCK_FIELD_DESCRIPTION.equals(cf.getDescription())) {
					result = cf;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Get CustomField of ApprovalData.
	 * @return CustomField
	 */
	public static CustomField getApprovalDataCustomField() {
		CustomField result = null;
		CustomFieldManager cfMan = ComponentAccessor.getCustomFieldManager();
		CustomFieldType<?, ?> cfType = getCustomFieldTypeTextArea();
		Collection<CustomField> list = cfMan.getCustomFieldObjectsByName(CustomApprovalUtil.CUSTOM_FIELD_NAME);
		if (list != null) {
			Iterator<CustomField> it = list.iterator();
			while (it.hasNext()) {
				CustomField cf = it.next();
				if (cfType.equals(cf.getCustomFieldType()) && 
					CustomApprovalUtil.CUSTOM_FIELD_DESCRIPTION.equals(cf.getDescription())) {
					result = cf;
					break;
				}
			}
		}
		return result;
	}
	
	public static CustomFieldType<?, ?> getCustomFieldTypeTextArea() {
		CustomFieldManager cfMan = ComponentAccessor.getCustomFieldManager();
		CustomFieldType<?, ?> cfType = cfMan.getCustomFieldType(CustomApprovalUtil.CUSTOM_FIELD_TEXT_AREA);
		return cfType;
	}
	
	public static CustomFieldType<?, ?> getCustomFieldTypeUsers() {
		CustomFieldManager cfMan = ComponentAccessor.getCustomFieldManager();
		CustomFieldType<?, ?> cfType = cfMan.getCustomFieldType(CustomApprovalUtil.CUSTOM_FIELD_USERS);
		return cfType;
	}
	
	private static void createCustomFields() {
		CustomFieldManager cfMan = ComponentAccessor.getCustomFieldManager();
		CustomFieldType<?, ?> cfTypeTextArea = getCustomFieldTypeTextArea(); 
		CustomFieldType<?, ?> cfTypeUsers = getCustomFieldTypeUsers();
		CustomFieldSearcher cfSearcherTextArea = null;
		List<CustomFieldSearcher> cfSearcherList = cfMan.getCustomFieldSearchers(cfTypeTextArea);
		if (cfSearcherList != null && cfSearcherList.size() != 0) {
			cfSearcherTextArea = cfSearcherList.get(0);
		}
		CustomFieldSearcher cfSearcherUsers = null;
		cfSearcherList = cfMan.getCustomFieldSearchers(cfTypeUsers);
		if (cfSearcherList != null && cfSearcherList.size() != 0) {
			cfSearcherUsers = cfSearcherList.get(0);
		}
		// Find if field exists
		CustomField result = getApprovalDataCustomField();
		// Create if not
		if (result == null) {
			List<JiraContextNode> contexts = Arrays.asList(GlobalIssueContext.getInstance());
			List<IssueType> issueTypes = Arrays.asList((IssueType) null);
			try {
				CustomField created = cfMan.createCustomField(
					CustomApprovalUtil.CUSTOM_FIELD_NAME, 
					CustomApprovalUtil.CUSTOM_FIELD_DESCRIPTION, 
					cfTypeTextArea, 
					cfSearcherTextArea,
					contexts, 
					issueTypes);
				// Lock the custom field
				ManagedConfigurationItemService configItemService = 
						ComponentAccessor.getComponent(ManagedConfigurationItemService.class);
				ManagedConfigurationItem item = configItemService.getManagedCustomField(created);
				ManagedConfigurationItemBuilder builder = item.newBuilder();
				item = builder
						.setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.ADMIN)
						.build();
				configItemService.updateManagedConfigurationItem(item);
				result = created;
				LOGGER.debug("Custom field created");
			} catch (Exception ex) {
				LOGGER.error("Failed to create custom field", ex);
			}
		}
		// Find if lock field exists
		CustomField lockField = getApprovalLockCustomField();
		// Create if not
		if (lockField == null) {
			List<JiraContextNode> contexts = Arrays.asList(GlobalIssueContext.getInstance());
			List<IssueType> issueTypes = Arrays.asList((IssueType) null);
			try {
				CustomField created = cfMan.createCustomField(
					CustomApprovalUtil.LOCK_FIELD_NAME, 
					CustomApprovalUtil.LOCK_FIELD_DESCRIPTION, 
					cfTypeTextArea, 
					cfSearcherTextArea,
					contexts, 
					issueTypes);
				// Lock the custom field
				ManagedConfigurationItemService configItemService = 
						ComponentAccessor.getComponent(ManagedConfigurationItemService.class);
				ManagedConfigurationItem item = configItemService.getManagedCustomField(created);
				ManagedConfigurationItemBuilder builder = item.newBuilder();
				item = builder
						.setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.ADMIN)
						.build();
				configItemService.updateManagedConfigurationItem(item);
				lockField = created;
				LOGGER.debug("Lock field created");
			} catch (Exception ex) {
				LOGGER.error("Failed to create lock field", ex);
			}
		}
		// Find if manual request participant field exists
		CustomField manualRequestParticipantField = getManualRequestParticipantCustomField();
		// Create if not
		if (manualRequestParticipantField == null) {
			List<JiraContextNode> contexts = Arrays.asList(GlobalIssueContext.getInstance());
			List<IssueType> issueTypes = Arrays.asList((IssueType) null);
			try {
				CustomField created = cfMan.createCustomField(
					CustomApprovalUtil.MANUAL_REUQEST_PARTICIPANT_FIELD_NAME, 
					CustomApprovalUtil.MANUAL_REUQEST_PARTICIPANT_FIELD_DESCRIPTION, 
					cfTypeUsers, 
					cfSearcherUsers,
					contexts, 
					issueTypes);
				// Lock the custom field
				ManagedConfigurationItemService configItemService = 
						ComponentAccessor.getComponent(ManagedConfigurationItemService.class);
				ManagedConfigurationItem item = configItemService.getManagedCustomField(created);
				ManagedConfigurationItemBuilder builder = item.newBuilder();
				item = builder
						.setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.ADMIN)
						.build();
				configItemService.updateManagedConfigurationItem(item);
				lockField = created;
				LOGGER.debug("Manual Request Participant field created");
			} catch (Exception ex) {
				LOGGER.error("Failed to create manual request participant field", ex);
			}
		}
	}
	
	public static Long getCustomEventType() {
		EventTypeManager evm = ComponentAccessor.getEventTypeManager();
		for (EventType et : evm.getEventTypes()) {
			if (DELEGATOR_CHANGED_EVENT.equals(et.getName())) {
				return et.getId();
			}
		}
		return null;
	}
	
	private static void createCustomEvent() {
		EventTypeManager evm = ComponentAccessor.getEventTypeManager();
		if (!evm.isEventTypeExists(DELEGATOR_CHANGED_EVENT)) {
			EventType ev = new EventType(DELEGATOR_CHANGED_EVENT, DELEGATOR_CHANGED_EVENT_DESC, EventType.ISSUE_GENERICEVENT_ID);
			evm.addEventType(ev);
		}
	}
	
	// PluginInstalledEvent, PluginUninstallingEvent and PluginUninstalledEvent 
	// The above events do not work (at least not in SDK), never invoked
	
	// No need to unlock or remove the custom field on disable or uninstall, because
	// Jira will hide the custom field when the custom field type got unregistered
	
	// So we only make sure the field is there and is locked on PluginEnabledEvent
	
	@EventListener
    public void onPluginEnabled(PluginEnabledEvent event) {
		LOGGER.debug("PluginEvent enabled: " + event.getPlugin().getKey());
		if (CustomApprovalUtil.PLUGIN_KEY.equals(event.getPlugin().getKey())) {
			CustomApprovalSetup.createCustomFields();
			CustomApprovalSetup.createCustomEvent();
		}
		CustomApprovalUtil.createScheduledJob(CustomApprovalUtil.getJobFrequency());
		// Recreate adhoc jobs from all delegation settings
		List<DelegationSetting> settings = DelegationUtil.loadAllData();
		for (DelegationSetting setting : settings) {
			Date now = new Date();
			// Create adhoc jobs
			LOGGER.debug("Adding adhoc job pair");
			// Create schedule for start/end time if they are in future
			if (setting.getStartDate().after(now)) {
				boolean addJob = CustomApprovalUtil.createAdhocJob(
						setting.getStartDate(), true, setting.getFromUser(), setting.getDelegateToUser());
				LOGGER.debug("Add job created: " + addJob);
			}
			if (setting.getEndDate() != null && setting.getEndDate().after(now)) {
				boolean removeJob = CustomApprovalUtil.createAdhocJob(
						setting.getEndDate(), false, setting.getFromUser(), setting.getDelegateToUser());
				LOGGER.debug("Remove job created " + removeJob);
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
