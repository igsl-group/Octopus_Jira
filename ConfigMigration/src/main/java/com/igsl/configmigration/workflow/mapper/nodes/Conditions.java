package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.igsl.configmigration.workflow.mapper.serialization.EmptyCheck;

@JacksonXmlRootElement(localName = "conditions")
public class Conditions implements EmptyCheck {
	@JacksonXmlProperty(isAttribute = true)
	private String type;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "conditions")
	private List<Conditions> conditionsList = new ArrayList<>();
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "condition")
	private List<Condition> conditionList = new ArrayList<>();
	@Override
	public boolean isEmpty() {
		return (conditionsList.size() == 0) && (conditionList.size() == 0);
	}
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
