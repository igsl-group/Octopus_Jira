package com.igsl.configmigration.propertyset;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.opensymphony.module.propertyset.PropertySet;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class PropertySetDTO extends JiraConfigItem {

	private Map<String, Object> properties;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		PropertySet obj = (PropertySet) o;
		this.properties = new HashMap<>();
		for (Object item : obj.getKeys()) {
			String key = String.valueOf(item);
			this.properties.put(key, obj.getAsActualType(key));
		}
	}

	@Override
	public String getUniqueKey() {
		return Integer.toString(this.hashCode());
	}

	@Override
	public String getInternalId() {
		return Integer.toString(this.hashCode());
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

}
