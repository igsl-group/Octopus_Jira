package com.igsl.configmigration.insight;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.riadalabs.jira.plugins.insight.services.model.ObjectSchemaBean;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ObjectSchemaBeanDTO extends JiraConfigDTO {

	private String description;
	private Integer id;
	private String name;
	private String objectSchemaKey;
	private ObjectSchemaPropertyBeanDTO objectSchemaPropertyBean;
	
	@Override
	public void fromJiraObject(Object obj, Object... params) throws Exception {
		ObjectSchemaBean o = (ObjectSchemaBean) obj;
		this.description = o.getDescription();
		this.id = o.getId();
		this.name = o.getName();
		this.objectSchemaKey = o.getObjectSchemaKey();
		this.objectSchemaPropertyBean = new ObjectSchemaPropertyBeanDTO();
		this.objectSchemaPropertyBean.setJiraObject(o.getObjectSchemaPropertyBean());
	}

	@Override
	public String getUniqueKey() {
		return this.getObjectSchemaKey();
	}

	@Override
	public String getInternalId() {
		return Integer.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getObjectSchemaKey",
				"getDescription",
				"getObjectSchemaPropertyBean");
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getObjectSchemaKey() {
		return objectSchemaKey;
	}

	public void setObjectSchemaKey(String objectSchemaKey) {
		this.objectSchemaKey = objectSchemaKey;
	}

	public ObjectSchemaPropertyBeanDTO getObjectSchemaPropertyBean() {
		return objectSchemaPropertyBean;
	}

	public void setObjectSchemaPropertyBean(ObjectSchemaPropertyBeanDTO objectSchemaPropertyBean) {
		this.objectSchemaPropertyBean = objectSchemaPropertyBean;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return ObjectSchemaBeanUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return ObjectSchemaBean.class;
	}

}
