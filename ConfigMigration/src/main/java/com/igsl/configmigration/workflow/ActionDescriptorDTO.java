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
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ActionDescriptorDTO extends AbstractDescriptorDTO {

	private static final Logger LOGGER = Logger.getLogger(ActionDescriptorDTO.class);
	
	private String view;
	private String name;
	private int id;
	private int entityId;
	private boolean autoExecute;
	private Map metaAttributes;
	private List<JiraConfigDTO> postFunctions;
	private List<JiraConfigDTO> preFunctions;
	private List<JiraConfigDTO> validators;
	private RestrictionDescriptorDTO restrictDescriptor;
	private List<JiraConfigDTO> conditionalResults;
	private ResultDescriptorDTO unconditionalResult;
	private ResultDescriptorDTO resultDescriptor;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		ActionDescriptor ad = (ActionDescriptor) obj; 
		this.autoExecute = ad.getAutoExecute();
		this.conditionalResults = new ArrayList<>();
		for (Object item : ad.getConditionalResults()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.conditionalResults.add(dto);
			}
		}
		this.unconditionalResult = new ResultDescriptorDTO();
		this.unconditionalResult.setJiraObject(ad.getUnconditionalResult());
		this.entityId = ad.getEntityId();
		this.id = ad.getId();
		this.metaAttributes = ad.getMetaAttributes();
		this.name = ad.getName();
		ad.getParent();
		this.postFunctions = new ArrayList<>();
		for (Object item : ad.getPostFunctions()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.postFunctions.add(dto);
			}
		}
		this.preFunctions = new ArrayList<>();
		for (Object item : ad.getPreFunctions()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.preFunctions.add(dto);
			}
		}
		this.restrictDescriptor = new RestrictionDescriptorDTO();
		this.restrictDescriptor.setJiraObject(ad.getRestriction());
		this.resultDescriptor = new ResultDescriptorDTO();
		this.resultDescriptor.setJiraObject(ad.getUnconditionalResult());
		this.validators = new ArrayList<>();
		for (Object item : ad.getValidators()) {
			JiraConfigDTO dto = WorkflowUtil.getWorkflowDetails(item);
			if (dto != null) {
				this.validators.add(dto);
			}
		}
		this.view = ad.getView();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getView",
				"getName",
				"isAutoExecute",
				"getMetaAttributes",
				"getRestrictDescriptor",
				"getResultDescriptor");
	}

	@Override
	public Class<?> getJiraClass() {
		return ActionDescriptor.class;
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

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public boolean isAutoExecute() {
		return autoExecute;
	}

	public void setAutoExecute(boolean autoExecute) {
		this.autoExecute = autoExecute;
	}

	public Map getMetaAttributes() {
		return metaAttributes;
	}

	public void setMetaAttributes(Map metaAttributes) {
		this.metaAttributes = metaAttributes;
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

	public List<JiraConfigDTO> getValidators() {
		return validators;
	}

	public void setValidators(List<JiraConfigDTO> validators) {
		this.validators = validators;
	}

	public RestrictionDescriptorDTO getRestrictDescriptor() {
		return restrictDescriptor;
	}

	public void setRestrictDescriptor(RestrictionDescriptorDTO restrictDescriptor) {
		this.restrictDescriptor = restrictDescriptor;
	}

	public List<JiraConfigDTO> getConditionalResults() {
		return conditionalResults;
	}

	public void setConditionalResults(List<JiraConfigDTO> conditionalResults) {
		this.conditionalResults = conditionalResults;
	}

	public ResultDescriptorDTO getResultDescriptor() {
		return resultDescriptor;
	}

	public void setResultDescriptor(ResultDescriptorDTO resultDescriptor) {
		this.resultDescriptor = resultDescriptor;
	}

	public ResultDescriptorDTO getUnconditionalResult() {
		return unconditionalResult;
	}

	public void setUnconditionalResult(ResultDescriptorDTO unconditionalResult) {
		this.unconditionalResult = unconditionalResult;
	}

}
