package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.igsl.configmigration.workflow.mapper.serialization.XMLDocumentHeader;

@JacksonXmlRootElement(localName = "workflow")
@JsonPropertyOrder({"meta", "initial-actions", "global-actions", "steps"})
public class Workflow implements XMLDocumentHeader {
	
	@JacksonXmlElementWrapper(useWrapping = false)	// For repeated elements
	@JacksonXmlProperty(localName = "meta")
	private List<Meta> metaList = new ArrayList<>();
	
	@JacksonXmlProperty(localName = "registers")
	private Registers registers;

	@JacksonXmlProperty(localName = "trigger-functions")
	private TriggerFunctions triggerFunctions;
	
	@JacksonXmlProperty(localName = "global-conditions")
	private GlobalConditions globalConditions;
	
	@JacksonXmlProperty(localName = "initial-actions")
	private Actions initialActions;
	
	@JacksonXmlProperty(localName = "global-actions")
	private Actions globalActions;
	
	@JacksonXmlProperty(localName = "common-actions")
	private Actions commonActions;
	
	@JacksonXmlProperty(localName = "steps")
	private Steps steps;
	
	@JacksonXmlProperty(localName = "splits")
	private Splits splits;
	
	@JacksonXmlProperty(localName = "joins")
	private Joins joins;
	
	// To add XML headers
	@JsonIgnore
	@Override
	public List<String> getHeaders() {
		return Arrays.asList(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", 
				"<!DOCTYPE workflow PUBLIC \"-//OpenSymphony Group//DTD OSWorkflow 2.8//EN\" \"http://www.opensymphony.com/osworkflow/workflow_2_8.dtd\">");
	}
	
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
