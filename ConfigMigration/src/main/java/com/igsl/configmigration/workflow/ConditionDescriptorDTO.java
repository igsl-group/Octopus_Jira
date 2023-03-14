package com.igsl.configmigration.workflow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.ConditionDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ConditionDescriptorDTO extends AbstractDescriptorDTO {
	
	private int entityId;
	private int id;
	private String name;
	private String type;
	private Map args;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		ConditionDescriptor cd = (ConditionDescriptor) obj;
		this.args = cd.getArgs();
		this.entityId = cd.getEntityId();
		this.id = cd.getId();
		this.name = cd.getName();
		//cd.getParent();
		this.type = cd.getType();
		this.uniqueKey = Long.toString(this.entityId);
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList();
	}

	@Override
	public Class<?> getJiraClass() {
		return ConditionDescriptor.class;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
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

	public Map getArgs() {
		return args;
	}

	public void setArgs(Map args) {
		this.args = args;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		// TODO Auto-generated method stub
		return null;
	}

}
