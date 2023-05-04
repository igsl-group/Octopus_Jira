package com.igsl.configmigration.projectrole;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.avatar.AvatarDTO;
import com.igsl.configmigration.avatar.AvatarUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectRoleUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ProjectRoleUtil.class);
	private static ProjectRoleManager MANAGER = ComponentAccessor.getComponent(ProjectRoleManager.class);
	
	@Override
	public String getName() {
		return "Project Role";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		ProjectRole role = MANAGER.getProjectRole(idAsLong);
		if (role != null) {
			ProjectRoleDTO item = new ProjectRoleDTO();
			item.setJiraObject(role);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (ProjectRole role : MANAGER.getProjectRoles()) {
			if (role.getName().equals(uniqueKey)) {
				ProjectRoleDTO item = new ProjectRoleDTO();
				item.setJiraObject(role);
				return item;
			}
		}
		return null;
	}

	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		throw new Exception("ProjectRole is read only");
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ProjectRoleDTO.class;
	}

	@Override
	public boolean isVisible() {
		return false;
	}
	
	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (ProjectRole role : MANAGER.getProjectRoles()) {
			ProjectRoleDTO item = new ProjectRoleDTO();
			item.setJiraObject(role);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
