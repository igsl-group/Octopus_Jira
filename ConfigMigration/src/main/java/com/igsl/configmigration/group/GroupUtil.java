package com.igsl.configmigration.group;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.groups.GroupManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.SessionData.ImportData;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class GroupUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(GroupUtil.class);
	private static GroupManager MANAGER = ComponentAccessor.getGroupManager();
	private static GroupPickerSearchService SERVICE = 
			ComponentAccessor.getComponent(GroupPickerSearchService.class);
	
	@Override
	public String getName() {
		return "User Group";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Group grp = MANAGER.getGroup(id);
		if (grp != null) {
			GroupDTO item = new GroupDTO();
			item.setJiraObject(grp);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (Group grp : SERVICE.findGroups("")) {
			if (grp.getName().equals(uniqueKey)) {
				GroupDTO item = new GroupDTO();
				item.setJiraObject(grp);
				return item;
			}
		}
		return null;
	}

	@Override
	public MergeResult merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		GroupDTO original;
		if (oldItem != null) {
			original = (GroupDTO) oldItem;
		} else {
			original = (GroupDTO) findByDTO(newItem);
		}
		GroupDTO src = (GroupDTO) newItem;
		if (original != null) {
			// Do nothing... group is just a name, and members are not stored in groups.
			result.setNewDTO(original);
		} else {
			// Create group
			Group createdJira = MANAGER.createGroup(src.getName());
			GroupDTO created = new GroupDTO();
			created.setJiraObject(createdJira);
			result.setNewDTO(created);
		}
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return GroupDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}
	
	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (Group grp : SERVICE.findGroups("")) {
			GroupDTO item = new GroupDTO();
			item.setJiraObject(grp);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);					
		}
		return result;
	}

}
