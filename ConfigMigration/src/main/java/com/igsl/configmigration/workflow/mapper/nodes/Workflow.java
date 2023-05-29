package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "workflow")
@XmlAccessorType(XmlAccessType.FIELD)
public class Workflow {
	
	@XmlElement(name = "meta")
	private List<Meta> metaList = new ArrayList<>();
	
	@XmlElement(name = "registers")
	private Registers registers;

	@XmlElement(name = "trigger-functions")
	private TriggerFunctions triggerFunctions;
	
	@XmlElement(name = "global-conditions")
	private GlobalConditions globalConditions;
	
	@XmlElement(name = "initial-actions")
	private Actions initialActions;
	
	@XmlElement(name = "global-actions")
	private Actions globalActions;
	
	@XmlElement(name = "common-actions")
	private Actions commonActions;
	
	@XmlElement(name = "steps")
	private Steps steps;
	
	@XmlElement(name = "splits")
	private Splits splits;
	
	@XmlElement(name = "joins")
	private Joins joins;
	
	public Actions getInitialActions() {
		return initialActions;
	}
	public void setInitialActions(Actions initialActions) {
		this.initialActions = initialActions;
	}
	public Actions getGlobalActions() {
		return globalActions;
	}
	public void setGlobalActions(Actions globalActions) {
		this.globalActions = globalActions;
	}
	public Steps getSteps() {
		return steps;
	}
	public void setSteps(Steps steps) {
		this.steps = steps;
	}
	public Registers getRegisters() {
		return registers;
	}
	public void setRegisters(Registers registers) {
		this.registers = registers;
	}
	public TriggerFunctions getTriggerFunctions() {
		return triggerFunctions;
	}
	public void setTriggerFunctions(TriggerFunctions triggerFunctions) {
		this.triggerFunctions = triggerFunctions;
	}
	public GlobalConditions getGlobalConditions() {
		return globalConditions;
	}
	public void setGlobalConditions(GlobalConditions globalConditions) {
		this.globalConditions = globalConditions;
	}
	public Actions getCommonActions() {
		return commonActions;
	}
	public void setCommonActions(Actions commonActions) {
		this.commonActions = commonActions;
	}
	public Splits getSplits() {
		return splits;
	}
	public void setSplits(Splits splits) {
		this.splits = splits;
	}
	public Joins getJoins() {
		return joins;
	}
	public void setJoins(Joins joins) {
		this.joins = joins;
	}
	public List<Meta> getMetaList() {
		return metaList;
	}
	public void setMetaList(List<Meta> metaList) {
		this.metaList = metaList;
	}
}
