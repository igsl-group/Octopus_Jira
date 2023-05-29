package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "action", propOrder = {"metas", "restrictTo", "validators", "preFunctions", "postFunctions", "results"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Action {
	@XmlAttribute
	private String id;
	@XmlAttribute
	private String name;
	@XmlAttribute
	private String view;
	@XmlAttribute
	private String auto;
	@XmlAttribute
	private String finish;
	@XmlElement(name = "meta")
	private List<Meta> metas = new ArrayList<>();
	@XmlElement(name = "validators")
	private Validators validators;
	@XmlElement(name = "results")
	private Results results;
	@XmlElement(name = "restrict-to")
	private RestrictTo restrictTo;
	@XmlElement(name = "pre-functions")
	private Functions preFunctions;
	@XmlElement(name = "post-functions")
	private Functions postFunctions;	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Meta> getMetas() {
		return metas;
	}
	public void setMetas(List<Meta> metas) {
		this.metas = metas;
	}
	public Validators getValidators() {
		return validators;
	}
	public void setValidators(Validators validators) {
		this.validators = validators;
	}
	public RestrictTo getRestrictTo() {
		return restrictTo;
	}
	public void setRestrictTo(RestrictTo restrictTo) {
		this.restrictTo = restrictTo;
	}
	public String getView() {
		return view;
	}
	public void setView(String view) {
		this.view = view;
	}
	public String getAuto() {
		return auto;
	}
	public void setAuto(String auto) {
		this.auto = auto;
	}
	public String getFinish() {
		return finish;
	}
	public void setFinish(String finish) {
		this.finish = finish;
	}
	public Functions getPreFunctions() {
		return preFunctions;
	}
	public void setPreFunctions(Functions preFunctions) {
		this.preFunctions = preFunctions;
	}
	public Functions getPostFunctions() {
		return postFunctions;
	}
	public void setPostFunctions(Functions postFunctions) {
		this.postFunctions = postFunctions;
	}
	public Results getResults() {
		return results;
	}
	public void setResults(Results results) {
		this.results = results;
	}
}
