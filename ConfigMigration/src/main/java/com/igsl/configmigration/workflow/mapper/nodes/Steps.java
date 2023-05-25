package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.igsl.configmigration.workflow.mapper.serialization.EmptyCheck;

@JacksonXmlRootElement(localName = "steps")
public class Steps implements EmptyCheck {
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "step")
	private List<Step> steps = new ArrayList<>();

	@Override
	public boolean isEmpty() {
		return (steps.size() == 0);
	}
	
	public List<Step> getSteps() {
		return steps;
	}
	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}
}
