package com.igsl.configmigration.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class StepDescriptorDTO extends AbstractDescriptorDTO {
	
	private static final Logger LOGGER = Logger.getLogger(StepDescriptorDTO.class);
	
	private List<JiraConfigDTO> actions;
	private List<JiraConfigDTO> commonActions;
	private int entityId;
	private int id;
	private Map metaAttributes;
	private String name;
	private List<JiraConfigDTO> postFunctions;
	private List<JiraConfigDTO> preFunctions;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		StepDescriptor sd = (StepDescriptor) obj;
		this.actions = new ArrayList<>();
		for (Object item : sd.getActions()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.actions.add(dto);
			}
		}
		this.commonActions = new ArrayList<>();
		for (Object item : sd.getCommonActions()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.commonActions.add(dto);
			}
		}
		this.entityId = sd.getEntityId();
		this.id = sd.getId();
		this.metaAttributes = sd.getMetaAttributes();
		this.name = sd.getName();
		//sd.getParent();
		//sd.getPermissions();
		
		// TODO
		if (sd.getPermissions() != null && sd.getPermissions().size() != 0) {
			LOGGER.debug("Permission class: " + sd.getPermissions().get(0).getClass());
		}
		
		this.postFunctions = new ArrayList<>();
		for (Object item : sd.getPostFunctions()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.postFunctions.add(dto);
			}
		}
		this.preFunctions = new ArrayList<>();
		for (Object item : sd.getPreFunctions()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.preFunctions.add(dto);
			}
		}
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

	public List<JiraConfigDTO> getActions() {
		return actions;
	}

	public void setActions(List<JiraConfigDTO> actions) {
		this.actions = actions;
	}

	public List<JiraConfigDTO> getCommonActions() {
		return commonActions;
	}

	public void setCommonActions(List<JiraConfigDTO> commonActions) {
		this.commonActions = commonActions;
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

	public Map getMetaAttributes() {
		return metaAttributes;
	}

	public void setMetaAttributes(Map metaAttributes) {
		this.metaAttributes = metaAttributes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<JiraConfigDTO> getPostFunctions() {
		return postFunctions;
	}

	public void setPostFunctions(List<JiraConfigDTO> postFunctions) {
		this.postFunctions = postFunctions;
	}

	public List<JiraConfigDTO> getPreFunctions() {
		return preFunctions;
	}

	public void setPreFunctions(List<JiraConfigDTO> preFunctions) {
		this.preFunctions = preFunctions;
	}

}
