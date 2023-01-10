package com.igsl.configmigration.projectcategory;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.project.ProjectCategory;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

/**
 * Project wrapper
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectCategoryDTO extends JiraConfigDTO {

	private Long id;
	private String name;
	private String description;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		ProjectCategory o = (ProjectCategory) obj;
		this.description = o.getDescription();
		this.id = o.getId();
		this.name = o.getName();
	}

	@Override
	public String getUniqueKey() {
		return this.getName();
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getDescription");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return ProjectCategoryUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return ProjectCategory.class;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

}
