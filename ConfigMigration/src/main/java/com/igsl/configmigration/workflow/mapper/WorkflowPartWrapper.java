package com.igsl.configmigration.workflow.mapper;

public class WorkflowPartWrapper {
	private WorkflowPart workflowPart;
	private String parentXPath;
	public WorkflowPartWrapper(WorkflowPart workflowPart, String parentXPath) {
		this.workflowPart = workflowPart;
		this.parentXPath = parentXPath;
	}
	public String getXPath() {
		if (this.workflowPart != null) {
			return this.workflowPart.getAbsoluteXPath(this.parentXPath);
		}
		return "";
	}
	
	public WorkflowPart getWorkflowPart() {
		return workflowPart;
	}
	public void setWorkflowPart(WorkflowPart workflowPart) {
		this.workflowPart = workflowPart;
	}
	public String getParentXPath() {
		return parentXPath;
	}
	public void setParentXPath(String parentXPath) {
		this.parentXPath = parentXPath;
	}
}
