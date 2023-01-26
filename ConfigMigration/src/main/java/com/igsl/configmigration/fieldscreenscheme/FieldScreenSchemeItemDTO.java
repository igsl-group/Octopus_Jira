package com.igsl.configmigration.fieldscreenscheme;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldscreen.FieldScreenDTO;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldScreenSchemeItemDTO extends JiraConfigDTO {

	private FieldScreenDTO fieldScreen;
	private Long id;
	private ScreenableIssueOperationDTO issueOperation;
	private String issueOperationName;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		FieldScreenSchemeItem o = (FieldScreenSchemeItem) obj;
		this.fieldScreen = new FieldScreenDTO();
		this.fieldScreen.setJiraObject(o.getFieldScreen());
		this.id = o.getId();
		this.issueOperation = new ScreenableIssueOperationDTO();
		this.issueOperation.setJiraObject(o.getIssueOperation());
		this.issueOperationName = o.getIssueOperationName();
	}

	@Override
	public String getUniqueKey() {
		return getIssueOperationName();
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getFieldScreen",
				"getIssueOperation",
				"getIssueOperationName");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldScreenSchemeItem.class;
	}

	public FieldScreenDTO getFieldScreen() {
		return fieldScreen;
	}

	public void setFieldScreen(FieldScreenDTO fieldScreen) {
		this.fieldScreen = fieldScreen;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ScreenableIssueOperationDTO getIssueOperation() {
		return issueOperation;
	}

	public void setIssueOperation(ScreenableIssueOperationDTO issueOperation) {
		this.issueOperation = issueOperation;
	}

	public String getIssueOperationName() {
		return issueOperationName;
	}

	public void setIssueOperationName(String issueOperationName) {
		this.issueOperationName = issueOperationName;
	}

}
