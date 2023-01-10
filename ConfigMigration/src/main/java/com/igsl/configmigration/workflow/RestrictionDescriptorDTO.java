package com.igsl.configmigration.workflow;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.RestrictionDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class RestrictionDescriptorDTO extends AbstractDescriptorDTO {

	private int entityId;
	private int id;
	private ConditionsDescriptorDTO conditionsDescriptor;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		RestrictionDescriptor rd = (RestrictionDescriptor) obj;
		this.conditionsDescriptor = new ConditionsDescriptorDTO();
		this.conditionsDescriptor.setJiraObject(rd.getConditionsDescriptor());
		this.entityId = rd.getEntityId();
		this.id = rd.getId();
		rd.getParent();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getConditionalDescriptor");
	}

	@Override
	public Class<?> getJiraClass() {
		return RestrictionDescriptor.class;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public String getUniqueKey() {
		return Integer.toString(this.getId());
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

	public ConditionsDescriptorDTO getConditionalDescriptor() {
		return conditionsDescriptor;
	}

	public void setConditionalDescriptor(ConditionsDescriptorDTO conditionalDescriptor) {
		this.conditionsDescriptor = conditionalDescriptor;
	}

}
