package com.igsl.configmigration.workflow.mapper.nodes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "trigger-function")
@XmlAccessorType(XmlAccessType.FIELD)
public class TriggerFunction {
	@XmlAttribute
	private String id;
	@XmlElement(name = "function")
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
