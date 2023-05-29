package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "step")
@XmlAccessorType(XmlAccessType.FIELD)
public class Step {
	@XmlAttribute
	private String id;
	@XmlAttribute
	private String name;
	@XmlElement(name = "meta")
	private List<Meta> metas = new ArrayList<>();
	@XmlElement(name = "actions")
	private Actions actions;
	@XmlElement(name = "pre-functions")
	private Functions preFunctions;
	@XmlElement(name = "post-functions")
	private Functions postFunctions;
	@XmlElement(name = "external-permissions")
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
