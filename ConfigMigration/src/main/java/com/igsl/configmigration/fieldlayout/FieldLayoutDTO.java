package com.igsl.configmigration.fieldlayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldLayoutDTO extends JiraConfigDTO {

	private Long id;
	private String name;
	private String description;
	private List<FieldLayoutItemDTO> fieldLayoutItems = new ArrayList<>();
	
	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return FieldLayoutUtil.class;
	}
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		FieldLayout o = (FieldLayout) obj;
		this.id = o.getId();
		this.name = o.getName();
		this.description = o.getDescription();
		for (FieldLayoutItem item : o.getFieldLayoutItems()) {
			FieldLayoutItemDTO dto = new FieldLayoutItemDTO();
			dto.setJiraObject(item);
			fieldLayoutItems.add(dto);
		}
		this.uniqueKey = this.name;
	}
	
	@Override
	protected void setupRelatedObjects() throws Exception {
//		if (this.fieldLayoutItems != null) {
//			for (FieldLayoutItemDTO dto : this.fieldLayoutItems) {
//				addRelatedObject(dto);
//				dto.addReferencedObject(this);
//			}
//		}
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Layout Items", new JiraConfigProperty(FieldLayoutItemUtil.class, this.fieldLayoutItems));
		return r;
	}
	
	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName", 
				"getNameKey");
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldLayout.class;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<FieldLayoutItemDTO> getFieldLayoutItems() {
		return fieldLayoutItems;
	}

	public void setFieldLayoutItems(List<FieldLayoutItemDTO> fieldLayoutItems) {
		this.fieldLayoutItems = fieldLayoutItems;
	}
}
