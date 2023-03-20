package com.igsl.configmigration.insight;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.riadalabs.jira.plugins.insight.services.model.ObjectSchemaPropertyBean;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ObjectSchemaPropertyBeanDTO extends JiraConfigDTO {

	private Integer id;
	private Integer objectSchemaId;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		ObjectSchemaPropertyBean o = (ObjectSchemaPropertyBean) obj;
		this.id = o.getId();
		this.objectSchemaId = o.getObjectSchemaId();
		this.uniqueKey = Long.toString(this.id);
	}

	@Override
	public String getInternalId() {
		return Integer.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getId",
				"getObjectSchemaId");
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getObjectSchemaId() {
		return objectSchemaId;
	}

	public void setObjectSchemaId(Integer objectSchemaId) {
		this.objectSchemaId = objectSchemaId;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return ObjectSchemaPropertyBeanUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return ObjectSchemaPropertyBean.class;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		// TODO Auto-generated method stub
		return null;
	}

}
