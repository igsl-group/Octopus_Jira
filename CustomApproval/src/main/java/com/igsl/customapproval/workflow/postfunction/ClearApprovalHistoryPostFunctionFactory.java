package com.igsl.customapproval.workflow.postfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class ClearApprovalHistoryPostFunctionFactory extends UpdateIssueFieldFunctionPluginFactory {

	private static final Logger LOGGER = Logger.getLogger(ClearApprovalHistoryPostFunctionFactory.class);
	private static final ObjectMapper OM = new ObjectMapper(); 
	
	// Velocity parameters
	public static final String VELOCITY_APPROVAL_LIST = "approvalList";
	
	// Form data in Velocity template
	public static final String PARAM_APPROVAL_NAME = "approvalName";
	
	@JiraImport
	private FieldManager fieldManager;
	@JiraImport
	private UserKeyService userKeyService;
	
	public ClearApprovalHistoryPostFunctionFactory(FieldManager fieldManager, UserKeyService userKeyService) {
		super(fieldManager, userKeyService);
		this.fieldManager = fieldManager;
		this.userKeyService = userKeyService;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void getVelocityParamsForInput(Map velocityParams) {
		LOGGER.debug("getVelocityParamsForInput");
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
		Set<String> approvalNames = CustomApprovalUtil.findApprovalNames(getWorkflowDescriptor(descriptor));
		velocityParams.put(VELOCITY_APPROVAL_LIST, approvalNames);
		// The settings is stored in FunctionDescriptor.getArgs().
		// Transfer them to velocityParams for display
		if (descriptor instanceof FunctionDescriptor) {
			FunctionDescriptor fd = (FunctionDescriptor) descriptor;
			Map map = fd.getArgs();
			Object o = map.get(PARAM_APPROVAL_NAME);
			LOGGER.debug("getDescriptorParams <" + PARAM_APPROVAL_NAME + "> = <" + o + ">(" + ((o != null)?o.getClass():"N/A") + ")");
			if (o != null) {
				if (o instanceof String) {
					List list = null;
					try {
						list = OM.readValue((String) o, List.class);
					} catch (Exception ex) {
						LOGGER.error("Failed to deserialize " + PARAM_APPROVAL_NAME, ex);
					}
					velocityParams.put(PARAM_APPROVAL_NAME, list);
				} else {
					throw new IllegalArgumentException("Unsupported data type " + ((o != null)?o.getClass():"N/A") + " for " + PARAM_APPROVAL_NAME);
				}
			} 
		} else {
			throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
		}
		LOGGER.debug("getVelocityParamsForEdit returning");
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
		// params contains form data submitted by input/edit page.
		// Values are string arrays

		// The return value is used to construct FunctionDescriptor 
		// for getVelocityParamsForEdit and getVelocityParamsForView
		
		// The goal here is get the velocity template parameters from params
		// and convert them to Strings in a map
		// The map is provided to post function for execution
		Map<String, Object> result = new HashMap<>();
		String value = null;
		Object o = params.get(PARAM_APPROVAL_NAME);
		LOGGER.debug("getDescriptorParams <" + PARAM_APPROVAL_NAME + "> = <" + o + ">(" + ((o != null)?o.getClass():"N/A") + ")");
		if (o != null) {
			if (o instanceof String) {
				try {
					value = OM.writeValueAsString(new String[] {(String) o});
				} catch (Exception ex) {
					LOGGER.error("Failed to serialize " + PARAM_APPROVAL_NAME, ex);
				}
			} else if (o instanceof String[]) {
				try {
					value = OM.writeValueAsString(o);
				} catch (Exception ex) {
					LOGGER.error("Failed to serialize " + PARAM_APPROVAL_NAME, ex);
				}
			} else {
				throw new IllegalArgumentException("Unsupported data type " + ((o != null)?o.getClass():"N/A") + " for " + PARAM_APPROVAL_NAME);
			}
		}
		result.put(PARAM_APPROVAL_NAME, value);
		LOGGER.debug("getDescriptorParams returning");
		return result;
    }
	
	/**
	 * Extract approval names parameter.
	 * @param m Map
	 * @return Map of String to String[]
	 * @throws Exception Error parsing the arguments.
	 */
	public static String[] parseArguments(@SuppressWarnings("rawtypes") Map m) throws Exception {
		String[] value = null;
		Object o = m.get(PARAM_APPROVAL_NAME);
		if (o != null) {
			if (o instanceof String) {
				String v = (String) o;
				if (v.length() != 0) {
					String[] list = OM.readValue(v, String[].class);
					value = list;
				}
			} else {
				throw new Exception("Parameter " + PARAM_APPROVAL_NAME + " is not a string");
			}
		}
		return value;
	}
}
