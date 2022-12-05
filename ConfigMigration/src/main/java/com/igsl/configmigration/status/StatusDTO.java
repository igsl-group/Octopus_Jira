package com.igsl.configmigration.status;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.status.Status;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class StatusDTO extends JiraConfigDTO {

	protected String id;
	protected String name;
	protected String description;
	protected StatusCategoryDTO statusCategoryConfigItem;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		Status o = (Status) obj;
		id = o.getId();
		name = o.getName();
		description = o.getDescription();
		statusCategoryConfigItem = new StatusCategoryDTO();
		statusCategoryConfigItem.setJiraObject(o.getStatusCategory());
	}

	@Override
	public String getUniqueKey() {
		return this.getName();
	}

	@Override
	public String getInternalId() {
		return this.getId();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public StatusCategoryDTO getStatusCategoryConfigItem() {
		return statusCategoryConfigItem;
	}

	public void setStatusCategoryConfigItem(StatusCategoryDTO statusCategoryConfigItem) {
		this.statusCategoryConfigItem = statusCategoryConfigItem;
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getDescription",
				"getStatusCategoryConfigItem");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return StatusUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return Status.class;
	}

}
