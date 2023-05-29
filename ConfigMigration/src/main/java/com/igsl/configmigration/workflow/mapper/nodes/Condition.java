package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "condition")
@XmlAccessorType(XmlAccessType.FIELD)
public class Condition {
	@XmlAttribute
	private String type;
	@XmlAttribute
	private String id;
	@XmlAttribute
	private String name;
	@XmlAttribute
	private String negate;
	@XmlElement(name = "arg")
	private List<Arg> args = new ArrayList<>();
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<Arg> getArgs() {
		return args;
	}
	public void setArgs(List<Arg> args) {
		this.args = args;
	}
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
	public String getNegate() {
		return negate;
	}
	public void setNegate(String negate) {
		this.negate = negate;
	}
}
