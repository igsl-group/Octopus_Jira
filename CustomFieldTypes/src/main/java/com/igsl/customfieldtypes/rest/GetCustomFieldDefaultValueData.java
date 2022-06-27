package com.igsl.customfieldtypes.rest;

public class GetCustomFieldDefaultValueData {
	
	// WS RS only find properties if public, public getter doesn't work?! Related to Plugin class loader?
	public String issueKey;
	public String customFieldId;
	public Object result;
	
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public String getIssueKey() {
		return issueKey;
	}
	public void setIssueKey(String issueKey) {
		this.issueKey = issueKey;
	}
	public String getCustomFieldId() {
		return customFieldId;
	}
	public void setCustomFieldId(String customFieldId) {
		this.customFieldId = customFieldId;
	}

}
