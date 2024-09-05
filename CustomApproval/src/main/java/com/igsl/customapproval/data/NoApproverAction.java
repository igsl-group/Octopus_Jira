package com.igsl.customapproval.data;

public enum NoApproverAction {
	NO_ACTION,
	APPROVE,
	REJECT;
	public static NoApproverAction parse(String value) {
		for (NoApproverAction action : NoApproverAction.values()) {
			if (action.toString().equals(value)) {
				return action;
			}
		}
		return null;
	}
}
