package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.igsl.configmigration.workflow.mapper.serialization.EmptyCheck;

@JacksonXmlRootElement(localName = "joins")
public class Joins implements EmptyCheck {
	@JacksonXmlProperty(localName = "join")
	private List<Join> joinList = new ArrayList<>();
	@Override
	public boolean isEmpty() {
		return (joinList.size() == 0);
	}
	public List<Join> getJoinList() {
		return joinList;
	}
	public void setJoinList(List<Join> joinList) {
		this.joinList = joinList;
	}
}
