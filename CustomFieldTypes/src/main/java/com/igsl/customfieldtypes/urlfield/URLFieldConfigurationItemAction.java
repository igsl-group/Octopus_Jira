package com.igsl.customfieldtypes.urlfield;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.web.action.admin.customfields.AbstractEditConfigurationItemAction;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

@Named
public class URLFieldConfigurationItemAction extends AbstractEditConfigurationItemAction {

	private static Logger LOGGER = LoggerFactory.getLogger(URLFieldConfigurationItemAction.class);

	protected ManagedConfigurationItemService service;
	private PluginSettingsFactory factory;
	private PluginSettings settings;
	
	@Inject
	protected URLFieldConfigurationItemAction(@ComponentImport ManagedConfigurationItemService managedConfigurationItemService) {
		super(managedConfigurationItemService);
		LOGGER.debug("constructor: " + managedConfigurationItemService);
		this.service = managedConfigurationItemService;
		factory = (PluginSettingsFactory) ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		settings = factory.createGlobalSettings();
	}
	
	public Integer getValue() {
		String s = String.valueOf(settings.get(URLField.CONFIG_MAX));
		LOGGER.debug("getValue: " + s);
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException nfex) {
			return null;
		}
	}
	
	protected void doValidation() {
		LOGGER.debug("doValidation");
		String data = null;
		String[] list = this.getHttpRequest().getParameterValues(URLField.CONFIG_MAX);
		if (list != null) {
			for(String s : list) {
				LOGGER.debug("Param: " + s);
				data = s;
			}
		}
		// Validate action data
		try {
			Integer.parseInt(data);
		} catch (NumberFormatException nfex) {
			addErrorMessage(data + " is not valid", Reason.VALIDATION_FAILED);
		}
	}
	
	// Expected return value is name of associated view
	@Override
	protected String doExecute() throws Exception {
		String value = null;
		LOGGER.debug("doExecute");
		String[] list = this.getHttpRequest().getParameterValues(URLField.CONFIG_MAX);
		if (list != null) {
			for(String s : list) {
				LOGGER.debug("Param: " + s);
				value = s;
			}
		}
		String save = getHttpRequest().getParameter("Save");
        if (save != null && save.equals("Save")) {
        	// Save data
        	settings.put(URLField.CONFIG_MAX, value);
        	/*
        	// Return to parent page - doesn't work due to getFieldConfig() is null
        	setReturnUrl("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + getFieldConfig().getCustomField().getId());
        	return getRedirect("not used");
        	*/
        }
    	return getRedirect("ViewCustomFields.jspa");
    	// return "input" to go to same page
	}
}
