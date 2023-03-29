package com.igsl.configmigration.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.type.ProjectTypeKey;
import com.atlassian.jira.project.version.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.applicationuser.ApplicationUserDTO;
import com.igsl.configmigration.applicationuser.ApplicationUserUtil;
import com.igsl.configmigration.avatar.AvatarDTO;
import com.igsl.configmigration.avatar.AvatarUtil;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.issuetype.IssueTypeUtil;
import com.igsl.configmigration.projectcategory.ProjectCategoryDTO;
import com.igsl.configmigration.projectcategory.ProjectCategoryUtil;
import com.igsl.configmigration.projectcomponent.ProjectComponentDTO;
import com.igsl.configmigration.projectcomponent.ProjectComponentUtil;
import com.igsl.configmigration.version.VersionDTO;
import com.igsl.configmigration.version.VersionUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(ProjectDTO.class);
	
	protected Long id;
	protected String key;
	protected String name;
	protected ApplicationUserDTO archivedBy;
	protected Date archivedDate;
	protected Long assigneeType;
	protected AvatarDTO avatar;
	protected String description;
	protected String email;
	protected Collection<IssueTypeDTO> issueTypes;
	protected String leadUserKey;
	protected String leadUserName;
	protected String originalKey;
	protected Collection<ProjectComponentDTO> components;
	protected ProjectCategoryDTO category;
	protected String url;
	protected Collection<VersionDTO> versions;
	protected ProjectTypeKeyDTO projectTypeKey;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		Project o = (Project) obj;
		this.archivedBy = new ApplicationUserDTO();
		this.archivedBy.setJiraObject(o.getArchivedBy());
		this.archivedDate = o.getArchivedDate();
		this.assigneeType = o.getAssigneeType();
		this.avatar = new AvatarDTO();
		this.avatar.setJiraObject(o.getAvatar());
		this.components = new ArrayList<>();
		for (ProjectComponent item : o.getComponents()) {
			ProjectComponentDTO dto = new ProjectComponentDTO();
			dto.setJiraObject(item, o.getId());
			this.components.add(dto);
		}
		this.description = o.getDescription();
		this.email = o.getEmail();
		this.id = o.getId();
		this.issueTypes = new ArrayList<>();
		for (IssueType it : o.getIssueTypes()) {
			IssueTypeDTO dto = new IssueTypeDTO();
			dto.setJiraObject(it);
			this.issueTypes.add(dto);
		}
		this.key = o.getKey();
		this.leadUserKey = o.getLeadUserKey();
		this.leadUserName = o.getLeadUserName();
		this.name = o.getName();
		this.originalKey = o.getOriginalKey();
		if (o.getProjectCategory() != null) {
			this.category = new ProjectCategoryDTO();
			this.category.setJiraObject(o.getProjectCategory());
		}
		this.projectTypeKey = new ProjectTypeKeyDTO();
		this.projectTypeKey.setJiraObject(o.getProjectTypeKey());
		this.url = o.getUrl();
		this.versions = new ArrayList<>();
		for (Version item : o.getVersions()) {
			VersionDTO dto = new VersionDTO();
			dto.setJiraObject(item);
			this.versions.add(dto);
		}
		this.uniqueKey = this.name;
	}
	
	@Override
	protected void setupRelatedObjects() throws Exception {
		if (this.avatar != null) {
			LOGGER.debug("Project related avatar: " + this.avatar);
			addRelatedObject(this.avatar);
			this.avatar.addReferencedObject(this);
		}
		if (this.components != null) {
			for (ProjectComponentDTO comp : this.components) {
				LOGGER.debug("Project related components: " + comp);
				addRelatedObject(comp);
				comp.addReferencedObject(this);
			}
		}
		if (this.issueTypes != null) {
			for (IssueTypeDTO issueType : this.issueTypes) {
				LOGGER.debug("Project related issueType: " + issueType);
				addRelatedObject(issueType);
				issueType.addReferencedObject(this);
			}
		}
		if (this.category != null) {
			LOGGER.debug("Project related category: " + this.category);
			addRelatedObject(this.category);
			this.category.addReferencedObject(this);
		}
		if (this.versions != null) {
			for (VersionDTO version : this.versions) {
				LOGGER.debug("Project related version: " + version);
				addRelatedObject(version);
				version.addReferencedObject(this);
			}
		}
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Archived By", new JiraConfigProperty(ApplicationUserUtil.class, this.archivedBy));
		r.put("Archived Date", new JiraConfigProperty(this.archivedDate));
		r.put("Assignee Type", new JiraConfigProperty(this.assigneeType));
		r.put("Avatar", new JiraConfigProperty(AvatarUtil.class, this.avatar));
		r.put("Project Components", new JiraConfigProperty(ProjectComponentUtil.class, this.components));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Email", new JiraConfigProperty(this.email));
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Issue Types", new JiraConfigProperty(IssueTypeUtil.class, this.issueTypes));
		r.put("Key", new JiraConfigProperty(this.key));
		r.put("Leader User Key", new JiraConfigProperty(this.leadUserKey));
		r.put("Leader User Name", new JiraConfigProperty(this.leadUserName));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Original Key", new JiraConfigProperty(this.originalKey));
		r.put("Project Category", new JiraConfigProperty(ProjectCategoryUtil.class, this.category));
		r.put("Project Type Key", new JiraConfigProperty(ProjectTypeKeyUtil.class, this.projectTypeKey));
		r.put("URL", new JiraConfigProperty(this.url));
		r.put("Versions", new JiraConfigProperty(VersionUtil.class, this.versions));
		return r;
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getKey",
				"getName",
				"getArchivedBy",
				"getArchivedDate",
				"getAssigneeType",
				"getAvatar",
				"getDescription",
				"getEmail",
				"getIssueTypes",
				"getLeadUserKey",
				"getLeadUserName",
				"getOriginalKey",
				"getComponents",
				"getCategory",
				"getUrl",
				"getVersions",
				"getProjectTypeKey");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return ProjectUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return Project.class;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ApplicationUserDTO getArchivedBy() {
		return archivedBy;
	}

	public void setArchivedBy(ApplicationUserDTO archivedBy) {
		this.archivedBy = archivedBy;
	}

	public Date getArchivedDate() {
		return archivedDate;
	}

	public void setArchivedDate(Date archivedDate) {
		this.archivedDate = archivedDate;
	}

	public Long getAssigneeType() {
		return assigneeType;
	}

	public void setAssigneeType(Long assigneeType) {
		this.assigneeType = assigneeType;
	}

	public AvatarDTO getAvatar() {
		return avatar;
	}

	public void setAvatar(AvatarDTO avatar) {
		this.avatar = avatar;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Collection<IssueTypeDTO> getIssueTypes() {
		return issueTypes;
	}

	public void setIssueTypes(Collection<IssueTypeDTO> issueTypes) {
		this.issueTypes = issueTypes;
	}

	public String getLeadUserKey() {
		return leadUserKey;
	}

	public void setLeadUserKey(String leadUserKey) {
		this.leadUserKey = leadUserKey;
	}

	public String getLeadUserName() {
		return leadUserName;
	}

	public void setLeadUserName(String leadUserName) {
		this.leadUserName = leadUserName;
	}

	public String getOriginalKey() {
		return originalKey;
	}

	public void setOriginalKey(String originalKey) {
		this.originalKey = originalKey;
	}

	public Collection<ProjectComponentDTO> getComponents() {
		return components;
	}

	public void setComponents(Collection<ProjectComponentDTO> components) {
		this.components = components;
	}

	public ProjectCategoryDTO getCategory() {
		return category;
	}

	public void setCategory(ProjectCategoryDTO category) {
		this.category = category;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Collection<VersionDTO> getVersions() {
		return versions;
	}

	public void setVersions(Collection<VersionDTO> versions) {
		this.versions = versions;
	}

	public ProjectTypeKeyDTO getProjectTypeKey() {
		return projectTypeKey;
	}

	public void setProjectTypeKey(ProjectTypeKeyDTO projectTypeKey) {
		this.projectTypeKey = projectTypeKey;
	}

}
