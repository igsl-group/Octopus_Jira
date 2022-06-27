package com.igsl.customfieldtypes.efforttable;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.web.action.admin.customfields.AbstractEditConfigurationItemAction;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

@Named
public class EffortTableConfigurationItemAction extends AbstractEditConfigurationItemAction {

	private static final long serialVersionUID = 1L;
	
	private static Logger LOGGER = LoggerFactory.getLogger(EffortTableConfigurationItemAction.class);
	private static final String PARAM_CUSTOM_FIELD_ID = "customFieldId";
	private static final String PARAM_FIELD_CONFIG_ID = "fieldConfigId";
	
	// Custom field configuration URL
	private static final String PARENT_PAGE_URL = "/secure/admin/ConfigureCustomField!default.jspa?customFieldId=";
	
	// Web action key words
	private static final String SAVE = "Save";
	private static final String CANCEL = "Cancel";
	
	// Magic word to return after calling setReturnUrl()
	private static final String NOT_USED = "not used";	
	
	// Magic word to stay on same page
	private static final String INPUT = "input";
	
	protected ManagedConfigurationItemService service;
	
	@Inject
	protected EffortTableConfigurationItemAction(@ComponentImport ManagedConfigurationItemService managedConfigurationItemService) {
		super(managedConfigurationItemService);
		LOGGER.debug("constructor: " + managedConfigurationItemService);
		this.service = managedConfigurationItemService;
	}
	
	public List<String> getValue() {
		return EffortTable2.getTaskListSettings();
	}
	
	protected void doValidation() {
		LOGGER.debug("doValidation");
		String[] list = this.getHttpRequest().getParameterValues(EffortTable2.SETTINGS_TASK_LIST);
		if (list != null) {
			for(String s : list) {
				LOGGER.debug("Param: " + s);
			}
		}
		// TODO Validate
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
	private String getParentPageURL() {
		String result = null;
		// Check parameters
		String fieldConfigId = getParameter(PARAM_FIELD_CONFIG_ID);
		if (fieldConfigId != null) {
			long id = Long.parseLong(fieldConfigId);
			this.setFieldConfigId(id);
			result = PARENT_PAGE_URL + this.getFieldConfig().getCustomField().getId();
		} 
		String customFieldId = getParameter(PARAM_CUSTOM_FIELD_ID);
		if (customFieldId != null) {
			result = PARENT_PAGE_URL + customFieldId;
		}
		LOGGER.debug("Parent page URL: [" + result + "]");
		return result;
	}
	
	// Expected return value is name of associated view
	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		String parentPageURL = getParentPageURL();
		String[] list = this.getHttpRequest().getParameterValues(EffortTable2.SETTINGS_TASK_LIST);
		String save = getHttpRequest().getParameter(SAVE);
        String cancel = getHttpRequest().getParameter(CANCEL);
		if (save != null && save.equals(SAVE)) {
        	// Save data
			EffortTable2.saveTaskListSettings(Arrays.asList(list));
        	// Return to parent page
			if (parentPageURL != null) {
				setReturnUrl(parentPageURL);
	        	return getRedirect(INPUT);
			}
        } else if (cancel != null && cancel.equals(CANCEL)) {
        	// Return to parent page
        	if (parentPageURL != null) {
				setReturnUrl(parentPageURL);
	        	return getRedirect(INPUT);
			}
        }
    	return INPUT;
	}
}
