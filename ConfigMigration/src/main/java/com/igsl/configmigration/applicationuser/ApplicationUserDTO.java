package com.igsl.configmigration.applicationuser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ApplicationUserDTO extends JiraConfigDTO {

	private static final UserManager USER_MANAGER = ComponentAccessor.getUserManager();
	// TODO Better way to identify internal directory?
	private static final String JIRA_INTERNAL_DIRECTORY = "Jira Internal Directory";	
	private Long id;
	private String key;
	private String name;
	private String userName;
	private String emailAddress;
	private String displayName;
	private boolean jiraUser;	// If user is in Jira, i.e. not in external directory
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		ApplicationUser o = (ApplicationUser) obj;
		this.id = o.getId();
		this.key = o.getKey();
		this.name = o.getName();
		this.displayName = o.getDisplayName();
		this.emailAddress = o.getEmailAddress();
		this.userName = o.getUsername();
		this.uniqueKey = o.getKey();
		Directory dir = USER_MANAGER.getDirectory(o.getDirectoryId());
		if (dir != null) {
			jiraUser = dir.getName().equals(JIRA_INTERNAL_DIRECTORY);
		}
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Display Name", new JiraConfigProperty(displayName));
		r.put("User Name", new JiraConfigProperty(userName));
		r.put("Email Address", new JiraConfigProperty(emailAddress));
		r.put("Key", new JiraConfigProperty(key));
		r.put("ID", new JiraConfigProperty(id));
		r.put("Jira Internal Directory User", new JiraConfigProperty(this.jiraUser));
		return r;
	}
	
	@Override
	public String getConfigName() {
		if (this.getInternalId() != null) {
			return this.displayName + " (" + this.name + ")";
		} 
		return DEFAULT_KEY;
	}
	
	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return ApplicationUserUtil.class;
	}
	
	@Override
	public String getInternalId() {
		if (this.id != null) {
			return Long.toString(this.id);
		} 
		return null;
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getKey",
				"getUserName",
				"getEmailAddress",
				"getDisplayName");
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public Class<?> getJiraClass() {
		return ApplicationUser.class;
	}

	public boolean isJiraUser() {
		return jiraUser;
	}

	public void setJiraUser(boolean jiraUser) {
		this.jiraUser = jiraUser;
	}

}
