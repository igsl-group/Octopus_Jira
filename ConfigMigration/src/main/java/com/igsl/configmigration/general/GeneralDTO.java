package com.igsl.configmigration.general;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

/**
 * Wrapper for Object.
 * Used to represent anything (except collection, map, and JiraConfigDTO) 
 * as a JiraConfigDTO for ease of deserialization.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class GeneralDTO extends JiraConfigDTO {

	private Object value;
	private String valueClass;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		this.value = obj;
		if (this.value != null) {
			this.valueClass = this.value.getClass().getCanonicalName();
		}
		this.uniqueKey = Integer.toString(this.hashCode());
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Value", new JiraConfigProperty(this.value));
		r.put("Value Class", new JiraConfigProperty(this.valueClass));
		return r;
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList("getValue");
	}

	@Override
	public Class<?> getJiraClass() {
		// Do not associate with anything
		return null;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return GeneralUtil.class;
	}

	@Override
	public String getInternalId() {
		return this.uniqueKey;
	}

	public String getValueClass() {
		return valueClass;
	}

	public void setValueClass(String valueClass) {
		this.valueClass = valueClass;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
