package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "trigger-functions")
@XmlAccessorType(XmlAccessType.FIELD)
public class TriggerFunctions {
	@XmlElement(name = "trigger-function")
	private List<TriggerFunction> triggerFunctions = new ArrayList<>();

	public List<TriggerFunction> getTriggerFunctions() {
		return triggerFunctions;
	}
	public void setTriggerFunctions(List<TriggerFunction> triggerFunctions) {
		this.triggerFunctions = triggerFunctions;
	}
}
