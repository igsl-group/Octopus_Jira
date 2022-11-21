package com.igsl.configmigration.issuetype;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.icon.IconOwningObjectId;
import com.atlassian.jira.icon.IconType;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeConfigUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(IssueTypeConfigUtil.class);
	private static IssueTypeManager ISSUE_MANAGER = ComponentAccessor.getComponent(IssueTypeManager.class);
	private static AvatarManager AVATAR_MANAGER = ComponentAccessor.getComponent(AvatarManager.class);
	
	@Override
	public String getName() {
		return "Issue Type";
	}
	
	@Override
	public TypeReference<?> getTypeReference() {
		return new TypeReference<Map<String, IssueTypeConfigItem>>() {};
	}
	
	@Override
	public Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigItem> result = new HashMap<>();
		for (IssueType it : ISSUE_MANAGER.getIssueTypes()) {
			IssueTypeConfigItem item = new IssueTypeConfigItem();
			item.setJiraObject(it);
			result.put(item.getKey(), item);
		}
		return result;
	}

	/**
	 * params[0]: identifier
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		assert params.length == 1 && String.class.isAssignableFrom(params[0].getClass());
		String identifier = (String) params[0];
		for (IssueType it : ISSUE_MANAGER.getIssueTypes()) {
			if (it.getName().equals(identifier)) {
				return it;
			}
		}
		return null;
	}
	
	// TODO Possibly move this to JiraConfigItem if the mechanism can be shared
	private Avatar getAvatar(JiraConfigItem src) {
		Avatar result = null;
		// Get avatar data from JiraConfigItem
		String avatarFileName = src.getMap().get(IssueTypeConfigItem.KEY_AVATAR_FILE_NAME);
		String avatarIconTypeString = src.getMap().get(IssueTypeConfigItem.KEY_AVATAR_ICON_TYPE);
		if (avatarFileName != null && avatarIconTypeString != null) {
			String avatarOwner = src.getMap().get(IssueTypeConfigItem.KEY_AVATAR_OWNER);
			String avatarContentType = src.getMap().get(IssueTypeConfigItem.KEY_AVATAR_CONTENT_TYPE);
			IconType avatarIconType = IconType.of(avatarIconTypeString);
			String avatarBase64 = src.getMap().get(IssueTypeConfigItem.KEY_AVATAR_BASE64);
			// Find among system avatars
			for (Avatar av : AVATAR_MANAGER.getAllSystemAvatars(avatarIconType)) {
				if (JiraConfigItem.compare(av.getContentType(), avatarContentType) == 0 &&
					JiraConfigItem.compare(av.getFileName(), avatarFileName) == 0 &&
					JiraConfigItem.compare(av.getIconType().toString(), avatarIconTypeString) == 0 &&
					JiraConfigItem.compare(av.getOwner(), avatarOwner) == 0) {
					result = av;
					LOGGER.debug("Avatar found, is system avatar: " + result);
					break;
				}
	 		} 
			// Find among custom avatars of owner
			if (result == null) {
				for (Avatar av : AVATAR_MANAGER.getCustomAvatarsForOwner(avatarIconType, avatarOwner)) {
					if (JiraConfigItem.compare(av.getContentType(), avatarContentType) == 0 &&
						JiraConfigItem.compare(av.getFileName(), avatarFileName) == 0 &&
						JiraConfigItem.compare(av.getIconType().toString(), avatarIconTypeString) == 0 &&
						JiraConfigItem.compare(av.getOwner(), avatarOwner) == 0) {
						result = av;
						LOGGER.debug("Avatar found, is custom avatar: " + result);
						break;
					}	
				}
			}
			// Create one
			if (result == null) {
				ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(avatarBase64));
				IconOwningObjectId owner = new IconOwningObjectId(avatarOwner);
				try {
					result = AVATAR_MANAGER.create(avatarFileName, avatarContentType, avatarIconType, owner, bais, null);
					LOGGER.debug("Avatar created from data: " + result);
				} catch (IOException ioex) {
					LOGGER.error("Error creating avatar from data", ioex);
				}
			}
		}
		if (result == null) {
			// Use default issue icon
			result = AVATAR_MANAGER.getDefaultAvatar(IconType.ISSUE_TYPE_ICON_TYPE);
		}
		return result;
	}
	
	public boolean merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception {
		IssueType original = null;
		if (oldItem != null && oldItem.getJiraObject() != null) {
			original = (IssueType) oldItem.getJiraObject();
		} else {
			original = (IssueType) findObject(oldItem.getKey());
		}
		String name = newItem.getMap().get(IssueTypeConfigItem.KEY_NAME);
		String description = newItem.getMap().get(IssueTypeConfigItem.KEY_DESCRIPTION);
		Long avatarId = null;
		Avatar avatar = getAvatar(newItem);
		if (avatar != null) {
			avatarId = avatar.getId();
		}
		if (original != null) {
			// Update
			ISSUE_MANAGER.updateIssueType(original, name, description, avatarId);
			return true;
		} else {
			// Create
			ISSUE_MANAGER.createIssueType(name, description, avatarId);
			return false;
		}
	}
	
	@Override
	public void merge(Map<String, ImportData> items) throws Exception {
		for (ImportData data : items.values()) {
			JiraConfigItem src = data.getData();
			String name = src.getMap().get(IssueTypeConfigItem.KEY_NAME);
			String description = src.getMap().get(IssueTypeConfigItem.KEY_DESCRIPTION);
			Long avatarId = null;
			Avatar avatar = getAvatar(src);
			if (avatar != null) {
				avatarId = avatar.getId();
			}
			try {
				if (merge(data.getServer(), data.getData())) {
					data.setImportResult("Updated");
				} else {
					data.setImportResult("Created");
				}
			} catch (Exception ex) {
				data.setImportResult(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
				throw ex;
			}
		}
	}

}
