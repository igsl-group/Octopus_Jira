package com.igsl.configmigration.fieldlayoutscheme;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldlayout.FieldLayoutDTO;
import com.igsl.configmigration.fieldlayout.FieldLayoutUtil;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.issuetype.IssueTypeUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldLayoutSchemeEntityDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(FieldLayoutSchemeEntityDTO.class);
	
	private Long id;
	private FieldLayoutDTO fieldLayout;
	private IssueTypeDTO issueType;
	
	@JsonIgnore
	protected int getObjectParameterCount() {
		// 0: FieldLayoutScheme
		return 1;
	}
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		FieldLayoutUtil flUtil = (FieldLayoutUtil) JiraConfigTypeRegistry.getConfigUtil(FieldLayoutUtil.class);
		IssueTypeUtil itUtil = (IssueTypeUtil) JiraConfigTypeRegistry.getConfigUtil(IssueTypeUtil.class);
		FieldLayoutSchemeEntity obj = (FieldLayoutSchemeEntity) o;
		if (obj.getFieldLayoutId() != null) {
			this.fieldLayout = (FieldLayoutDTO) flUtil.findByInternalId(Long.toString(obj.getFieldLayoutId()));
		}
		this.id = obj.getId();
		if (obj.getIssueTypeObject() != null) {
			this.issueType = (IssueTypeDTO) itUtil.findByUniqueKey(obj.getIssueTypeObject().getName());
		}
		this.uniqueKey = Long.toString(this.id);
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Field Layout", new JiraConfigProperty(FieldLayoutUtil.class, this.fieldLayout));
		r.put("Issue Type", new JiraConfigProperty(IssueTypeUtil.class, this.issueType));
		return r;
	}

	protected void setupRelatedObjects() throws Exception {
		// Add issue type and field layout to parent
		FieldLayoutSchemeDTO scheme = (FieldLayoutSchemeDTO) getObjectParameters()[0];
		if (scheme != null) {
			if (this.fieldLayout != null) {
				scheme.addRelatedObject(this.fieldLayout);
				this.fieldLayout.addRelatedObject(scheme);
			}
			if (this.issueType != null) {
				scheme.addReferencedObject(this.issueType);
				this.issueType.addRelatedObject(scheme);
			}
		}
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
				"getAssociatedIssueTypes",
				"getAssociatedProjects",
				"getFieldConfig");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return FieldLayoutSchemeEntityUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldLayoutScheme.class;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public FieldLayoutDTO getFieldLayout() {
		return fieldLayout;
	}

	public void setFieldLayout(FieldLayoutDTO fieldLayout) {
		this.fieldLayout = fieldLayout;
	}

	public IssueTypeDTO getIssueType() {
		return issueType;
	}

	public void setIssueType(IssueTypeDTO issueType) {
		this.issueType = issueType;
	}

}
