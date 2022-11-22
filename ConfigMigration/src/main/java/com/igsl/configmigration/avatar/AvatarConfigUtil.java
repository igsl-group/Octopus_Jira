package com.igsl.configmigration.avatar;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.icon.IconOwningObjectId;
import com.atlassian.jira.icon.IconType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class AvatarConfigUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(AvatarConfigUtil.class);
	private static AvatarManager MANAGER = ComponentAccessor.getComponent(AvatarManager.class);
	
	@Override
	public String getName() {
		return "Avatar";
	}
	
	@Override
	public TypeReference<?> getTypeReference() {
		return new TypeReference<Map<String, AvatarConfigItem>>() {};
	}

	/**
	 * params[0]: owner as String, optional
	 */
	@Override
	public Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigItem> result = new TreeMap<>();
		// Find among system avatars
		for (Avatar av : MANAGER.getAllSystemAvatars(IconType.ISSUE_TYPE_ICON_TYPE)) {
			AvatarConfigItem item = new AvatarConfigItem();
			item.setJiraObject(av);
			result.put(item.getUniqueKey(), item); 
 		}
		for (Avatar av : MANAGER.getAllSystemAvatars(IconType.PROJECT_ICON_TYPE)) {
			AvatarConfigItem item = new AvatarConfigItem();
			item.setJiraObject(av);
			result.put(item.getUniqueKey(), item); 
 		}
		for (Avatar av : MANAGER.getAllSystemAvatars(IconType.USER_ICON_TYPE)) {
			AvatarConfigItem item = new AvatarConfigItem();
			item.setJiraObject(av);
			result.put(item.getUniqueKey(), item); 
 		}
		// Find among custom avatars of owner
		if (params.length == 1 && String.class.isAssignableFrom(params[0].getClass())) {
			String avatarOwner = (String) params[0];			
			for (Avatar av : MANAGER.getCustomAvatarsForOwner(IconType.ISSUE_TYPE_ICON_TYPE, avatarOwner)) {
				AvatarConfigItem item = new AvatarConfigItem();
				item.setJiraObject(av);
				result.put(item.getUniqueKey(), item); 
			}
			for (Avatar av : MANAGER.getCustomAvatarsForOwner(IconType.PROJECT_ICON_TYPE, avatarOwner)) {
				AvatarConfigItem item = new AvatarConfigItem();
				item.setJiraObject(av);
				result.put(item.getUniqueKey(), item); 
			}
			for (Avatar av : MANAGER.getCustomAvatarsForOwner(IconType.USER_ICON_TYPE, avatarOwner)) {
				AvatarConfigItem item = new AvatarConfigItem();
				item.setJiraObject(av);
				result.put(item.getUniqueKey(), item); 
			}
		}
		return result;
	}

	/**
	 * params[0]: AvatarConfigItem.getUniqueKey()
	 * params[1]: owner as String, optional
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		String uniqueKey = null;
		if (params.length >= 1) {
			uniqueKey = (String) params[0];
		}
		String owner = null;
		if (params.length == 2) {
			owner = (String) params[1];
		}
		Map<String, JiraConfigItem> all;
		if (owner != null) {
			all = readAllItems(owner);
		} else {
			all = readAllItems();
		}
		LOGGER.debug("Avatar list: " + OM.writeValueAsString(all));
		LOGGER.debug("Finding uniqueKey: " + uniqueKey);
		if (all.containsKey(uniqueKey)) {
			LOGGER.debug("Found: " + all.get(uniqueKey).getJiraObject());
			return all.get(uniqueKey).getJiraObject();
		}
		return null;
	}
	
	@Override
	public Object merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception {
		Avatar old = null;
		if (oldItem != null) {
			if (oldItem.getJiraObject() != null) {
				old = (Avatar) oldItem.getJiraObject();
			} else {
				old = (Avatar) findObject(oldItem.getUniqueKey());
			}
		} else {
			old = (Avatar) findObject(newItem.getUniqueKey());
		}
		AvatarConfigItem src = (AvatarConfigItem) newItem;
		if (old != null) {
			// Keep using old one
			return old;
		} else {
			// Create
			LOGGER.debug("Avatar file: " + src.getFileName());
			LOGGER.debug("Avatar content type: " + src.getContentType());
			LOGGER.debug("Avatar icon type: " + src.getIconTypeObject());
			LOGGER.debug("Avatar image data: " + src.getImageData());
			ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(src.getImageData()));
			LOGGER.debug("Avatar image data: " + bais.available());
			IconOwningObjectId owner = null; 
			if (src.getOwner() != null) {
				owner = new IconOwningObjectId(src.getOwner());
			} else {
				owner = new IconOwningObjectId("admin");	// TODO
			}
			LOGGER.debug("Avatar owner: " + owner);
			Avatar created = MANAGER.create(
						src.getFileName(), src.getContentType(), src.getIconTypeObject(), owner, bais, null);
			LOGGER.debug("Avatar created from data: " + created);
			return created;
		}
	}
	
	@Override
	public void merge(Map<String, ImportData> items) throws Exception {
		for (ImportData data : items.values()) {
			try {
				merge(data.getServer(), data.getData());
				data.setImportResult("Updated");
			} catch (Exception ex) {
				data.setImportResult(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
				throw ex;
			}
		}
	}

}
