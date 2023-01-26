package com.igsl.configmigration.fieldscreenscheme;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class ScreenableIssueOperationDTO extends JiraConfigDTO {
	
	private String descriptionKey;
	private Long id;
	private String nameKey;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		ScreenableIssueOperation o = (ScreenableIssueOperation) obj;
		this.descriptionKey = o.getDescriptionKey();
		this.id = o.getId();
		this.nameKey = o.getNameKey();
	}

	@Override
	public String getUniqueKey() {
		return this.getNameKey();
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getNameKey",
				"getDescriptionKey");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public Class<?> getJiraClass() {
		return ScreenableIssueOperation.class;
	}

	public String getDescriptionKey() {
		return descriptionKey;
	}

	public void setDescriptionKey(String descriptionKey) {
		this.descriptionKey = descriptionKey;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNameKey() {
		return nameKey;
	}

	public void setNameKey(String nameKey) {
		this.nameKey = nameKey;
	}

}
