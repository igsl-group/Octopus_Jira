package com.igsl.configmigration.workflow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.FunctionDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FunctionDescriptorDTO extends AbstractDescriptorDTO {

	private static final Logger LOGGER = Logger.getLogger(FunctionDescriptorDTO.class);
	
	private int entityId;
	private int id;
	private String name;
	private String type;
	private Map args;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		FunctionDescriptor fd = (FunctionDescriptor) obj; 
		this.args = fd.getArgs();
		this.entityId = fd.getEntityId();
		this.id = fd.getId();
		this.name = fd.getName();
		this.type = fd.getType();
		//fd.getParent();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getType",
				"getArgs");
	}

	@Override
	public Class<?> getJiraClass() {
		return FunctionDescriptor.class;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public String getUniqueKey() {
		return this.getName();
	}

	@Override
	public String getInternalId() {
		return Integer.toString(this.getId());
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

}
