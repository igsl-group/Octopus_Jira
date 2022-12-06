package com.igsl.configmigration.fieldscreen;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
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
	
	/**
	 * #0: FieldScreenTab
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
		// o.getOrderableField();	Is this important?
		
		// Resolve field ID into something searchable
		FieldUtil util = (FieldUtil) JiraConfigTypeRegistry.getConfigUtil(FieldUtil.class);
		this.field = (FieldDTO) util.findByInternalId(this.fieldId);
	}

	@Override
	public String getUniqueKey() {
		return Long.toString(this.getId());
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getPosition",
				"getField");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return FieldScreenUtil.class;	// TODO
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

}
