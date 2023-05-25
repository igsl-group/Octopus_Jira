package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.igsl.configmigration.workflow.mapper.serialization.EmptyCheck;

@JacksonXmlRootElement(localName = "splits")
public class Splits implements EmptyCheck {
	@JacksonXmlProperty(localName = "split")
	private List<Split> splitList = new ArrayList<>();
	@Override
	public boolean isEmpty() {
		return (splitList.size() == 0);
	}
	public List<Split> getSplitList() {
		return splitList;
	}
	public void setSplitList(List<Split> splitList) {
		this.splitList = splitList;
	}
}
