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
import com.opensymphony.workflow.loader.WorkflowDescriptor;

public class InitializeApprovalPostFunctionFactory extends UpdateIssueFieldFunctionPluginFactory {

	private static final Logger LOGGER = Logger.getLogger(InitializeApprovalPostFunctionFactory.class);
	private static final ObjectMapper OM = new ObjectMapper(); 
	
	// Velocity parameters
	public static final String VELOCITY_USER_FIELD_LIST = "userFieldList";
	public static final String VELOCITY_GROUP_FIELD_LIST = "groupFieldList";
	public static final String VELOCITY_STATUS_LIST = "statusList";
	public static final String VELOCITY_TRANSITION_LIST = "transitionList";
	public static final String VELOCITY_INTEGER = "Integer";
	public static final String VELOCITY_LONG = "Long";
	
	// Form data in Velocity template
	public static final String PARAM_APPROVAL_NAME = "approvalName";
	public static final String PARAM_USERS_FIELD = "approverUsersField";
	public static final String PARAM_GROUPS_FIELD = "approverGroupsField";
	public static final String PARAM_STATUS_STARING = "startingStatus";
	public static final String PARAM_STATUS_APPROVED = "approvedStatus";
	public static final String PARAM_APPROVE_TRANSITION = "approveTransition";
	public static final String PARAM_STATUS_REJECTED = "rejectedStatus";
	public static final String PARAM_REJECT_TRANSITION = "rejectTransition";
	public static final String PARAM_APPROVE_COUNT = "approveCount";
	public static final String PARAM_REJECT_COUNT = "rejectCount";
	public static final String PARAM_NO_APPROVER_ACTION = "noApproverAction";
	public static final String PARAM_ALLOW_CHANGE_DECISION = "allowChangeDecision";
	public static final String PARAM_CONFIRM_DECISION = "confirmDecision";
	public static final String PARAM_CONFIRM_TITLE = "confirmTitle";
	public static final String PARAM_APPROVE_MESSAGE = "approveMessage";
	public static final String PARAM_REJECT_MESSAGE = "rejectMessage";
	public static final String PARAM_CONFIRM_OK = "confirmOK";
	public static final String PARAM_CONFIRM_CANCEL = "confirmCancel";
	public static final String[] PARAMETERS_LIST = {
			PARAM_APPROVAL_NAME,
			PARAM_USERS_FIELD,
			PARAM_GROUPS_FIELD,
			PARAM_STATUS_STARING,
			PARAM_STATUS_APPROVED,
			PARAM_APPROVE_TRANSITION,
			PARAM_STATUS_REJECTED,
			PARAM_REJECT_TRANSITION,
			PARAM_APPROVE_COUNT,
			PARAM_REJECT_COUNT,
			PARAM_NO_APPROVER_ACTION,
			PARAM_ALLOW_CHANGE_DECISION,
			PARAM_CONFIRM_DECISION,
			PARAM_CONFIRM_TITLE,
			PARAM_APPROVE_MESSAGE,
			PARAM_REJECT_MESSAGE,
			PARAM_CONFIRM_OK,
			PARAM_CONFIRM_CANCEL
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
		velocityParams.put(VELOCITY_INTEGER, new Integer(0));
		velocityParams.put(VELOCITY_LONG, new Long(0));
	}

	protected WorkflowDescriptor getWorkflowDescriptor(AbstractDescriptor descriptor) {
		AbstractDescriptor topDesc = descriptor;
		do {
			if (topDesc.getParent() != null) {
				topDesc = topDesc.getParent();
			} else {
				break;
			}
		} while (true);
		if (topDesc instanceof WorkflowDescriptor) {
			return (WorkflowDescriptor) topDesc;
		}
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor) {
		WorkflowDescriptor wfDesc = getWorkflowDescriptor(descriptor);
		if (wfDesc == null) {
			throw new IllegalArgumentException("Unable to locate Workflow Descriptor");
		}
		velocityParams.put(VELOCITY_TRANSITION_LIST, CustomApprovalUtil.getTransitions(wfDesc));
		getVelocityParamsForInput(velocityParams);
		// The settings is stored in FunctionDescriptor.getArgs().
		// Transfer them to velocityParams for display
		if (descriptor instanceof FunctionDescriptor) {
			FunctionDescriptor fd = (FunctionDescriptor) descriptor;
			Map map = fd.getArgs();
			// Scan for maxListSize
			int maxListSize = 0;
			for (String s : PARAMETERS_LIST) {
				Object o = map.get(s);
				if (o != null) {
					List list = null;
					try {
						list = OM.readValue((String) o, List.class);
						maxListSize = Math.max(maxListSize, list.size());
					} catch (Exception ex) {
						LOGGER.error("Failed to deserialize " + s, ex);
					}
				}
			}
			// Put the lists into velocityParams and map
			for (String s : PARAMETERS_LIST) {
				Object o = map.get(s);
				if (o != null) {
					List list = null;
					try {
						list = OM.readValue((String) o, List.class);
					} catch (Exception ex) {
						LOGGER.error("Failed to deserialize " + s, ex);
					}
					while (list.size() < maxListSize) {
						list.add("");
					}
					velocityParams.put(s, list);
					map.put(s, list);
				}
			}
			/*
			for (String s : PARAMETERS_LIST) {
				Object o = map.get(s);
				LOGGER.debug("getVelocityParamsForEdit <" + s + "> = <" + o + ">(" + ((o != null)?o.getClass():"N/A") + ")");
				if (o != null) {
					if (o instanceof String) {
						List list = null;
						try {
							list = OM.readValue((String) o, List.class);
							listSize = list.size();
						} catch (Exception ex) {
							LOGGER.error("Failed to deserialize " + s, ex);
						}
						velocityParams.put(s, list);
					} else {
						throw new IllegalArgumentException("Unsupported data type " + ((o != null)?o.getClass():"N/A") + " for " + s);
					}
				}
			}
			// For null values, add empty values matching other lists
			List list = new ArrayList<>();
			for (int i = 0; i < listSize; i++) {
				list.add("");
			}
			for (String s : PARAMETERS_LIST) {
				Object o = map.get(s);
				if (o == null) {
					map.put(s, list);
				}
			}
			*/
		} else {
			throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
		}
		LOGGER.debug("getVelocityParamsForEdit returning");
	}

	@SuppressWarnings("rawtypes")
	@Override
    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor) {
		LOGGER.debug("getVelocityParamsForView");
		getVelocityParamsForInput(velocityParams);
		getVelocityParamsForEdit(velocityParams, descriptor);
    }

	@SuppressWarnings("rawtypes")
	@Override
    public Map<String, Object> getDescriptorParams(Map params) {
		// params contains form data submitted by input/edit page.
		// Values are string arrays

		// The return value is used to construct FunctionDescriptor 
		// for getVelocityParamsForEdit and getVelocityParamsForView
		
		// The goal here is get the velocity template parameters from params
		// and convert them to Strings in a map
		// The map is provided to post function for execution
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
		LOGGER.debug("getDescriptorParams returning");
		return result;
    }
	
	/**
	 * Extract parameter lists from a generic map.
	 * The lists will be stored in the map as string.
	 * @param m Map
	 * @return Map of String to String[]
	 * @throws Exception Error parsing the arguments.
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
