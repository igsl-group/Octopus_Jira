package com.igsl.configmigration.fieldlayout;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.customfield.CustomFieldDTO;
import com.igsl.configmigration.customfield.CustomFieldUtil;
import com.igsl.configmigration.field.FieldDTO;
import com.igsl.configmigration.field.FieldUtil;
import com.igsl.configmigration.projectcomponent.ProjectComponentDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldLayoutItemDTO extends JiraConfigDTO {

	private String fieldDescription;
	private String rendererType;
	private FieldDTO systemField;
	private CustomFieldDTO customField;
	private boolean hidden;
	private boolean required;
	
	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return FieldLayoutItemUtil.class;
	}
	
	@Override
	public int getObjectParameterCount() {
		// #0: FieldLayoutDTO
		return 1;
	}
	
	@Override
	protected void setupRelatedObjects() throws Exception {
		FieldLayoutDTO parent = (FieldLayoutDTO) this.objectParameters[0];
		if (this.customField != null && parent != null) {
			CustomFieldUtil util = (CustomFieldUtil) JiraConfigTypeRegistry.getConfigUtil(CustomFieldUtil.class);
			CustomFieldDTO cf = (CustomFieldDTO) util.findByInternalId(this.customField.getInternalId());
			if (cf != null) {
				parent.addRelatedObject(cf);
				cf.addReferencedObject(parent);
			}
		}
	}
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		FieldUtil fieldUtil = (FieldUtil) JiraConfigTypeRegistry.getConfigUtil(FieldUtil.class);
		CustomFieldUtil cfUtil = (CustomFieldUtil) JiraConfigTypeRegistry.getConfigUtil(CustomFieldUtil.class);
		FieldLayoutItem o = (FieldLayoutItem) obj;
		this.fieldDescription = o.getRawFieldDescription();		
		this.hidden = o.isHidden();
		this.required = o.isRequired();
		OrderableField<?> of = o.getOrderableField();
		// Check if it is custom field
		CustomFieldDTO cf = (CustomFieldDTO) cfUtil.findByInternalId(of.getId());
		if (cf != null) {
			this.customField = cf;
			this.uniqueKey = o.getFieldLayout().getName() + "." + this.customField.getUniqueKey();
		} else {
			this.systemField = (FieldDTO) fieldUtil.findByInternalId(o.getOrderableField().getId());
			this.uniqueKey = o.getFieldLayout().getName() + "." + this.systemField.getUniqueKey();
		}
		this.rendererType = o.getRendererType();
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Field Description", new JiraConfigProperty(this.fieldDescription));
		r.put("Renderer Type", new JiraConfigProperty(this.rendererType));
		r.put("Hidden", new JiraConfigProperty(this.hidden));
		r.put("Required", new JiraConfigProperty(this.required));
		r.put("System Field", new JiraConfigProperty(FieldUtil.class, this.systemField));
		r.put("Custom Field", new JiraConfigProperty(CustomFieldUtil.class, this.customField));
		return r;
	}
	
	@Override
	public String getInternalId() {
		return Long.toString(this.hashCode());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName", 
				"getNameKey");
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldLayoutItem.class;
	}

	public String getFieldDescription() {
		return fieldDescription;
	}

	public void setFieldDescription(String fieldDescription) {
		this.fieldDescription = fieldDescription;
	}

	public String getRendererType() {
		return rendererType;
	}

	public void setRendererType(String rendererType) {
		this.rendererType = rendererType;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public FieldDTO getSystemField() {
		return systemField;
	}

	public void setSystemField(FieldDTO systemField) {
		this.systemField = systemField;
	}

	public CustomFieldDTO getCustomField() {
		return customField;
	}

	public void setCustomField(CustomFieldDTO customField) {
		this.customField = customField;
	}

}
