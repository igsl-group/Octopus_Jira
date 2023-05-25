package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.igsl.configmigration.workflow.mapper.serialization.EmptyCheck;

@JacksonXmlRootElement(localName = "trigger-functions")
public class TriggerFunctions implements EmptyCheck {
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "trigger-function")
	private List<TriggerFunction> triggerFunctions = new ArrayList<>();
	@Override
	public boolean isEmpty() {
		return (triggerFunctions.size() == 0);
	}
	public List<TriggerFunction> getTriggerFunctions() {
		return triggerFunctions;
	}
	public void setTriggerFunctions(List<TriggerFunction> triggerFunctions) {
		this.triggerFunctions = triggerFunctions;
	}
}
