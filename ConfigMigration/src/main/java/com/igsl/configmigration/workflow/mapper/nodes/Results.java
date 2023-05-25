package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "results")
public class Results {
	@JacksonXmlProperty(localName = "unconditional-result")
	private UnconditionalResult unconditionalResult;
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "result")
	private List<Result> resultList;
	public UnconditionalResult getUnconditionalResult() {
		return unconditionalResult;
	}
	public void setUnconditionalResult(UnconditionalResult unconditionalResult) {
		this.unconditionalResult = unconditionalResult;
	}
	public List<Result> getResultList() {
		return resultList;
	}
	public void setResultList(List<Result> resultList) {
		this.resultList = resultList;
	}
}
