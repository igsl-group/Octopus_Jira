package com.igsl.customfieldtypes.generictable;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.action.admin.customfields.AbstractEditConfigurationItemAction;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

/**
 * WebWorks class responsible for GenericTable's configuration page
 */
@Named
public class GenericTableConfigurationItemAction extends AbstractEditConfigurationItemAction {

	private static final long serialVersionUID = 1L;
	
	private static Logger LOGGER = LoggerFactory.getLogger(GenericTableConfigurationItemAction.class);
	private static final String PARAM_CUSTOM_FIELD_ID = "customFieldId";
	private static final String PARAM_FIELD_CONFIG_ID = "fieldConfigId";
		
	// Custom field configuration URL
	private static final String PARENT_PAGE_URL = "/secure/admin/ConfigureCustomField!default.jspa?customFieldId=";
	private static final String MY_PAGE_URL = "/secure/admin/GenericTableConfigurationItemAction.jspa?customFieldId=";
	
	// Web action key words
	private static final String SAVE = "Save";
	private static final String CANCEL = "Cancel";
	private static final String PREVIEW = "Preview";

	protected ManagedConfigurationItemService service;
	private static final CustomFieldManager CUSTOM_FIELD_MANAGER = ComponentAccessor.getCustomFieldManager();
	
	private String fieldId;	// Without customfield_
	private String customFieldName;
	private String myPageURL;
	private String parentPageURL;
	
	@Inject
	protected GenericTableConfigurationItemAction(@ComponentImport ManagedConfigurationItemService managedConfigurationItemService) {
		super(managedConfigurationItemService);
		LOGGER.debug("constructor: " + managedConfigurationItemService);
		this.service = managedConfigurationItemService;
	}
	
	public GenericTableSettings getValue() {
		String id = "customfield_" + this.getHttpRequest().getParameter(PARAM_CUSTOM_FIELD_ID);
		String name = "";
		CustomField cf = CUSTOM_FIELD_MANAGER.getCustomFieldObject(id);
		if (cf != null) {
			name = cf.getName();
		}
		return GenericTableSettings.getSettings(id, name);
	}
	
	protected void doValidation() {
		LOGGER.debug("doValidation");
	}
	
	// Strangely setFieldConfigId() is never invoked. So instead, grab custom field ID from request parameters if possible
	// The HTTP parameters available is... not consistent? In local site, both fieldConfigId and customFieldId are available. In Octopus site, only fieldConfigId is. 
	private void refreshData() {
		this.fieldId = this.getHttpRequest().getParameter(PARAM_CUSTOM_FIELD_ID);
		LOGGER.debug("WTF fieldId: " + this.fieldId);
		String fieldConfigId = this.getHttpRequest().getParameter(PARAM_FIELD_CONFIG_ID);
		if (fieldConfigId != null) {
			long id = Long.parseLong(fieldConfigId);
			this.setFieldConfigId(id);
		}
		this.customFieldName = "";
		CustomField cf = CUSTOM_FIELD_MANAGER.getCustomFieldObject("customfield_" + this.fieldId);
		if (cf != null) {
			this.customFieldName = cf.getName();
		}
		this.myPageURL = MY_PAGE_URL + this.fieldId;
		this.parentPageURL = PARENT_PAGE_URL + this.fieldId;
		LOGGER.debug("WTF customFieldName: " + this.customFieldName);
		LOGGER.debug("WTF myPageURL: " + this.myPageURL);
		LOGGER.debug("WTF parentPageURL: " + this.parentPageURL);
	}
	
	// Expected return value is name of associated view
	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		refreshData();
		GenericTableSettings settings = GenericTableSettings.parseParameters(this.getHttpRequest());
		if (settings != null) {
			settings.setCustomFieldId("customfield_" + this.fieldId);
			settings.setCustomFieldName(customFieldName);
			String save = getHttpRequest().getParameter(SAVE);
	        String cancel = getHttpRequest().getParameter(CANCEL);
	        if (save != null && save.equals(SAVE)) {
	        	// Save data
	        	LOGGER.debug("WTF Saving: " + settings);
				settings.saveSettings();
				LOGGER.debug("WTF Saved, Going to: " + this.myPageURL);
				return getRedirect(this.myPageURL);
	        } else if (cancel != null && cancel.equals(CANCEL)) {
				LOGGER.debug("WTF Cancel, Going to: " + this.parentPageURL);
	        	return getRedirect(this.parentPageURL);
	        }
		}
    	return INPUT;
	}

	public String getFieldId() {
		return fieldId;
	}

	public String getCustomFieldName() {
		return customFieldName;
	}
}
