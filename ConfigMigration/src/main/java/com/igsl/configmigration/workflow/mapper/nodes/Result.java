package com.igsl.configmigration.workflow.mapper.nodes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "result")
public class Result {
	@JacksonXmlProperty(isAttribute = true, localName = "old-status")
	private String oldStatus;
	@JacksonXmlProperty(isAttribute = true, localName = "status")
	private String status;
	@JacksonXmlProperty(isAttribute = true, localName = "step")
	private String step;
	@JacksonXmlProperty(isAttribute = true, localName = "owner")
	private String owner;
	@JacksonXmlProperty(isAttribute = true, localName = "split")
	private String split;
	@JacksonXmlProperty(isAttribute = true, localName = "join")
	private String join;
	@JacksonXmlProperty(isAttribute = true, localName = "due-date")
	private String dueDate;
	@JacksonXmlProperty(isAttribute = true, localName = "id")
	private String id;
	@JacksonXmlProperty(isAttribute = true, localName = "display-name")
	private String displayName;
	@JacksonXmlProperty(localName = "conditions")
	private Conditions conditions;
	@JacksonXmlProperty(localName = "validators")
	private Validators validators;
	@JacksonXmlProperty(localName = "pre-functions")
	private Functions preFunctions;
	@JacksonXmlProperty(localName = "post-functions")
	private Functions postFunctions;
	public String getOldStatus() {
		return oldStatus;
	}
	public void setOldStatus(String oldStatus) {
		this.oldStatus = oldStatus;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStep() {
		return step;
	}
	public void setStep(String step) {
		this.step = step;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getSplit() {
		return split;
	}
	public void setSplit(String split) {
		this.split = split;
	}
	public String getJoin() {
		return join;
	}
	public void setJoin(String join) {
		this.join = join;
	}
	public String getDueDate() {
		return dueDate;
	}
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public Conditions getConditions() {
		return conditions;
	}
	public void setConditions(Conditions conditions) {
		this.conditions = conditions;
	}
	public Validators getValidators() {
		return validators;
	}
	public void setValidators(Validators validators) {
		this.validators = validators;
	}
	public Functions getPreFunctions() {
		return preFunctions;
	}
	public void setPreFunctions(Functions preFunctions) {
		this.preFunctions = preFunctions;
	}
	public Functions getPostFunctions() {
		return postFunctions;
	}
	public void setPostFunctions(Functions postFunctions) {
		this.postFunctions = postFunctions;
	}
}
