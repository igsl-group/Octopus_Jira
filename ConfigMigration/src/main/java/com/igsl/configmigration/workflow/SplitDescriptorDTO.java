package com.igsl.configmigration.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.RegisterDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import com.opensymphony.workflow.loader.SplitDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class SplitDescriptorDTO extends AbstractDescriptorDTO {

	private int entityId;
	private int id;
	private List<JiraConfigDTO> results;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		SplitDescriptor o = (SplitDescriptor) obj;
		this.entityId = o.getEntityId();
		this.id = o.getId();
		this.results = new ArrayList<>();
		for (Object item : o.getResults()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.results.add(dto);
			}
		}
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getResults");
	}

	@Override
	public Class<?> getJiraClass() {
		return SplitDescriptor.class;
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

	public List<JiraConfigDTO> getResults() {
		return results;
	}

	public void setResults(List<JiraConfigDTO> results) {
		this.results = results;
	}

}
