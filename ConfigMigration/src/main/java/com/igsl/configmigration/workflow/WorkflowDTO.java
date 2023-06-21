package com.igsl.configmigration.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.applicationuser.ApplicationUserDTO;
import com.igsl.configmigration.applicationuser.ApplicationUserUtil;
import com.igsl.configmigration.status.StatusDTO;
import com.igsl.configmigration.status.StatusUtil;
import com.opensymphony.workflow.loader.StepDescriptor;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class WorkflowDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(WorkflowDTO.class);
	
	private String name;
	private String description;
	private String mode;
	private String displayName;
	private String updateAuthorName;
	private ApplicationUserDTO updateAuthor;
	private Date updatedDate;
	private String xml;
	@JsonIgnore
	private List<StatusDTO> statuses = new ArrayList<>();
	private Map<String, String> layoutXml = new HashMap<>();
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		JiraWorkflow wf = (JiraWorkflow) obj;
		this.name = wf.getName();
		this.description = wf.getDescription();
		this.displayName = wf.getDisplayName();
		this.mode = wf.getMode();
		this.updateAuthor = new ApplicationUserDTO();
		this.updateAuthor.setJiraObject(wf.getUpdateAuthor());
		this.updateAuthorName = wf.getUpdateAuthorName();
		this.updatedDate = wf.getUpdatedDate();
		this.xml = com.atlassian.jira.workflow.WorkflowUtil.convertDescriptorToXML(wf.getDescriptor());
		this.uniqueKey = this.name;
		StatusUtil statusUtil = (StatusUtil) JiraConfigTypeRegistry.getConfigUtil(StatusUtil.class);
		for (Object step : wf.getDescriptor().getSteps()) {
			StepDescriptor sd = (StepDescriptor) step;
			String statusId = (String) sd.getMetaAttributes().get("jira.status.id");
			StatusDTO status = (StatusDTO) statusUtil.findByInternalId(statusId);
			if (status != null) {
				this.statuses.add(status);
			}
		}
		WorkflowUtil workflowUtil = (WorkflowUtil) JiraConfigTypeRegistry.getConfigUtil(WorkflowUtil.class);
		this.layoutXml = workflowUtil.getLayoutXML(this);
	}
	
	@Override
	protected void setupRelatedObjects() throws Exception {
		// Link to status
		for (StatusDTO status : this.statuses) {
			this.addRelatedObject(status);
			status.addReferencedObject(this);
		}
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Display Name", new JiraConfigProperty(this.displayName));
		r.put("Mode", new JiraConfigProperty(this.mode));
		r.put("Update Author", new JiraConfigProperty(ApplicationUserUtil.class, this.updateAuthor));
		r.put("Update Author Name", new JiraConfigProperty(this.updateAuthorName));
		r.put("Updated Date", new JiraConfigProperty(this.updatedDate));
		r.put("Status", new JiraConfigProperty(StatusUtil.class, this.statuses));
		r.put("XML", new JiraConfigProperty(this.xml));
		r.put("Layout", new JiraConfigProperty(this.layoutXml));
		WorkflowUtil util = (WorkflowUtil) JiraConfigTypeRegistry.getConfigUtil(this.getUtilClass());
		if (util != null) {
			MergeResult mr = new MergeResult();
			String mappedXML = util.remapWorkflowMXML(this.name, this.xml, mr);
			r.put("Mapped XML", new JiraConfigProperty(mappedXML));
		}		
		return r;
	}
	
	@Override
	public String getInternalId() {
		return this.getName();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getXml",
				"getName");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return com.igsl.configmigration.workflow.WorkflowUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return JiraWorkflow.class;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getUpdateAuthorName() {
		return updateAuthorName;
	}

	public void setUpdateAuthorName(String updateAuthorName) {
		this.updateAuthorName = updateAuthorName;
	}

	public ApplicationUserDTO getUpdateAuthor() {
		return updateAuthor;
	}

	public void setUpdateAuthor(ApplicationUserDTO updateAuthor) {
		this.updateAuthor = updateAuthor;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public List<StatusDTO> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<StatusDTO> statuses) {
		this.statuses = statuses;
	}

	public Map<String, String> getLayoutXml() {
		return layoutXml;
	}

	public void setLayoutXml(Map<String, String> layoutXml) {
		this.layoutXml = layoutXml;
	}

}
