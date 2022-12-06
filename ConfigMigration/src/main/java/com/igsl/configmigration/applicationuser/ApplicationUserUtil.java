package com.igsl.configmigration.applicationuser;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.LazyLoadingApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ApplicationUserUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ApplicationUserUtil.class);
	private static UserManager MANAGER = ComponentAccessor.getUserManager();
	private static UserSearchService SERVICE = ComponentAccessor.getUserSearchService();
	
	@Override
	public String getName() {
		return "Application User";
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for(ApplicationUser user : SERVICE.findUsersByFullName("")) {
			ApplicationUserDTO item = new ApplicationUserDTO();
			item.setJiraObject(user);
			result.put(item.getUniqueKey(), item);					
		}
		return result;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		Optional<ApplicationUser> user = MANAGER.getUserById(idAsLong);
		if (user != null && user.isPresent()) {
			ApplicationUserDTO dto = new ApplicationUserDTO();
			dto.setJiraObject(user.get());
			return dto;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		ApplicationUser user = MANAGER.getUserByKey(uniqueKey);
		if (user != null) {
			ApplicationUserDTO dto = new ApplicationUserDTO();
			dto.setJiraObject(user);
			return dto;
		}
		return null;
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ApplicationUserDTO.class;
	}

	@Override
	public boolean isVisible() {
		// TODO Users not supported for now
		return false;
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		throw new Exception("Not implemented");
	}

}
