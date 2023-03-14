package com.igsl.configmigration.licensedapplication;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.application.api.ApplicationKey;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class LicensedApplicationDTO extends JiraConfigDTO {

	private String value;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		ApplicationKey o = (ApplicationKey) obj;
		this.value = o.value();
		this.uniqueKey = this.value;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Value", new JiraConfigProperty(this.value));
		return r;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return LicensedApplicationUtil.class;
	}

	@Override
	public String getInternalId() {
		return this.getValue();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getValue");
	}

	@Override
	public Class<?> getJiraClass() {
		return ApplicationKey.class;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
