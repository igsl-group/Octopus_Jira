package com.igsl.configmigration.permissionscheme;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectPermissionKeyDTO extends JiraConfigDTO {

	private String permissionKey;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		ProjectPermissionKey obj = (ProjectPermissionKey) o;
		this.permissionKey = obj.permissionKey();
		this.uniqueKey = this.permissionKey;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Permission Key", new JiraConfigProperty(this.permissionKey));
		return r;
	}
	
	@Override
	public String getInternalId() {
		return Long.toString(this.hashCode());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getKey",
				"getPluginVersion",
				"getPluginInformation",
				"getPluginState");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return ProjectPermissionKeyUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return ProjectPermissionKey.class;
	}

	public String getPermissionKey() {
		return permissionKey;
	}

	public void setPermissionKey(String permissionKey) {
		this.permissionKey = permissionKey;
	}

}
