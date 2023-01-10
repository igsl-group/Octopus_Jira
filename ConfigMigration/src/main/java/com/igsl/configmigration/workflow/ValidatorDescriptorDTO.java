package com.igsl.configmigration.workflow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ValidatorDescriptorDTO extends AbstractDescriptorDTO {
	
	private int entityId;
	private int id;
	private Map args;
	private String name;
	private String type;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		ValidatorDescriptor o = (ValidatorDescriptor) obj;
		this.args = o.getArgs();
		this.entityId = o.getEntityId();
		this.id = o.getId();
		this.name = o.getName();
		//o.getParent();
		this.type = o.getType();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getArgs",
				"getName",
				"getType");
	}

	@Override
	public Class<?> getJiraClass() {
		return ValidatorDescriptor.class;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public String getUniqueKey() {
		return Integer.toString(this.getEntityId());
	}

	@Override
	public String getInternalId() {
		return this.getUniqueKey();
	}

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Map getArgs() {
		return args;
	}

	public void setArgs(Map args) {
		this.args = args;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
