package com.igsl.customapproval.workflow.postfunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.plugin.workflow.UpdateIssueFieldFunctionPluginFactory;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igsl.customapproval.CustomApprovalUtil;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

public class InitializeApprovalPostFunctionFactory extends UpdateIssueFieldFunctionPluginFactory {

	private static final Logger LOGGER = Logger.getLogger(InitializeApprovalPostFunctionFactory.class);
	private static final ObjectMapper OM = new ObjectMapper(); 
	
	// Velocity parameters
	public static final String VELOCITY_USER_FIELD_LIST = "userFieldList";
	public static final String VELOCITY_GROUP_FIELD_LIST = "groupFieldList";
	public static final String VELOCITY_STATUS_LIST = "statusList";
	
	// Form data in Velocity template
	public static final String PARAM_APPROVAL_NAME = "approvalName";
	public static final String PARAM_USERS_FIELD = "approverUsersField";
	public static final String PARAM_GROUPS_FIELD = "approverGroupsField";
	public static final String PARAM_STATUS_STARING = "startingStatus";
	public static final String PARAM_STATUS_APPROVED = "approvedStatus";
	public static final String PARAM_STATUS_REJECTED = "rejectedStatus";
	public static final String PARAM_APPROVE_COUNT = "approveCount";
	public static final String PARAM_REJECT_COUNT = "rejectCount";
	public static final String PARAM_ALLOW_CHANGE_DECISION = "allowChangeDecision";
	public static final String[] PARAMETERS_LIST = {
			PARAM_APPROVAL_NAME,	// This must go first
			PARAM_USERS_FIELD,
			PARAM_GROUPS_FIELD,
			PARAM_STATUS_STARING,
			PARAM_STATUS_APPROVED,
			PARAM_STATUS_REJECTED,
			PARAM_APPROVE_COUNT,
			PARAM_REJECT_COUNT,
			PARAM_ALLOW_CHANGE_DECISION
	};	
	public static final String VALUE_ALLOW = "Allow";
	public static final String VALUE_DENY = "Deny";
	
	@JiraImport
	private FieldManager fieldManager;
	@JiraImport
	private UserKeyService userKeyService;
	
	public InitializeApprovalPostFunctionFactory(FieldManager fieldManager, UserKeyService userKeyService) {
		super(fieldManager, userKeyService);
		this.fieldManager = fieldManager;
		this.userKeyService = userKeyService;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void getVelocityParamsForInput(Map velocityParams) {
		LOGGER.debug("getVelocityParamsForInput");
		// Set data for pickers
		velocityParams.put(VELOCITY_USER_FIELD_LIST, CustomApprovalUtil.getUserFieldList());
		velocityParams.put(VELOCITY_GROUP_FIELD_LIST, CustomApprovalUtil.getGroupFieldList());
		velocityParams.put(VELOCITY_STATUS_LIST, CustomApprovalUtil.getStatusList());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor) {
		getVelocityParamsForInput(velocityParams);
		// The settings is stored in FunctionDescriptor.getArgs().
		// Transfer them to velocityParams for display
		if (descriptor instanceof FunctionDescriptor) {
			FunctionDescriptor fd = (FunctionDescriptor) descriptor;
			Map map = fd.getArgs();
			for (String s : PARAMETERS_LIST) {
				Object o = map.get(s);
				LOGGER.debug("getVelocityParamsForEdit <" + s + "> = <" + o + ">(" + ((o != null)?o.getClass():"N/A") + ")");
				if (o instanceof String) {
					List list = null;
					try {
						list = OM.readValue((String) o, List.class);
					} catch (Exception ex) {
						LOGGER.error("Failed to deserialize " + s, ex);
					}
					velocityParams.put(s, list);
				} else {
					throw new IllegalArgumentException("Unsupported data type " + ((o != null)?o.getClass():"N/A") + " for " + s);
				}
			}
		} else {
			throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
		}
		String s = null;
		try {
			s = OM.writeValueAsString(velocityParams);
		} catch (Exception ex) {
			LOGGER.error("Failed to serialize " + s, ex);
		}
		LOGGER.debug("getVelocityParamsForEdit result <" + s + ">");
	}

	@SuppressWarnings("rawtypes")
	@Override
    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor) {
		LOGGER.debug("getVelocityParamsForView");
		getVelocityParamsForEdit(velocityParams, descriptor);
    }

	@SuppressWarnings("rawtypes")
	@Override
    public Map<String, Object> getDescriptorParams(Map params) {
		// conditionParams contains form data submitted by input/edit page.
		// Values are string arrays
		
		// The return value is used to construct FunctionDescriptor 
		// for getVelocityParamsForEdit and getVelocityParamsForView
		
		// The goal here is get the velocity template parameters from params
		// and convert them to Strings in a map
		Map<String, Object> result = new HashMap<>();
		for (String s : PARAMETERS_LIST) {
			String value = null;
			Object o = params.get(s);
			LOGGER.debug("getDescriptorParams <" + s + "> = <" + o + ">(" + ((o != null)?o.getClass():"N/A") + ")");
			if (o != null) {
				if (o instanceof String) {
					try {
						value = OM.writeValueAsString(new String[] {(String) o});
					} catch (Exception ex) {
						LOGGER.error("Failed to serialize " + s, ex);
					}
				} else if (o instanceof String[]) {
					try {
						value = OM.writeValueAsString(o);
					} catch (Exception ex) {
						LOGGER.error("Failed to serialize " + s, ex);
					}
				} else {
					throw new IllegalArgumentException("Unsupported data type " + ((o != null)?o.getClass():"N/A") + " for " + s);
				}
			}
			result.put(s, value);
		}
		String s = null;
		try {
			s = OM.writeValueAsString(result);
		} catch (Exception ex) {
			LOGGER.error("Failed to serialize map", ex);
		}
		LOGGER.debug("getDescriptorParams result <" + s + ">");
		return result;
    }
	
	/**
	 * Extract parameter lists from a generic map.
	 * The lists will be stored in the map as string.
	 */
	public static Map<String, String[]> parseArguments(@SuppressWarnings("rawtypes") Map m) throws Exception {
		Map<String, String[]> result = new HashMap<>();
		for (String key : PARAMETERS_LIST) {
			Object o = m.get(key);
			String[] value = null;
			if (o != null) {
				if (o instanceof String) {
					String v = (String) o;
					if (v.length() != 0) {
						String[] list = OM.readValue(v, String[].class);
						value = list;
					}
				} else {
					throw new Exception("Parameter " + key + " is not a string");
				}
			}
			result.put(key, value);
		}
		return result;
	}
}
