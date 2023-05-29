package com.igsl.configmigration.workflow.mapper.nodes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "join")
@XmlAccessorType(XmlAccessType.FIELD)
public class Join {
	@XmlAttribute
	private String id;
	@XmlElement(name = "conditions")
	private Conditions conditions;	
	@XmlElement(name = "unconditional-result")
	private UnconditionalResult unconditionalResultList;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Conditions getConditions() {
		return conditions;
	}
	public void setConditions(Conditions conditions) {
		this.conditions = conditions;
	}
	public UnconditionalResult getUnconditionalResultList() {
		return unconditionalResultList;
	}
	public void setUnconditionalResultList(UnconditionalResult unconditionalResultList) {
		this.unconditionalResultList = unconditionalResultList;
	}
}
