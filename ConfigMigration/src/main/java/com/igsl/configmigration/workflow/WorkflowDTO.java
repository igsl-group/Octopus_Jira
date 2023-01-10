package com.igsl.configmigration.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.applicationuser.ApplicationUserDTO;
import com.opensymphony.workflow.loader.ActionDescriptor;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class WorkflowDTO extends JiraConfigDTO {

	private String description;
	private String displayName;
	private String mode;
	private String name;
	private ApplicationUserDTO author;
	private String authorName;
	private Date updatedDate;
	private Set<String> statusIds;
	private WorkflowDescriptorDTO descriptor;
	private Collection<ActionDescriptorDTO> actions;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		JiraWorkflow wf = (JiraWorkflow) obj;
		actions = new ArrayList<>();
		for (ActionDescriptor ad : wf.getAllActions()) {
			ActionDescriptorDTO dto = new ActionDescriptorDTO();
			dto.setJiraObject(ad);
			actions.add(dto);
		}
		this.description = wf.getDescription();
		this.descriptor = new WorkflowDescriptorDTO();
		this.descriptor.fromJiraObject(wf.getDescriptor());
		this.displayName = wf.getDisplayName();
		this.statusIds = wf.getLinkedStatusIds();
		this.mode = wf.getMode();
		this.name = wf.getName();
		this.author = new ApplicationUserDTO();
		this.author.setJiraObject(wf.getUpdateAuthor());
		this.authorName = wf.getUpdateAuthorName();
		this.updatedDate = wf.getUpdatedDate();
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
				"getDescription",
				"getActions",
				"getDescriptor",
				"getStatusIds",
				"getMode",
				"getName",
				"getAuthor",
				"getAuthorName",
				"getUpdatedDate");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public Class<?> getJiraClass() {
		//return JiraWorkflow.class;
		return null;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ApplicationUserDTO getAuthor() {
		return author;
	}

	public void setAuthor(ApplicationUserDTO author) {
		this.author = author;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Set<String> getStatusIds() {
		return statusIds;
	}

	public void setStatusIds(Set<String> statusIds) {
		this.statusIds = statusIds;
	}

	public WorkflowDescriptorDTO getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(WorkflowDescriptorDTO descriptor) {
		this.descriptor = descriptor;
	}

	public Collection<ActionDescriptorDTO> getActions() {
		return actions;
	}

	public void setActions(Collection<ActionDescriptorDTO> actions) {
		this.actions = actions;
	}

}
