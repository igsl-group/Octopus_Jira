package com.igsl.configmigration.globalpermission;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.security.GlobalPermissionEntry;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.group.GroupDTO;
import com.igsl.configmigration.group.GroupUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class GlobalPermissionEntryDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(GlobalPermissionEntryDTO.class);
	
	private String permissionKey;
	private GroupDTO group;
	
	@Override
	public int getObjectParameterCount() {
		// #0: GlobalPermissionsDTO
		return 1;
	}
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		GlobalPermissionEntry o = (GlobalPermissionEntry) obj;
		this.permissionKey = o.getPermissionKey();
		if (o.getGroup() != null) {
			GroupUtil groupUtil = (GroupUtil) JiraConfigTypeRegistry.getConfigUtil(GroupUtil.class);
			this.group = (GroupDTO) groupUtil.findByUniqueKey(o.getGroup());
		}
		this.uniqueKey = this.permissionKey + "." + ((this.group == null)? "Anyone" : this.group.getName());
	}
	
	@Override
	public String getConfigName() {
		return ((this.group == null)? "(Anyone)" : this.group.getConfigName());
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Permission Key", new JiraConfigProperty(this.permissionKey));
		r.put("Group", new JiraConfigProperty(GroupUtil.class, this.group));
		return r;
	}
	
	@Override
	public void setupRelatedObjects() {
		GlobalPermissionsDTO parent = (GlobalPermissionsDTO) objectParameters[0];
		if (this.group != null) {
			parent.addRelatedObject(this.group);
			this.group.addReferencedObject(parent);
		}
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList("getValue");
	}

	@Override
	public Class<?> getJiraClass() {
		return GlobalPermissionEntry.class;
	}
	
	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return GlobalPermissionEntryUtil.class;
	}

	@Override
	public String getInternalId() {
		return Integer.toString(this.hashCode());
	}

	public String getPermissionKey() {
		return permissionKey;
	}

	public void setPermissionKey(String permissionKey) {
		this.permissionKey = permissionKey;
	}

	public GroupDTO getGroup() {
		return group;
	}

	public void setGroup(GroupDTO group) {
		this.group = group;
	}

}
