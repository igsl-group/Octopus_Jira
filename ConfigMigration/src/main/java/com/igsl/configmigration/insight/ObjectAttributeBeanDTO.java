package com.igsl.configmigration.insight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
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
	public void fromJiraObject(Object obj) throws Exception {
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
		this.uniqueKey = Long.toString(this.objectId);
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Object Attribute Value Beans", 
				new JiraConfigProperty(ObjectAttributeValueBeanUtil.class, this.objectAttributeValueBeans));
		r.put("Object ID", new JiraConfigProperty(this.objectId));
		r.put("Object Type Attribute ID", new JiraConfigProperty(this.objectTypeAttributeId));
		return r;
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
		return ObjectAttributeBeanUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return ObjectAttributeBean.class;
	}

}
