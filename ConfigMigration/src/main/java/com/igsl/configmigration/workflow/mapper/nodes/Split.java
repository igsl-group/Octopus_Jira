package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "split")
public class Split {
	@JacksonXmlProperty(isAttribute = true)
	private String id;
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "unconditional-result")
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
