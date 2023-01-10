package com.igsl.configmigration.workflow;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.applicationuser.ApplicationUserDTO;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class WorkflowDTO2 extends JiraConfigDTO {

	private String name;
	private String description;
	private String mode;
	private String displayName;
	private String updateAuthorName;
	private ApplicationUserDTO updateAuthor;
	private Date updatedDate;
	private String xml;
	
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
	}

	@Override
	public String getUniqueKey() {
		return this.getName();
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

}
