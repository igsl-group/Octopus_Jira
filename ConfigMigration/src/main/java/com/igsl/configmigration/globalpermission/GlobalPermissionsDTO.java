package com.igsl.configmigration.globalpermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionEntry;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class GlobalPermissionsDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(GlobalPermissionsDTO.class);
	private static final GlobalPermissionManager MANAGER = ComponentAccessor.getGlobalPermissionManager();
	
	private String permissionKey;
	private List<GlobalPermissionEntryDTO> entries;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		GlobalPermissionKey o = (GlobalPermissionKey) obj;
		this.permissionKey = o.getKey();
		this.entries = new ArrayList<>();
		for (GlobalPermissionEntry entry : MANAGER.getPermissions(o)) {
			GlobalPermissionEntryDTO dto = new GlobalPermissionEntryDTO();
			dto.setJiraObject(entry, this);
			this.entries.add(dto);
		}
		this.uniqueKey = this.permissionKey;
	}
	
	@Override
	public String getConfigName() {
		// TODO Resolve i18n key?
		return this.permissionKey;
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Permission Key", new JiraConfigProperty(this.permissionKey));
		r.put("Entries", new JiraConfigProperty(GlobalPermissionEntryUtil.class, this.entries));
		return r;
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList("getValue");
	}

	@Override
	public Class<?> getJiraClass() {
		return GlobalPermissionKey.class;
	}
	
	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return GlobalPermissionsUtil.class;
	}

	@Override
	public String getInternalId() {
		return this.permissionKey;
	}

	public String getPermissionKey() {
		return permissionKey;
	}

	public void setPermissionKey(String permissionKey) {
		this.permissionKey = permissionKey;
	}

	public List<GlobalPermissionEntryDTO> getEntries() {
		return entries;
	}

	public void setEntries(List<GlobalPermissionEntryDTO> entries) {
		this.entries = entries;
	}

}
