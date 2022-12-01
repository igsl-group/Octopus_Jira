package com.igsl.configmigration.insight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.riadalabs.jira.plugins.insight.services.model.ObjectAttributeBean;
import com.riadalabs.jira.plugins.insight.services.model.ObjectAttributeValueBean;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ObjectAttributeBeanDTO extends JiraConfigDTO {

	private Long id;
	private Integer objectId;
	private Integer objectTypeAttributeId;
	private List<ObjectAttributeValueBeanDTO> objectAttributeValueBeans;
	
	@Override
	public void fromJiraObject(Object obj, Object... params) throws Exception {
		ObjectAttributeBean o = (ObjectAttributeBean) obj;
		this.id = o.getId();
		this.objectAttributeValueBeans = new ArrayList<>();
		for (ObjectAttributeValueBean e : o.getObjectAttributeValueBeans()) {
			ObjectAttributeValueBeanDTO item = new ObjectAttributeValueBeanDTO();
			item.setJiraObject(e);
			this.objectAttributeValueBeans.add(item);
		}
		this.objectId = o.getObjectId();
		this.objectTypeAttributeId = o.getObjectTypeAttributeId();
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
				);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getObjectId() {
		return objectId;
	}

	public void setObjectId(Integer objectId) {
		this.objectId = objectId;
	}

	public Integer getObjectTypeAttributeId() {
		return objectTypeAttributeId;
	}

	public void setObjectTypeAttributeId(Integer objectTypeAttributeId) {
		this.objectTypeAttributeId = objectTypeAttributeId;
	}

	public List<ObjectAttributeValueBeanDTO> getObjectAttributeValueBeans() {
		return objectAttributeValueBeans;
	}

	public void setObjectAttributeValueBeans(List<ObjectAttributeValueBeanDTO> objectAttributeValueBeans) {
		this.objectAttributeValueBeans = objectAttributeValueBeans;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getJiraClass() {
		return ObjectAttributeBean.class;
	}

}
