package com.igsl.configmigration.insight;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.riadalabs.jira.plugins.insight.services.model.ObjectAttributeBean;
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean;
import com.riadalabs.jira.plugins.insight.services.model.ObjectSchemaBean;
import com.riadalabs.jira.plugins.insight.services.model.ObjectTypeBean;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ObjectBeanDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(ObjectBeanDTO.class); 
	
	private Integer id;
	private String label;
	private String objectKey;	
	private Integer objectTypeId;
	private String objectTypeName;	// Calculated from objectTypeId
	private String schemaKey;	// Calculated from objectTypeId
	private List<ObjectAttributeBeanDTO> objectAttributeBeans;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		ObjectBean o = (ObjectBean) obj;
		this.id = o.getId();
		this.label = o.getLabel();
		this.objectAttributeBeans = new ArrayList<>();
		for (ObjectAttributeBean e : o.getObjectAttributeBeans()) {
			ObjectAttributeBeanDTO item = new ObjectAttributeBeanDTO();
			item.setJiraObject(e);
			this.objectAttributeBeans.add(item);
		}
		this.objectKey = o.getObjectKey();
		this.objectTypeId = o.getObjectTypeId();
		// Locate schema and object type name
		for (Object schema : ObjectBeanUtil.findObjectSchemaBeans()) {
			int schemaId = ObjectBeanUtil.getObjectSchemaId(schema);
			List<Object> typeList = ObjectBeanUtil.findObjectTypeBeansFlat(schemaId);
			for (Object type : typeList) {
				Integer typeId = ObjectBeanUtil.getObjectTypeId(type);
				String typeName = ObjectBeanUtil.getObjectTypeName(type);
				if (this.objectTypeId.equals(typeId)) {
					this.schemaKey = ObjectBeanUtil.getObjectSchemaKey(schema);
					this.objectTypeName = typeName;
					break;
				}
			}
		}
	}
	
	@Override
	public String getUniqueKey() {
		return this.schemaKey + "." + this.objectTypeName + "." + this.label;
	}

	@Override
	public String getInternalId() {
		return getObjectKey();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getLabel",
				"getObjectAttributeBeans",
				"getObjectTypeName",
				"getSchemaKey");
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

	public List<ObjectAttributeBeanDTO> getObjectAttributeBeans() {
		return objectAttributeBeans;
	}

	public void setObjectAttributeBeans(List<ObjectAttributeBeanDTO> objectAttributeBeans) {
		this.objectAttributeBeans = objectAttributeBeans;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return ObjectBeanUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return ObjectBean.class;
	}

	public String getObjectTypeName() {
		return objectTypeName;
	}

	public void setObjectTypeName(String objectTypeName) {
		this.objectTypeName = objectTypeName;
	}

	public String getSchemaKey() {
		return schemaKey;
	}

	public void setSchemaKey(String schemaKey) {
		this.schemaKey = schemaKey;
	}

}
