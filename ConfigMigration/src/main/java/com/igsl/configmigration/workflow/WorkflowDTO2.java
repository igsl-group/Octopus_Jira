package com.igsl.configmigration.workflow;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.applicationuser.ApplicationUserDTO;
import com.igsl.configmigration.applicationuser.ApplicationUserUtil;

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
		this.uniqueKey = this.name;
	}
	
	@Override
	protected void setupRelatedObjects() throws Exception {
		// Do nothing
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
		r.put("XML", new JiraConfigProperty(this.xml));
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

}
