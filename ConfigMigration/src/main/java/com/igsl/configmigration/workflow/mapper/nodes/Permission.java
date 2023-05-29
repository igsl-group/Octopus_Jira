package com.igsl.configmigration.workflow.mapper.nodes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "permission")
@XmlAccessorType(XmlAccessType.FIELD)
public class Permission {
	@XmlAttribute
	private String id;
	@XmlAttribute
	private String name;
	@XmlElement(name = "restrict-to")

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
