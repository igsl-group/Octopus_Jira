package com.igsl.configmigration.issuetypescreencheme;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldscreenscheme.FieldScreenSchemeDTO;
import com.igsl.configmigration.fieldscreenscheme.FieldScreenSchemeUtil;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.issuetype.IssueTypeUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeScreenSchemeEntityDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(IssueTypeScreenSchemeEntityDTO.class);
	
	private IssueTypeDTO issueType;
	private FieldScreenSchemeDTO fieldScreenScheme;
	private Long id;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		IssueTypeUtil issueTypeUtil = (IssueTypeUtil) JiraConfigTypeRegistry.getConfigUtil(IssueTypeUtil.class);
		FieldScreenSchemeUtil fieldScreenSchemeUtil = (FieldScreenSchemeUtil) JiraConfigTypeRegistry.getConfigUtil(FieldScreenSchemeUtil.class);
		IssueTypeScreenSchemeEntity obj = (IssueTypeScreenSchemeEntity) o;
		this.id = obj.getId();
		String issueTypeId = obj.getIssueTypeId();
		if (issueTypeId != null) {
			this.issueType = (IssueTypeDTO) issueTypeUtil.findByInternalId(issueTypeId);
		}
		Long fieldScreenSchemeId = obj.getFieldScreenSchemeId();
		if (fieldScreenSchemeId != null) {
			this.fieldScreenScheme = (FieldScreenSchemeDTO) fieldScreenSchemeUtil.findByInternalId(Long.toString(fieldScreenSchemeId));
		}
		StringBuilder s = new StringBuilder();
		if (this.getIssueType() != null) {
			s.append(this.getIssueType().getName());
		}
		s.append("-");
		if (this.getFieldScreenScheme() != null) {
			s.append(this.getFieldScreenScheme().getName());
		}
		this.uniqueKey = s.toString();
	}
	
	@Override
	protected void setupRelatedObjects() throws Exception {
		if (this.issueType != null) {
			this.addRelatedObject(this.issueType);
			this.issueType.addReferencedObject(this);
		}
		if (this.issueType != null) {
			this.addRelatedObject(this.fieldScreenScheme);
			this.fieldScreenScheme.addReferencedObject(this);
		}
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Issue Type", new JiraConfigProperty(IssueTypeUtil.class, this.issueType));
		r.put("Field Screen Scheme", new JiraConfigProperty(FieldScreenSchemeUtil.class, this.fieldScreenScheme));
		return r;
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getIssueType",
				"getFieldScreenScheme");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return IssueTypeScreenSchemeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return IssueTypeScreenScheme.class;
	}

	public IssueTypeDTO getIssueType() {
		return issueType;
	}

	public void setIssueType(IssueTypeDTO issueType) {
		this.issueType = issueType;
	}

	public FieldScreenSchemeDTO getFieldScreenScheme() {
		return fieldScreenScheme;
	}

	public void setFieldScreenScheme(FieldScreenSchemeDTO fieldScreenScheme) {
		this.fieldScreenScheme = fieldScreenScheme;
	}

	/**
	 * #0: IssueTypeScreenScheme
	 */
	@Override
	protected int getObjectParameterCount() {
		return 1;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
