package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "results")
@XmlAccessorType(XmlAccessType.FIELD)
public class Results {
	@XmlElement(name = "unconditional-result")
	private UnconditionalResult unconditionalResult;
	@XmlElement(name = "result")
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
