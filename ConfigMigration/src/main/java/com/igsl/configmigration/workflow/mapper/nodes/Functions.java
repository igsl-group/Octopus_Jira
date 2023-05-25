package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.igsl.configmigration.workflow.mapper.serialization.EmptyCheck;

@JacksonXmlRootElement(localName = "functions")
public class Functions implements EmptyCheck {
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "function")
	private List<Function> functions = new ArrayList<>();

	@Override
	public boolean isEmpty() {
		return (functions.size() == 0);
	}
	
	public List<Function> getFunctions() {
		return functions;
	}
	public void setFunctions(List<Function> functions) {
		this.functions = functions;
	}
}
