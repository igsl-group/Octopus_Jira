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
	
	// Web action key words
	private static final String SAVE = "Save";
	private static final String CANCEL = "Cancel";
	private static final String PREVIEW = "Preview";

	protected ManagedConfigurationItemService service;
	private static final CustomFieldManager CUSTOM_FIELD_MANAGER = ComponentAccessor.getCustomFieldManager();
	
	private String customFieldId;
	private String customFieldName;
	private String parentPageURL;
	
	@Inject
	protected GenericTableConfigurationItemAction(@ComponentImport ManagedConfigurationItemService managedConfigurationItemService) {
		super(managedConfigurationItemService);
		LOGGER.debug("constructor: " + managedConfigurationItemService);
		this.service = managedConfigurationItemService;
	}
	
	public GenericTableSettings getValue() {
		String customFieldId = "customfield_" + getParameter(PARAM_CUSTOM_FIELD_ID);
		String customFieldName = "";
		CustomField cf = CUSTOM_FIELD_MANAGER.getCustomFieldObject(customFieldId);
		if (cf != null) {
			customFieldName = cf.getName();
		}
		return GenericTableSettings.getSettings(customFieldId, customFieldName);
	}
	
	protected void doValidation() {
		LOGGER.debug("doValidation");
		refreshData();
		GenericTableSettings.parseParameters(this.getHttpRequest());
	}
	
	private String getParameter(String name) {
		String s = this.getHttpRequest().getParameter(name);
		if (s != null && !s.isEmpty()) {
			LOGGER.debug("From request param: " + name + " = [" + s + "]");
			this.getHttpSession().setAttribute(name, s);
			return s;
		} else {
			Object o = this.getHttpSession().getAttribute(name);
			if (o != null && o instanceof String) {
				LOGGER.debug("From session: " + name + " = [" + o + "]");
				return (String) o;
	 		}
		}
		return null;
	}
	
	// Strangely setFieldConfigId() is never invoked. So instead, grab custom field ID from request parameters if possible
	// The HTTP parameters available is... not consistent? In local site, both fieldConfigId and customFieldId are available. In Octopus site, only fieldConfigId is. 
	private void refreshData() {
		String fieldConfigId = getParameter(PARAM_FIELD_CONFIG_ID);
		if (fieldConfigId != null) {
			long id = Long.parseLong(fieldConfigId);
			this.setFieldConfigId(id);
		}
		customFieldId = "customfield_" + getParameter(PARAM_CUSTOM_FIELD_ID);
		customFieldName = "";
		CustomField cf = CUSTOM_FIELD_MANAGER.getCustomFieldObject(customFieldId);
		if (cf != null) {
			customFieldName = cf.getName();
		}
		parentPageURL = PARENT_PAGE_URL + getParameter(PARAM_CUSTOM_FIELD_ID);
	}
	
	// Expected return value is name of associated view
	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		refreshData();
		GenericTableSettings settings = GenericTableSettings.parseParameters(this.getHttpRequest());
		if (settings != null) {
			settings.setCustomFieldId(customFieldId);
			settings.setCustomFieldName(customFieldName);
			String save = getHttpRequest().getParameter(SAVE);
	        String cancel = getHttpRequest().getParameter(CANCEL);
	        if (save != null && save.equals(SAVE)) {
	        	// Save data
				settings.saveSettings();
				// Stay on same page for preview
				return INPUT;
	        } else if (cancel != null && cancel.equals(CANCEL)) {
				// Return to parent page
	        	if (parentPageURL != null) {
					setReturnUrl(parentPageURL);
		        	return getRedirect(INPUT);
				}
	        }
		}
    	return INPUT;
	}
}
