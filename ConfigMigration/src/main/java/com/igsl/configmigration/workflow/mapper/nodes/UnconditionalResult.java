package com.igsl.configmigration.workflow.mapper.nodes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "unconditional-result")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnconditionalResult {
	@XmlAttribute(name = "old-status")
	private String oldStatus;
	@XmlAttribute
	private String status;
	@XmlAttribute
	private String step;
	@XmlAttribute(name = "owner")
	private String owner;
	@XmlAttribute(name = "split")
	private String split;
	@XmlAttribute(name = "join")
	private String join;
	@XmlAttribute(name = "due-date")
	private String dueDate;
	@XmlAttribute(name = "id")
	private String id;
	@XmlAttribute(name = "display-name")
	private String displayName;
	@XmlElement(name = "validators")
	private Validators validators;
	@XmlElement(name = "pre-functions")
	private Functions preFunctions;
	@XmlElement(name = "post-functions")
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
	public Functions getPostFunctions() {
		return postFunctions;
	}
	public void setPostFunctions(Functions postFunctions) {
		this.postFunctions = postFunctions;
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
}
