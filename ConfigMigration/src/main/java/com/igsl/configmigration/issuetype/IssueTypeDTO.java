package com.igsl.configmigration.issuetype;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.icon.IconType;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.avatar.AvatarDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeDTO extends JiraConfigItem {

	private static final AvatarManager AVATAR_MANAGER = ComponentAccessor.getComponent(AvatarManager.class);
	
	private String description;
	private AvatarDTO avatarConfigItem;
	private String name;
	private String id;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		IssueType obj = (IssueType) o;
		this.avatarConfigItem = new AvatarDTO();
		Avatar av;
		if (obj.getAvatar() == null) {
			av = AVATAR_MANAGER.getDefaultAvatar(IconType.ISSUE_TYPE_ICON_TYPE);
		} else {
			av = obj.getAvatar();
		}
		this.avatarConfigItem.setJiraObject(av);
		this.description = obj.getDescription();
		this.name = obj.getName();
		this.id = obj.getId();
	}

	@Override
	public String getUniqueKey() {
		return this.getName();
	}

	@Override
	public String getInternalId() {
		return this.getId();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AvatarDTO getAvatarConfigItem() {
		return avatarConfigItem;
	}

	public void setAvatarConfigItem(AvatarDTO avatarConfigItem) {
		this.avatarConfigItem = avatarConfigItem;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getDescription",
				"getAvatarConfigItem");
	}

}
