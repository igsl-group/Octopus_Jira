package com.igsl.configmigration.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class WorkflowDescriptorDTO extends AbstractDescriptorDTO {

	private static final Logger LOGGER = Logger.getLogger(WorkflowDescriptorDTO.class);
	
	private int entityId;
	private int id;
	private String name;
	private List<JiraConfigDTO> globalActions;
	private ConditionsDescriptorDTO globalConditions;
	private List<JiraConfigDTO> initialActions;
	private List<JiraConfigDTO> joins;
	private Map metaAttributes;
	private List<JiraConfigDTO> registers;
	private List<JiraConfigDTO> splits;
	private List<JiraConfigDTO> steps;
	private Map triggerFunctions;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		WorkflowDescriptor wfd = (WorkflowDescriptor) obj; 
		this.entityId = wfd.getEntityId();
		this.globalActions = new ArrayList<>();
		for (Object item : wfd.getGlobalActions()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.globalActions.add(dto);
			}
		}
		this.globalConditions = new ConditionsDescriptorDTO();
		this.globalConditions.setJiraObject(wfd.getGlobalConditions());
		this.id = wfd.getId();
		this.initialActions = new ArrayList<>();
		for (Object item : wfd.getInitialActions()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			this.initialActions.add(dto);
		}
		this.joins = new ArrayList<>();
		for (Object item : wfd.getJoins()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.joins.add(dto);
			}
		}
		this.metaAttributes = wfd.getMetaAttributes();
		this.name = wfd.getName();
		//wfd.getParent();
		this.registers = new ArrayList<>();		
		for (Object item : wfd.getRegisters()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.registers.add(dto);
			}
		}
		this.splits = new ArrayList<>();		
		for (Object item : wfd.getSplits()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.splits.add(dto);
			}
		}
		this.steps = new ArrayList<>();		
		for (Object item : wfd.getSteps()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.steps.add(dto);
			}
		}
		this.triggerFunctions = wfd.getTriggerFunctions();
		this.uniqueKey = this.name;
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getMetaAttributes",
				"getTriggerFunctions");
	}

	@Override
	public Class<?> getJiraClass() {
		return WorkflowDescriptor.class;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
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

	public List<JiraConfigDTO> getGlobalActions() {
		return globalActions;
	}

	public void setGlobalActions(List<JiraConfigDTO> globalActions) {
		this.globalActions = globalActions;
	}

	public ConditionsDescriptorDTO getGlobalConditions() {
		return globalConditions;
	}

	public void setGlobalConditions(ConditionsDescriptorDTO globalConditions) {
		this.globalConditions = globalConditions;
	}

	public List<JiraConfigDTO> getInitialActions() {
		return initialActions;
	}

	public void setInitialActions(List<JiraConfigDTO> initialActions) {
		this.initialActions = initialActions;
	}

	public List<JiraConfigDTO> getJoins() {
		return joins;
	}

	public void setJoins(List<JiraConfigDTO> joins) {
		this.joins = joins;
	}

	public Map getMetaAttributes() {
		return metaAttributes;
	}

	public void setMetaAttributes(Map metaAttributes) {
		this.metaAttributes = metaAttributes;
	}

	public List<JiraConfigDTO> getRegisters() {
		return registers;
	}

	public void setRegisters(List<JiraConfigDTO> registers) {
		this.registers = registers;
	}

	public List<JiraConfigDTO> getSplits() {
		return splits;
	}

	public void setSplits(List<JiraConfigDTO> splits) {
		this.splits = splits;
	}

	public List<JiraConfigDTO> getSteps() {
		return steps;
	}

	public void setSteps(List<JiraConfigDTO> steps) {
		this.steps = steps;
	}

	public Map getTriggerFunctions() {
		return triggerFunctions;
	}

	public void setTriggerFunctions(Map triggerFunctions) {
		this.triggerFunctions = triggerFunctions;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		// TODO Auto-generated method stub
		return null;
	}

}
