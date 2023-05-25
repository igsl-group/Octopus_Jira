package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.igsl.configmigration.workflow.mapper.serialization.EmptyCheck;

@JacksonXmlRootElement(localName = "validators")
public class Validators implements EmptyCheck {
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "validator")
	private List<Validator> validators = new ArrayList<>();

	@Override
	public boolean isEmpty() {
		return (validators.size() == 0);
	}	

	public List<Validator> getValidators() {
		return validators;
	}
	public void setValidators(List<Validator> validators) {
		this.validators = validators;
	}
}
