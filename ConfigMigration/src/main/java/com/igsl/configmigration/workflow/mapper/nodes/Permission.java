package com.igsl.configmigration.workflow.mapper.nodes;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "permission")
public class Permission {
	@JacksonXmlProperty(isAttribute = true)
	private String id;
	@JacksonXmlProperty(isAttribute = true)
	private String name;
	@JacksonXmlProperty(localName = "restrict-to")
	private RestrictTo restrictTo;
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
	public RestrictTo getRestrictTo() {
		return restrictTo;
	}
	public void setRestrictTo(RestrictTo restrictTo) {
		this.restrictTo = restrictTo;
	}
}
