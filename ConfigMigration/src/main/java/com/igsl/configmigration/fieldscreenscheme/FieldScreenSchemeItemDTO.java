package com.igsl.configmigration.fieldscreenscheme;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldscreen.FieldScreenDTO;
import com.igsl.configmigration.fieldscreen.FieldScreenUtil;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldScreenSchemeItemDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(FieldScreenSchemeItemDTO.class);
	
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
		this.issueOperationName = o.getIssueOperationName();	// TODO This is i18n key, how to translate?
		this.uniqueKey = Long.toString(this.id);
	}

	@Override
	public String getConfigName() {
		return this.issueOperation.getConfigName();
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Field Screen", new JiraConfigProperty(FieldScreenUtil.class, this.fieldScreen));
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Issue Operation", new JiraConfigProperty(ScreenableIssueOperationUtil.class, this.issueOperation));
		return r;
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
		return FieldScreenSchemeItemUtil.class;
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
