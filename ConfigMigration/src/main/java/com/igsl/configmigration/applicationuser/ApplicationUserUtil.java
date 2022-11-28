package com.igsl.configmigration.applicationuser;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.icon.IconOwningObjectId;
import com.atlassian.jira.icon.IconType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ApplicationUserUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ApplicationUserUtil.class);
	private static UserManager MANAGER = ComponentAccessor.getUserManager();
	private static UserSearchService SERVICE = ComponentAccessor.getUserSearchService();
	
	@Override
	public String getName() {
		return "Application User";
	}
	
	/**
	 * No params
	 */
	@Override
	public Map<String, JiraConfigDTO> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for(ApplicationUser user : SERVICE.findUsersByFullName("")) {
			ApplicationUserDTO item = new ApplicationUserDTO();
			item.setJiraObject(user);
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
		return MANAGER.getUserByKey(uniqueKey);
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
		return ApplicationUserDTO.class;
	}

	@Override
	public boolean isPublic() {
		// TODO Users not supported for now
		return false;
	}

}
