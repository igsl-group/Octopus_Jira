package com.igsl.configmigration.workflow.mapper.nodes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.igsl.configmigration.workflow.mapper.serialization.EmptyCheck;

@JacksonXmlRootElement(localName = "registers")
public class Registers implements EmptyCheck {
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "register")
	private List<Register> registers = new ArrayList<>();
	@Override
	public boolean isEmpty() {
		return (registers.size() == 0);
	}
	public List<Register> getRegisters() {
		return registers;
	}
	public void setRegisters(List<Register> registers) {
		this.registers = registers;
	}
}
