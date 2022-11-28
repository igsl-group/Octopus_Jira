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
	
	/**
	 * No params
	 */
	@Override
	public Map<String, JiraConfigDTO> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (Group grp : SERVICE.findGroups("")) {
			GroupDTO item = new GroupDTO();
			item.setJiraObject(grp);
			result.put(item.getUniqueKey(), item);					
		}
		return result;
	}

	/**
	 * params[0]: User name as String
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		String uniqueKey = String.valueOf(params[0]);
		return MANAGER.getGroup(uniqueKey);
	}
	
	@Override
	public Object merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		// TODO
		return null;
	}
	
	@Override
	public void merge(Map<String, ImportData> items) throws Exception {
		// TODO
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return GroupDTO.class;
	}

	@Override
	public boolean isPublic() {
		return true;
	}

}
