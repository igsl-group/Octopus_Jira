package com.igsl.configmigration.workflow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.RegisterDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class RegisterDescriptorDTO extends AbstractDescriptorDTO {

	private int entityId;
	private int id;
	private String type;
	private String variableName;
	private Map args;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		RegisterDescriptor o = (RegisterDescriptor) obj;
		this.args = o.getArgs();
		this.entityId = o.getEntityId();
		this.id = o.getId();
		this.type = o.getType();
		this.variableName = o.getVariableName();
		this.uniqueKey = Integer.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getType",
				"getVariableName",
				"getArgs");
	}

	@Override
	public Class<?> getJiraClass() {
		return RegisterDescriptor.class;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
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
