package com.igsl.configmigration.insight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.riadalabs.jira.plugins.insight.services.model.ObjectAttributeBean;
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class InsightObjectBeanDTO extends JiraConfigItem {

	private Integer id;
	private String label;
	private String objectKey;
	private Integer objectTypeId;
	private List<InsightObjectAttributeBeanDTO> objectAttributeBeans;
	
	@Override
	public void fromJiraObject(Object obj, Object... params) throws Exception {
		ObjectBean o = (ObjectBean) obj;
		this.id = o.getId();
		this.label = o.getLabel();
		this.objectAttributeBeans = new ArrayList<>();
		for (ObjectAttributeBean e : o.getObjectAttributeBeans()) {
			InsightObjectAttributeBeanDTO item = new InsightObjectAttributeBeanDTO();
			item.setJiraObject(e);
			this.objectAttributeBeans.add(item);
		}
		this.objectKey = o.getObjectKey();
		this.objectTypeId = o.getObjectTypeId();
	}

	@Override
	public String getUniqueKey() {
		return this.getLabel();
	}

	@Override
	public String getInternalId() {
		return Integer.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getLabel",
				"getObjectTypeId",
				"getObjectKey",
				"getObjectAttributeBeans");
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getObjectKey() {
		return objectKey;
	}

	public void setObjectKey(String objectKey) {
		this.objectKey = objectKey;
	}

	public Integer getObjectTypeId() {
		return objectTypeId;
	}

	public void setObjectTypeId(Integer objectTypeId) {
		this.objectTypeId = objectTypeId;
	}

	public List<InsightObjectAttributeBeanDTO> getObjectAttributeBeans() {
		return objectAttributeBeans;
	}

	public void setObjectAttributeBeans(List<InsightObjectAttributeBeanDTO> objectAttributeBeans) {
		this.objectAttributeBeans = objectAttributeBeans;
	}

}
