package com.igsl.configmigration.workflow.mapper.nodes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "global-conditions")
public class GlobalConditions {
	@JacksonXmlProperty(localName = "conditions")
	private Conditions conditions;
	public Conditions getConditions() {
		return conditions;
	}
	public void setConditions(Conditions conditions) {
		this.conditions = conditions;
	}
}
