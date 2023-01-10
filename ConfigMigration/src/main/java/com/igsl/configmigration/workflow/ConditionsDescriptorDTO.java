package com.igsl.configmigration.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.ConditionsDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ConditionsDescriptorDTO extends AbstractDescriptorDTO {
	
	private int entityId;
	private int id;
	private String type;
	private List<JiraConfigDTO> conditions;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		ConditionsDescriptor cd = (ConditionsDescriptor) obj;
		this.conditions = new ArrayList<>();
		for (Object item : cd.getConditions()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.conditions.add(dto);
			}
		}
		this.entityId = cd.getEntityId();
		this.id = cd.getId();
		//cd.getParent();
		this.type = cd.getType();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList();
	}

	@Override
	public Class<?> getJiraClass() {
		return ConditionsDescriptor.class;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<JiraConfigDTO> getConditions() {
		return conditions;
	}

	public void setConditions(List<JiraConfigDTO> conditions) {
		this.conditions = conditions;
	}

}
