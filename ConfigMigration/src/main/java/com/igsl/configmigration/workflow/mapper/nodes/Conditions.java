package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "conditions")
@XmlAccessorType(XmlAccessType.FIELD)
public class Conditions {
	@XmlAttribute
	private String type;
	
	@XmlElement(name = "conditions")
	private List<Conditions> conditionsList = new ArrayList<>();
	
	@XmlElement(name = "condition")
	private List<Condition> conditionList = new ArrayList<>();
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<Conditions> getConditionsList() {
		return conditionsList;
	}
	public void setConditionsList(List<Conditions> conditionsList) {
		this.conditionsList = conditionsList;
	}
	public List<Condition> getConditionList() {
		return conditionList;
	}
	public void setConditionList(List<Condition> conditionList) {
		this.conditionList = conditionList;
	}
}
