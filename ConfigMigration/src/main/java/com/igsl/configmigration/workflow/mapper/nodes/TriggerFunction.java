package com.igsl.configmigration.workflow.mapper.nodes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "trigger-function")
public class TriggerFunction {
	@JacksonXmlProperty
	private String id;
	@JacksonXmlProperty(localName = "function")
	private Function function;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Function getFunction() {
		return function;
	}
	public void setFunction(Function function) {
		this.function = function;
	}
}
