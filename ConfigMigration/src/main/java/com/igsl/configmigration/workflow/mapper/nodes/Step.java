package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "step")
public class Step {
	@JacksonXmlProperty(isAttribute = true)
	private String id;
	@JacksonXmlProperty(isAttribute = true)
	private String name;
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "meta")
	private List<Meta> metas = new ArrayList<>();
	@JacksonXmlProperty(localName = "actions")
	private Actions actions;
	@JacksonXmlProperty(localName = "pre-functions")
	private Functions preFunctions;
	@JacksonXmlProperty(localName = "post-functions")
	private Functions postFunctions;
	@JacksonXmlProperty(localName = "external-permissions")
	private ExternalPermissions externalPermissions;	
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
	public Actions getActions() {
		return actions;
	}
	public void setActions(Actions actions) {
		this.actions = actions;
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
	public ExternalPermissions getExternalPermissions() {
		return externalPermissions;
	}
	public void setExternalPermissions(ExternalPermissions externalPermissions) {
		this.externalPermissions = externalPermissions;
	}
}
