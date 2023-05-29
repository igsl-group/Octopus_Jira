package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "splits")
@XmlAccessorType(XmlAccessType.FIELD)
public class Splits {
	@XmlElement(name = "split")
	private List<Split> splitList = new ArrayList<>();

	public List<Split> getSplitList() {
		return splitList;
	}
	public void setSplitList(List<Split> splitList) {
		this.splitList = splitList;
	}
}
