package com.igsl.configmigration.general;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

/**
 * Wrapper for Object.
 * Used to represent anything as a JiraConfigDTO for ease of deserialization.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class GeneralDTO extends JiraConfigDTO {

	private Object value;
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList("getValue");
	}

	@Override
	public Class<?> getJiraClass() {
		return Object.class;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public String getUniqueKey() {
		return Integer.toString(value.hashCode());
	}

	@Override
	public String getInternalId() {
		// TODO Auto-generated method stub
		return Integer.toString(value.hashCode());
	}

	@Override
	protected void fromJiraObject(Object obj, Object... params) throws Exception {
		// TODO Check if any DTO is available?
		this.value = obj;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
