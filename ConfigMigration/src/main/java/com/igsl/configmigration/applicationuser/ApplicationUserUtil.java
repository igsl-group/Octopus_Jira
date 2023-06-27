package com.igsl.configmigration.applicationuser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.group.GroupRemoveChildMapper;
import com.atlassian.jira.bc.group.GroupService;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.bc.user.UserService.CreateUserRequest;
import com.atlassian.jira.bc.user.UserService.CreateUserValidationResult;
import com.atlassian.jira.bc.user.UserService.UpdateUserValidationResult;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigSearchType;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.general.GeneralDTO;
import com.igsl.configmigration.group.GroupDTO;
import com.opensymphony.module.propertyset.PropertySet;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ApplicationUserUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ApplicationUserUtil.class);
	private static UserManager MANAGER = ComponentAccessor.getUserManager();
	private static UserSearchService SEARCH_SERVICE = ComponentAccessor.getUserSearchService();
	private static UserService SERVICE = ComponentAccessor.getComponent(UserService.class);
	private static final GroupManager GROUP_MANAGER = ComponentAccessor.getGroupManager();
	private static final GroupService GROUP_SERVICE = ComponentAccessor.getComponent(GroupService.class);
	private static final UserPropertyManager PROPERTY_MANAGER = ComponentAccessor.getUserPropertyManager();	
	
	@Override
	public String getName() {
		return "Application User";
	}
	
	public boolean isDefaultObject(JiraConfigDTO dto) {
		if (dto != null) {
			if (JiraConfigDTO.NULL_KEY.equals(dto.getUniqueKey()) || 
				dto.getInternalId() == null) {
				return true;
			}
			// Treat external directory users as default object as well
			ApplicationUserDTO user = (ApplicationUserDTO) dto;
			if (!user.isJiraUser()) {
				return true;
			}
		}	
		return false;
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
		return true;
	}
	
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		ApplicationUserDTO original;
		if (oldItem != null) {
			original = (ApplicationUserDTO) oldItem;
		} else {
			original = (ApplicationUserDTO) findByDTO(newItem);
		}
		ApplicationUserDTO src = (ApplicationUserDTO) newItem;
		ApplicationUser createdJira = null;
		if (original != null) {
			// Update user
			createdJira = SERVICE.newUserBuilder((ApplicationUser) original.getJiraObject())
				.displayName(src.getDisplayName())
				.emailAddress(src.getEmailAddress())
				.build();
			UpdateUserValidationResult uvr = SERVICE.validateUpdateUser(createdJira);
			if (uvr.isValid()) {
				SERVICE.updateUser(uvr);
			} else {
				StringBuilder sb = new StringBuilder();
				for (String s : uvr.getErrorCollection().getErrorMessages()) {
					sb.append(s).append(" ");
				}
				throw new Exception("Failed to update user: " + sb.toString());
			}
		} else {
			// Create user
			// Created user will have random password
			CreateUserRequest cur = CreateUserRequest.withUserDetails(
					getAdminUser(), src.getName(), null, src.getEmailAddress(), src.getDisplayName());
			CreateUserValidationResult cvr = SERVICE.validateCreateUser(cur);
			if (cvr.isValid()) {
				createdJira = SERVICE.createUser(cvr);
			} else {
				StringBuilder sb = new StringBuilder();
				for (String s : cvr.getErrorCollection().getErrorMessages()) {
					sb.append(s).append(" ");
				}
				throw new Exception("Failed to create user: " + sb.toString());
			}
		}
		// Update property set
		PropertySet ps = PROPERTY_MANAGER.getPropertySet(createdJira);
		ps.remove();	// Is it too risky to wipe them all out?
		for (Map.Entry<String, GeneralDTO> entry : src.getProperties().entrySet()) {
			GeneralDTO dto = entry.getValue();
			ps.setAsActualType(entry.getKey(), dto.getValue());
		}
		// Update group membership
		List<String> removeGroups = new ArrayList<>();
		if (original != null) {
			for (GroupDTO dto : original.getGroups()) {
				removeGroups.add(dto.getName());
			}
		}
		for (GroupDTO dto : src.getGroups()) {
			removeGroups.remove(dto.getName());
		}
		List<String> addGroups = new ArrayList<>();
		for (GroupDTO dto : src.getGroups()) {
			addGroups.add(dto.getName());
		}
		if (original != null) {
			for (GroupDTO dto : original.getGroups()) {
				addGroups.remove(dto.getName());
			}
		}
		JiraServiceContext ctx = new JiraServiceContextImpl(getAdminUser());
		GroupRemoveChildMapper removeMapper = new GroupRemoveChildMapper(removeGroups);
		removeMapper.register(src.getName());
		boolean removeResult = GROUP_SERVICE.removeUsersFromGroups(ctx, removeMapper);
		if (!removeResult) {
			StringBuilder sb = new StringBuilder();
			for (String s : removeGroups) {
				sb.append(s).append(", ");
			}
			String list = "";
			if (sb.length() > 2) {
				list = sb.substring(0, sb.length() - 2);
			}
			result.addWarning("Unable to remove group(s): " + list);
		}
		List<String> users = new ArrayList<>();
		users.add(createdJira.getName());
		boolean addResult = GROUP_SERVICE.addUsersToGroups(ctx, addGroups, users);
		if (!addResult) {
			StringBuilder sb = new StringBuilder();
			for (String s : addGroups) {
				sb.append(s).append(", ");
			}
			String list = "";
			if (sb.length() > 2) {
				list = sb.substring(0, sb.length() - 2);
			}
			result.addWarning("Unable to add group(s): " + list);
		}		
		// Reload user
		createdJira = MANAGER.getUserByName(createdJira.getName());
		// Set result
		ApplicationUserDTO created = new ApplicationUserDTO();
		created.setJiraObject(createdJira);
		result.setNewDTO(created);
		return result;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		int limit = 100000;
		LOGGER.debug("UserSearchService: " + SEARCH_SERVICE.getClass().getCanonicalName());
		UserSearchParams sp = UserSearchParams.builder(limit)
				.allowEmptyQuery(true)
				.includeActive(true)
				.includeInactive(true)
				.build();
		LOGGER.debug("UserSearchParams: " + sp);
		for (ApplicationUser user : SEARCH_SERVICE.findUsers("", sp)) {
			LOGGER.debug("search found user: " + user.getKey() + ": " + user.getName());
			ApplicationUserDTO dto = new ApplicationUserDTO();
			dto.setJiraObject(user, params);
			if (filter != null) {
				if (!matchFilter(dto, filter)) {
					continue;
				}
			}
			result.put(dto.getUniqueKey(), dto);
		}
		return result;
	}
	
	@Override
	public List<JiraConfigSearchType> getSearchTypes() {
		return ApplicationUserSearchType.values();
	}

}
