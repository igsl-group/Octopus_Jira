package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "validator")
public class Validator {
	@JacksonXmlProperty(isAttribute = true)
	private String id;
	@JacksonXmlProperty(isAttribute = true)
	private String name;
	@JacksonXmlProperty(isAttribute = true)
	private String type;
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "arg")
	private List<Arg> args = new ArrayList<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<Arg> getArgs() {
		return args;
	}
	public void setArgs(List<Arg> args) {
		this.args = args;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
