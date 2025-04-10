package com.igsl.customfieldtypes.generictable;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
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
	private static final String PARENT_PAGE_URL = "/secure/admin/ConfigureCustomField!default.jspa?" + PARAM_FIELD_CONFIG_ID + "=";
	private static final String MY_PAGE_URL = "/secure/admin/GenericTableConfigurationItemAction.jspa?" + PARAM_FIELD_CONFIG_ID + "=";
	
	// Web action key words
	private static final String SAVE = "Save";
	private static final String CANCEL = "Cancel";
	private static final String PREVIEW = "Preview";

	protected ManagedConfigurationItemService service;
	private static final CustomFieldManager CUSTOM_FIELD_MANAGER = ComponentAccessor.getCustomFieldManager();
	private static final FieldConfigSchemeManager FIELD_CONFIG_SCHEME_MANAGER = 
			ComponentAccessor.getFieldConfigSchemeManager();
	
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
		String id = fieldId;
		String name = customFieldName;
		LOGGER.debug("getSettings: " + id + ", " + name);
		return GenericTableSettings.getSettings(id, name);
	}
	
	protected void doValidation() {
		LOGGER.debug("doValidation");
	}
	
	// Strangely setFieldConfigId() is never invoked. So instead, grab custom field ID from request parameters if possible
	// The HTTP parameters available is... not consistent? In local site, both fieldConfigId and customFieldId are available. In Octopus site, only fieldConfigId is. 
	private boolean refreshData() {
		String fcid = this.getHttpRequest().getParameter(PARAM_FIELD_CONFIG_ID);
		LOGGER.info("refreshData Parameter: [" + fcid + "]");
		if (fcid != null) {
			long id = Long.parseLong(fcid);
			setFieldConfigId(id);
			FieldConfigScheme scheme = FIELD_CONFIG_SCHEME_MANAGER.getFieldConfigScheme(id);
			if (scheme != null) {
				fieldId = scheme.getField().getId();
				LOGGER.info("refreshData fieldId: [" + fieldId + "]");
				customFieldName = "";
				CustomField cf = CUSTOM_FIELD_MANAGER.getCustomFieldObject(this.fieldId);
				if (cf != null) {
					customFieldName = cf.getName();
				}
				LOGGER.info("refreshData customFieldName: [" + customFieldName + "]");
				this.myPageURL = MY_PAGE_URL + id;
				LOGGER.info("refreshData myPageURL: [" + myPageURL + "]");
				this.parentPageURL = PARENT_PAGE_URL + id;
				return true;
			}
		}
		return false;
	}
	
	// Expected return value is name of associated view
	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		if (!refreshData()) {
			String msg = "Unable to locate custom field";
			LOGGER.error(msg);
			throw new Exception(msg);
		}
		GenericTableSettings settings = GenericTableSettings.parseParameters(this.getHttpRequest());
		if (settings != null) {
			settings.setCustomFieldId(fieldId);
			settings.setCustomFieldName(customFieldName);
			String save = getHttpRequest().getParameter(SAVE);
	        String cancel = getHttpRequest().getParameter(CANCEL);
	        if (save != null && save.equals(SAVE)) {
	        	// Save data
	        	LOGGER.debug("saveSettings: " + settings);
				settings.saveSettings();
				return getRedirect(this.myPageURL);
	        } else if (cancel != null && cancel.equals(CANCEL)) {
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
