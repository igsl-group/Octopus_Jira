package com.igsl.configmigration.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.JoinDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class JoinDescriptorDTO extends AbstractDescriptorDTO {
	
	private int entityId;
	private int id;
	private List<JiraConfigDTO> conditions;
	private ResultDescriptorDTO result;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		JoinDescriptor o = (JoinDescriptor) obj;
		this.conditions = new ArrayList<>();
		for (Object item : o.getConditions()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.conditions.add(dto);
			}
		}
		this.entityId = o.getEntityId();
		this.id = o.getId();
		//o.getParent();
		this.result = new ResultDescriptorDTO();
		this.result.setJiraObject(o.getResult());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList();
	}

	@Override
	public Class<?> getJiraClass() {
		return JoinDescriptor.class;
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

	public List<JiraConfigDTO> getConditions() {
		return conditions;
	}

	public void setConditions(List<JiraConfigDTO> conditions) {
		this.conditions = conditions;
	}

	public ResultDescriptorDTO getResult() {
		return result;
	}

	public void setResult(ResultDescriptorDTO result) {
		this.result = result;
	}

}
