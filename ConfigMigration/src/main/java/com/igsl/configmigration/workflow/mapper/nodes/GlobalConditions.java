package com.igsl.configmigration.workflow.mapper.nodes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "global-conditions")
@XmlAccessorType(XmlAccessType.FIELD)
public class GlobalConditions {
	@XmlElement(name = "conditions")
	private Conditions conditions;

	public Conditions getConditions() {
		return conditions;
	}
	public void setConditions(Conditions conditions) {
		this.conditions = conditions;
	}
}
