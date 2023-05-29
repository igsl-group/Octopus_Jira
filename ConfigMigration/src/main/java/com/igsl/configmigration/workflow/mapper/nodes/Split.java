package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "split")
@XmlAccessorType(XmlAccessType.FIELD)
public class Split {
	@XmlAttribute
	private String id;
	@XmlElement(name = "unconditional-result")
	private List<UnconditionalResult> unconditionalResultList = new ArrayList<>();

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<UnconditionalResult> getUnconditionalResultList() {
		return unconditionalResultList;
	}
	public void setUnconditionalResultList(List<UnconditionalResult> unconditionalResultList) {
		this.unconditionalResultList = unconditionalResultList;
	}
}
