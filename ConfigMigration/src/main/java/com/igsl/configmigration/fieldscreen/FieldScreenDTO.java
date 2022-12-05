package com.igsl.configmigration.fieldscreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldScreenDTO extends JiraConfigDTO {
	
	private String description;
	private Long id;
	private String name;
	private List<FieldScreenTabDTO> tabs;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		FieldScreen o = (FieldScreen) obj;
		this.description = o.getDescription();
		this.id = o.getId();
		this.name = o.getName();
		this.tabs = new ArrayList<>();
		for (FieldScreenTab item : o.getTabs()) {
			FieldScreenTabDTO dto = new FieldScreenTabDTO();
			dto.setJiraObject(item);
			tabs.add(dto);
		}
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
				"getDescription",
				"getStatusCategoryConfigItem");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return FieldScreenUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldScreen.class;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public List<FieldScreenTabDTO> getTabs() {
		return tabs;
	}

	public void setTabs(List<FieldScreenTabDTO> tabs) {
		this.tabs = tabs;
	}

}
