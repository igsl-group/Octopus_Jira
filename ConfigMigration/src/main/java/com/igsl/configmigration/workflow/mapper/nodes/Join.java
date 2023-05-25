package com.igsl.configmigration.workflow.mapper.nodes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "join")
public class Join {
	@JacksonXmlProperty(isAttribute = true)
	private String id;
	@JacksonXmlProperty(localName = "conditions")
	private Conditions conditions;	
	@JacksonXmlProperty(localName = "unconditional-result")
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
