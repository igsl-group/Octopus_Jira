package com.igsl.configmigration.workflow.mapper.nodes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "register")
public class Register {
	@JacksonXmlProperty(isAttribute = true)
	private String type;
	@JacksonXmlProperty(isAttribute = true, localName = "variable-name")
	private String variableName;
	@JacksonXmlProperty(isAttribute = true)
	private String id;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getVariableName() {
		return variableName;
	}
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
