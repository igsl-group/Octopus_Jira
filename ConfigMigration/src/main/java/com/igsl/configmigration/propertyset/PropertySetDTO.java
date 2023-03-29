package com.igsl.configmigration.propertyset;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.module.propertyset.PropertySet;

/**
 * PropertySet wrapper
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class PropertySetDTO extends JiraConfigDTO {

	private Map<String, Object> properties;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		PropertySet obj = (PropertySet) o;
		this.properties = new HashMap<>();
		for (Object item : obj.getKeys()) {
			String key = String.valueOf(item);
			this.properties.put(key, obj.getAsActualType(key));
		}
		this.uniqueKey = Integer.toString(this.hashCode());
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Properties", new JiraConfigProperty(this.properties));
		return r;
	}
	
	@Override
	public String getInternalId() {
		return this.uniqueKey;
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getProperties");
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return PropertySetUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return PropertySet.class;
	}

}
