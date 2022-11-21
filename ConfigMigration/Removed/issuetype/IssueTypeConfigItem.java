package com.igsl.configmigration.issuetype;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.icon.IconType;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.util.Base64InputStreamConsumer;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeConfigItem extends JiraConfigItem {

	private static final AvatarManager AVATAR_MANAGER = ComponentAccessor.getComponent(AvatarManager.class);
	
	public static final String KEY_DESCRIPTION = "Description";
	public static final String KEY_AVATAR_ID = "Avatar ID";
	public static final String KEY_AVATAR_FILE_NAME = "Avatar File Name";
	public static final String KEY_AVATAR_CONTENT_TYPE = "Avatar Content Type";
	public static final String KEY_AVATAR_ICON_TYPE = "Avatar Icon Type";
	public static final String KEY_AVATAR_OWNER = "Avatar Owner";
	public static final String KEY_AVATAR_BASE64 = "Avatar Image";
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		IssueType obj = (IssueType) o;
		this.getMap().put(KEY_ID, obj.getId());
		this.getMap().put(KEY_NAME, obj.getName());
		this.getMap().put(KEY_DESCRIPTION, obj.getDescription());
		Avatar avatar = obj.getAvatar();
		if (avatar == null) {
			avatar = AVATAR_MANAGER.getDefaultAvatar(IconType.ISSUE_TYPE_ICON_TYPE);
		}
		Avatar.Size size = Avatar.Size.defaultSize();
		Base64InputStreamConsumer stream = new Base64InputStreamConsumer(false);
		AVATAR_MANAGER.readAvatarData(avatar, size, stream);
		this.getMap().put(KEY_AVATAR_BASE64, stream.getEncoded());			
		this.getMap().put(KEY_AVATAR_ID, Long.toString(avatar.getId()));
		this.getMap().put(KEY_AVATAR_FILE_NAME, avatar.getFileName());
		this.getMap().put(KEY_AVATAR_CONTENT_TYPE, avatar.getContentType());
		this.getMap().put(KEY_AVATAR_ICON_TYPE, avatar.getIconType().toString());
		this.getMap().put(KEY_AVATAR_OWNER, avatar.getOwner());
	}

	@Override
	public int compareTo(JiraConfigItem o) {
		if (o != null) {
			return compare(
					this, o,
					KEY_NAME,
					KEY_DESCRIPTION,
					KEY_AVATAR_BASE64,
					KEY_AVATAR_FILE_NAME,
					KEY_AVATAR_CONTENT_TYPE,
					KEY_AVATAR_ICON_TYPE,
					KEY_AVATAR_OWNER);
		}
		return 1;
	}

}
