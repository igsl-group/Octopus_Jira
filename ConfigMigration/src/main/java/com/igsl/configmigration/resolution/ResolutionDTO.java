package com.igsl.configmigration.resolution;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.resolution.Resolution;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ResolutionDTO extends JiraConfigDTO {

	private String id;
	private String description;
	private String name;
	private Long sequence;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		Resolution obj = (Resolution) o;
		this.id = obj.getId();
		this.description = obj.getDescription();
		this.name = obj.getName();
		this.sequence = obj.getSequence();
	}

	@Override
	public String getUniqueKey() {
		return this.getName();
	}

	@Override
	public String getInternalId() {
		return this.getId();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getDescription",
				"getSequence");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getSequence() {
		return sequence;
	}

	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return ResolutionUtil.class;
	}

}
