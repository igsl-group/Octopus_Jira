package com.igsl.configmigration.globalpermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.security.GlobalPermissionEntry;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.plugin.GlobalPermissionTypesManager;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.group.GroupDTO;

public class GlobalPermissionsUtil extends JiraConfigUtil {

	private static final GlobalPermissionManager MANAGER = ComponentAccessor.getGlobalPermissionManager();
	
	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String getName() {
		return "Global Permissions";
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return GlobalPermissionEntryDTO.class;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		for (JiraConfigDTO dto : search(null, params).values()) {
			if (dto.getInternalId().equals(id)) {
				return dto;
			}
		}
		return null;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new HashMap<>();
		Iterator<GlobalPermissionType> it = MANAGER.getAllGlobalPermissions().iterator();
		while (it.hasNext()) {
			GlobalPermissionKey key = it.next().getGlobalPermissionKey();
			GlobalPermissionsDTO dto = new GlobalPermissionsDTO();
			dto.setJiraObject(key);
			result.put(dto.getUniqueKey(), dto);
		}
		return result;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (JiraConfigDTO dto : search(null, params).values()) {
			if (dto.getUniqueKey().equals(uniqueKey)) {
				return dto;
			}
		}
		return null;
	}

	@Override
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		GlobalPermissionsDTO original;
		if (oldItem != null) {
			original = (GlobalPermissionsDTO) oldItem;
		} else {
			original = (GlobalPermissionsDTO) findByDTO(newItem);
		}
		GlobalPermissionsDTO src = (GlobalPermissionsDTO) newItem;
		if (original != null) {
			// Calculate delta in entries
			Set<String> expectedGroupList = new HashSet<>();
			for (GlobalPermissionEntryDTO entry : src.getEntries()) {
				if (entry.getGroup() != null) {
					expectedGroupList.add(entry.getGroup().getName());
				} else {
					expectedGroupList.add(null);
				}
			}
			Set<String> existingGroupList = new HashSet<>();
			for (GlobalPermissionEntryDTO entry : original.getEntries()) {
				if (entry.getGroup() != null) {
					existingGroupList.add(entry.getGroup().getName());
				} else {
					existingGroupList.add(null);
				}
			}
			Set<String> removeList = new HashSet<>();
			removeList.addAll(existingGroupList);
			removeList.removeAll(expectedGroupList);
			Set<String> addList = new HashSet<>();
			addList.addAll(expectedGroupList);
			addList.removeAll(existingGroupList);
			GlobalPermissionKey key = GlobalPermissionKey.of(src.getPermissionKey());
			if (key == null) {
				throw new Exception("Unrecognized global permission key: " + src.getPermissionKey());
			}
			GlobalPermissionType type = MANAGER.getGlobalPermission(key).getOrNull();
			if (type == null) {
				throw new Exception("Unrecognized global permission type: " + src.getPermissionKey());
			}
			for (String removeGroup : removeList) {
				if (!MANAGER.removePermission(type, removeGroup)) {
					result.addWarning("Unable to remove group " + removeGroup + " from " + src.getPermissionKey());
				}
			}
			for (String addGroup : addList) {
				if (!MANAGER.addPermission(type, addGroup)) {
					result.addWarning("Unable to add group " + addGroup + " to " + src.getPermissionKey());
				}
			}
		} else {
			throw new Exception("Global permission key not found: " + oldItem.getUniqueKey());
		}
		// Set result
		GlobalPermissionsDTO created = (GlobalPermissionsDTO) findByDTO(src);
		result.setNewDTO(created);
		return result;
	}

}
