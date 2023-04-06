package com.igsl.configmigration.applicationuser;

import java.util.Comparator;
import java.util.LinkedHashMap;
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
import com.igsl.configmigration.DTOStore;
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
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		Optional<ApplicationUser> user = MANAGER.getUserById(idAsLong);
		if (user != null && user.isPresent()) {
			ApplicationUserDTO dto = new ApplicationUserDTO();
			dto.setJiraObject(user.get(), params);
			return dto;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		ApplicationUser user = MANAGER.getUserByKey(uniqueKey);
		if (user != null) {
			ApplicationUserDTO dto = new ApplicationUserDTO();
			dto.setJiraObject(user, params);
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

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for(ApplicationUser user : SERVICE.findUsersByFullName("")) {
			ApplicationUserDTO dto = new ApplicationUserDTO();
			dto.setJiraObject(null, user, params);
			if (filter != null) {
				if (!matchFilter(dto, filter)) {
					continue;
				}
			}
			result.put(dto.getUniqueKey(), dto);
		}
		return result;
	}

}
