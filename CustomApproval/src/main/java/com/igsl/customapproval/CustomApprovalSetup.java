package com.igsl.customapproval;

import java.util.Arrays;
import java.util.Collection;
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
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;

@Component
public class CustomApprovalSetup implements InitializingBean, DisposableBean {
	
	private static final Logger LOGGER = Logger.getLogger(CustomApprovalSetup.class);
	
	private EventPublisher eventPublisher;
	
	@Autowired
	public CustomApprovalSetup(@JiraImport EventPublisher eventPublisher) {
	    this.eventPublisher = eventPublisher;
	}

	/**
	 * Get CustomField for locking.
	 * @return CustomField
	 */
	public static CustomField getApprovalLockCustomField() {
		CustomField result = null;
		CustomFieldManager cfMan = ComponentAccessor.getCustomFieldManager();
		CustomFieldType<?, ?> cfType = getCustomFieldType();
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
		CustomFieldType<?, ?> cfType = getCustomFieldType();
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
	
	public static CustomFieldType<?, ?> getCustomFieldType() {
		CustomFieldManager cfMan = ComponentAccessor.getCustomFieldManager();
		CustomFieldType<?, ?> cfType = cfMan.getCustomFieldType(CustomApprovalUtil.CUSTOM_FIELD_TEXT_AREA);
		return cfType;
	}
	
	private static void createCustomFields() {
		CustomFieldManager cfMan = ComponentAccessor.getCustomFieldManager();
		CustomFieldType<?, ?> cfType = getCustomFieldType(); 
		CustomFieldSearcher cfSearcher = null;
		List<CustomFieldSearcher> cfSearcherList = cfMan.getCustomFieldSearchers(cfType);
		if (cfSearcherList != null && cfSearcherList.size() != 0) {
			cfSearcher = cfSearcherList.get(0);
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
					cfType, 
					cfSearcher,
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
					cfType, 
					cfSearcher,
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
		}
		CustomApprovalUtil.createScheduledJob(CustomApprovalUtil.getJobFrequency());
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
