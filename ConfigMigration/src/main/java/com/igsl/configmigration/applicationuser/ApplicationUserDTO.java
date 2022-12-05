package com.igsl.configmigration.applicationuser;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.user.ApplicationUser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ApplicationUserDTO extends JiraConfigDTO {

	private Long id;
	private String key;
	private String name;
	private String userName;
	private String emailAddress;
	private String displayName;
	
	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		ApplicationUser o = (ApplicationUser) obj;
		this.id = o.getId();
		this.key = o.getKey();
		this.name = o.getName();
		this.displayName = o.getDisplayName();
		this.emailAddress = o.getEmailAddress();
		this.userName = o.getUsername();
	}

	@Override
	public String getUniqueKey() {
		return this.getKey();
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
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

}
