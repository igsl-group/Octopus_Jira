package com.igsl.configmigration.project;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.avatar.AvatarUrls;
import com.atlassian.jira.bc.project.ProjectCreationData;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.UpdateProjectParameters;
import com.atlassian.jira.project.template.ProjectTemplate;
import com.atlassian.jira.project.template.ProjectTemplateManager;
import com.atlassian.jira.project.template.hook.ConfigureData;
import com.atlassian.jira.project.type.ProjectType;
import com.atlassian.jira.project.type.ProjectTypeKey;
import com.atlassian.jira.project.type.ProjectTypeManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.avatar.AvatarDTO;
import com.igsl.configmigration.avatar.AvatarUtil;
import com.igsl.configmigration.projectcategory.ProjectCategoryDTO;
import com.igsl.configmigration.projectcategory.ProjectCategoryUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ProjectUtil.class);
	private static final ProjectManager MANAGER = ComponentAccessor.getProjectManager();
	private static final UserManager USER_MANAGER = ComponentAccessor.getUserManager();
	private static final ProjectService SERVICE = ComponentAccessor.getComponent(ProjectService.class);
	private static final ProjectTypeManager TYPE_MANAGER = 
			ComponentAccessor.getComponent(ProjectTypeManager.class);
	private static final ProjectTemplateManager TEMPLATE_MANAGER = 
			ComponentAccessor.getComponent(ProjectTemplateManager.class);
	
	@Override
	public String getName() {
		return "Project";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Project p = MANAGER.getProjectObj(Long.parseLong(id));
		if (p != null) {
			ProjectDTO dto = new ProjectDTO();
			dto.setJiraObject(p);
			return dto;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (Project p : MANAGER.getProjects()) {
			if (p.getName().equals(uniqueKey)) {
				ProjectDTO dto = new ProjectDTO();
				dto.setJiraObject(p);
				return dto;
			}
		}
		return null;
	}

	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		AvatarUtil avatarUtil = (AvatarUtil) JiraConfigTypeRegistry.getConfigUtil(AvatarUtil.class);
		ProjectCategoryUtil categoryUtil = (ProjectCategoryUtil) 
				JiraConfigTypeRegistry.getConfigUtil(ProjectCategoryUtil.class);
		ProjectDTO original = null;
		if (oldItem != null) {
			original = (ProjectDTO) oldItem;
		} else {
			original = (ProjectDTO) findByUniqueKey(newItem.getUniqueKey());
		}
		ProjectDTO src = (ProjectDTO) newItem;
		if (original != null) {
			Project p = (Project) original.getJiraObject();
			// Update
			UpdateProjectParameters data = UpdateProjectParameters.forProject(p.getId());
			data.assigneeType(src.getAssigneeType());
			AvatarDTO avatar = (AvatarDTO) avatarUtil.findByUniqueKey(src.getAvatar().getUniqueKey());
			if (avatar != null) {
				data.avatarId(avatar.getId());
			}
			data.description(src.getDescription());
			data.key(src.getKey());
			data.leadUserKey(src.getLeadUserKey());	// TODO Translate user key using username
			data.name(src.getName());
			ProjectCategoryDTO cat = (ProjectCategoryDTO) 
					categoryUtil.findByUniqueKey(src.getCategory().getUniqueKey());
			if (cat != null) {
				data.projectCategoryId(cat.getId());
			}
			data.projectType(src.getProjectTypeKey().getKey());
			data.url(src.getUrl());
			MANAGER.updateProject(data);
			return findByInternalId(Long.toString(p.getId()));
		} else {
			ProjectTypeKey typeKey = null;
			for (ProjectType pt : TYPE_MANAGER.getAllProjectTypes()) {
				if (src.getProjectTypeKey().getKey().equals(pt.getKey().getKey())) {
					typeKey = pt.getKey();
				}
			}
			if (typeKey == null) {
				throw new Exception("Project Type Key \"" + src.getProjectTypeKey().getKey() + "\" does not exist");
			}
			LOGGER.debug("Project Type Key: " + typeKey.getKey());
			// Create
			ProjectCreationData data = new ProjectCreationData.Builder()
					.withAssigneeType(src.getAssigneeType())
					.withAvatarId(src.getAvatar().getId())
					.withDescription(src.getDescription())
					.withKey(src.getKey())
					//.withLead(USER_MANAGER.getUserByKey(src.getLeadUserKey()))
					.withLead(USER_MANAGER.getUserByName(src.getLeadUserName()))
					.withName(src.getName())
					.withType(typeKey)
					.withUrl(src.getUrl())
					.withProjectTemplateKey(null)
					.build();
			CreateProjectValidationResult result = SERVICE.validateCreateProject(currentUser, data);
			if (!result.isValid()) {
				StringBuilder sb = new StringBuilder();
				if (result.getExistingProjectId().isPresent()) {
					sb.append("Project ID already exists: " + result.getExistingProjectId().get() + "; ");
				}
				for (Map.Entry<String, String> e : result.getErrorCollection().getErrors().entrySet()) {
					sb.append("Error: " + e.getKey() + " = " + e.getValue() + "; ");
				}
				for (String s : result.getErrorCollection().getErrorMessages()) {
					sb.append(s + "; ");
				}
				for (String s : result.getWarningCollection().getWarnings()) {
					sb.append(s + "; ");
				}
				throw new Exception("Project creation data validation failed: " + sb.toString());
			}
			Project p = SERVICE.createProject(result);
			if (src.getCategory() != null) {
				ProjectCategoryUtil catUtil = (ProjectCategoryUtil) 
						JiraConfigTypeRegistry.getConfigUtil(ProjectCategoryUtil.class);
				ProjectCategoryDTO cat = (ProjectCategoryDTO) catUtil.findByUniqueKey(src.getCategory().getUniqueKey());
				MANAGER.setProjectCategory(p, (ProjectCategory) cat.getJiraObject());
			}
			ProjectDTO dto = new ProjectDTO();
			dto.setJiraObject(p);
			return dto;
		}
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ProjectDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new HashMap<>();
		for (Project p : MANAGER.getProjects()) {
			ProjectDTO item = new ProjectDTO();
			item.setJiraObject(p);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
