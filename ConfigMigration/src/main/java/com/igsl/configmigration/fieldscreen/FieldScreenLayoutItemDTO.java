package com.igsl.configmigration.fieldscreen;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.customfield.CustomFieldUtil;
import com.igsl.configmigration.field.FieldDTO;
import com.igsl.configmigration.field.FieldUtil;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldScreenLayoutItemDTO extends JiraConfigDTO {
	
	private String fieldId;
	// Derived from fieldId, so we can relocate a field when migrating
	private FieldDTO field;
	
	private Long id;
	private int position;

	private String fieldName;
	private String tabName;
	
	/**
	 * #0: FieldScreenTabDTO
	 */
	@Override
	protected int getObjectParameterCount() {
		return 1;
	}
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		FieldScreenLayoutItem o = (FieldScreenLayoutItem) obj;
		this.fieldId = o.getFieldId();
		this.id = o.getId();
		this.position = o.getPosition();
		// TODO o.getOrderableField();	Is this important?
		
		// Resolve field ID into something searchable
		FieldUtil util = (FieldUtil) JiraConfigTypeRegistry.getConfigUtil(FieldUtil.class);
		this.field = (FieldDTO) util.findByInternalId(this.fieldId);
		if (this.field != null) {
			// Field ID can point to deleted fields... so only grab the name if field can be found.
			this.fieldName = this.field.getUniqueKey();
		}
		
		FieldScreenTabDTO tab = (FieldScreenTabDTO) this.objectParameters[0];
		this.tabName = tab.getName();
		this.uniqueKey = tab.getUniqueKey() + "." + this.fieldName + "." + this.id;
	}

	@Override
	protected void setupRelatedObjects() throws Exception {
		// Add custom fields as related item in FieldScreenDTO
		FieldScreenTabDTO tab = (FieldScreenTabDTO) this.objectParameters[0];
		FieldScreenDTO screen = (FieldScreenDTO) tab.getObjectParameters()[0];
		if (this.field != null) {
			CustomFieldUtil util = (CustomFieldUtil) JiraConfigTypeRegistry.getConfigUtil(CustomFieldUtil.class);
			JiraConfigDTO dto = util.findByUniqueKey(this.field.getName());
			if (dto != null) {
				screen.addRelatedObject(this.field);
			}
		}
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Field ID", new JiraConfigProperty(this.fieldId));
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Position", new JiraConfigProperty(this.position));
		r.put("Field", new JiraConfigProperty(FieldUtil.class, this.field));
		r.put("Field Name", new JiraConfigProperty(this.fieldName));
		r.put("Tab Name", new JiraConfigProperty(this.tabName));
		return r;
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getPosition",
				"getField",
				"getFieldName",
				"getTabName");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return FieldScreenLayoutItemUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldScreenLayoutItem.class;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public FieldDTO getField() {
		return field;
	}

	public void setField(FieldDTO field) {
		this.field = field;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getTabName() {
		return tabName;
	}

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}

}
