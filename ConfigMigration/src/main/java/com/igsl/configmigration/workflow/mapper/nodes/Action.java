package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.igsl.configmigration.workflow.mapper.serialization.EmptyFilter;

@JacksonXmlRootElement(localName = "action")
@JsonPropertyOrder({"meta", "restrict-to", "validators", "results"})
public class Action {
	@JacksonXmlProperty(isAttribute = true)
	private String id;
	@JacksonXmlProperty(isAttribute = true)
	private String name;
	@JacksonXmlProperty(isAttribute = true)
	private String view;
	@JacksonXmlProperty(isAttribute = true)
	private String auto;
	@JacksonXmlProperty(isAttribute = true)
	private String finish;
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "meta")
	private List<Meta> metas = new ArrayList<>();
	@JacksonXmlProperty(localName = "validators")
	private Validators validators;
	@JacksonXmlElementWrapper(localName = "results")
	@JacksonXmlProperty(localName = "unconditional-result")
	private List<UnconditionalResult> results = new ArrayList<>();
	@JacksonXmlProperty(localName = "restrict-to")
	private RestrictTo restrictTo;
	@JacksonXmlProperty(localName = "pre-functions")
	private Functions preFunctions;
	@JacksonXmlProperty(localName = "post-functions")
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
	public List<UnconditionalResult> getResults() {
		return results;
	}
	public void setResults(List<UnconditionalResult> results) {
		this.results = results;
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
}
