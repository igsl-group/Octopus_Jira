package com.igsl.configmigration.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.ResultDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ResultDescriptorDTO extends AbstractDescriptorDTO {
	
	private String displayName;
	private String dueDate;
	private int entityId;
	private int id;
	private int join;
	private String oldStatus;
	private String owner;
	private List<JiraConfigDTO> postFunctions;
	private List<JiraConfigDTO> preFunctions;
	private int split;
	private String status;
	private int step;
	private List<JiraConfigDTO> validators;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		ResultDescriptor rd = (ResultDescriptor) obj;
		this.displayName = rd.getDisplayName();
		this.dueDate = rd.getDueDate();
		this.entityId = rd.getEntityId();
		this.id = rd.getId();
		this.join = rd.getJoin();
		this.oldStatus = rd.getOldStatus();
		this.owner = rd.getOwner();
		rd.getParent();
		this.postFunctions = new ArrayList<>();
		for (Object o : rd.getPostFunctions()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(o);
			if (dto != null) {
				this.postFunctions.add(dto);
			}
		}
		this.preFunctions = new ArrayList<>();
		for (Object o : rd.getPreFunctions()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(o);
			if (dto != null) {
				this.preFunctions.add(dto);
			}
		}
		this.split = rd.getSplit();
		this.status = rd.getStatus();
		this.step = rd.getStep();
		this.validators = new ArrayList<>();
		for (Object o : rd.getValidators()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(o);
			if (dto != null) {
				this.validators.add(dto);
			}
		}
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getDisplayName",
				"getDueDate");
	}

	@Override
	public Class<?> getJiraClass() {
		return ResultDescriptor.class;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public String getUniqueKey() {
		return this.getDisplayName();
	}

	@Override
	public String getInternalId() {
		return Integer.toString(this.getId());
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
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

	public int getJoin() {
		return join;
	}

	public void setJoin(int join) {
		this.join = join;
	}

	public String getOldStatus() {
		return oldStatus;
	}

	public void setOldStatus(String oldStatus) {
		this.oldStatus = oldStatus;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public List getPostFunctions() {
		return postFunctions;
	}

	public void setPostFunctions(List postFunctions) {
		this.postFunctions = postFunctions;
	}

	public List getPreFunctions() {
		return preFunctions;
	}

	public void setPreFunctions(List preFunctions) {
		this.preFunctions = preFunctions;
	}

	public int getSplit() {
		return split;
	}

	public void setSplit(int split) {
		this.split = split;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public List<JiraConfigDTO> getValidators() {
		return validators;
	}

	public void setValidators(List<JiraConfigDTO> validators) {
		this.validators = validators;
	}

}
