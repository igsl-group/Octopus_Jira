package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "joins")
@XmlAccessorType(XmlAccessType.FIELD)
public class Joins {
	@XmlElement(name = "join")
	private List<Join> joinList = new ArrayList<>();
	
	public List<Join> getJoinList() {
		return joinList;
	}
	public void setJoinList(List<Join> joinList) {
		this.joinList = joinList;
	}
}
