package com.igsl.configmigration.avatar;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.icon.IconOwningObjectId;
import com.atlassian.jira.icon.IconType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class AvatarUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(AvatarUtil.class);
	private static AvatarManager MANAGER = ComponentAccessor.getComponent(AvatarManager.class);
	
	@Override
	public String getName() {
		return "Avatar";
	}

	@SuppressWarnings("rawtypes")
	public Comparator getComparator() {
		return new AvatarComparator();
	}
	
	/**
	 * #0: owner as String, optional
	 */
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Map<String, JiraConfigDTO> all = search(null, params);
		for (JiraConfigDTO item : all.values()) {
			if (item.getInternalId().equals(id)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * #0: owner as String, optional
	 */
	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		Map<String, JiraConfigDTO> all = search(null, params);
		if (all.containsKey(uniqueKey)) {
			return all.get(uniqueKey);
		}
		return null;
	}
	
	@Override
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		AvatarDTO old = null;
		if (oldItem != null) {
			old = (AvatarDTO) oldItem;
		} else {
			old = (AvatarDTO) findByDTO(newItem);
		}
		AvatarDTO src = (AvatarDTO) newItem;
		if (old != null) {
			// Keep using old one
			// TODO Delete and recreate?
			result.setNewDTO(old);
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
				owner = new IconOwningObjectId("admin");	// TODO Find admin user
			}
			LOGGER.debug("Avatar owner: " + owner);
			Avatar createdJira = MANAGER.create(
						src.getFileName(), src.getContentType(), src.getIconTypeObject(), owner, bais, null);
			LOGGER.debug("Avatar created from data: " + createdJira);
			LOGGER.debug("Avatar ID: " + createdJira.getId());
			AvatarDTO created = new AvatarDTO();
			created.setJiraObject(createdJira);
			result.setNewDTO(created);
		}
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return AvatarDTO.class;
	}

	@Override
	public boolean isVisible() {
		// Avatar is only referenced via other DTOs
		return false;
	}

	public boolean isReadOnly() {
		// Avatar can be created by itself
		return false;
	}
	
	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		// Filter is ignored
		Map<String, JiraConfigDTO> result = new LinkedHashMap<>();
		List<AvatarDTO> list = new ArrayList<>();
		// Find among system avatars
		for (Avatar av : MANAGER.getAllSystemAvatars(IconType.ISSUE_TYPE_ICON_TYPE)) {
			AvatarDTO item = new AvatarDTO();
			try {
				item.setJiraObject(av, params);
				list.add(item); 
			} catch (Exception ex) {
				LOGGER.error("Error loading system avatar (issue type)", ex);
			}
 		}
		for (Avatar av : MANAGER.getAllSystemAvatars(IconType.PROJECT_ICON_TYPE)) {
			AvatarDTO item = new AvatarDTO();
			try {
				item.setJiraObject(av, params);
				list.add(item); 
			} catch (Exception ex) {
				LOGGER.error("Error loading system avatar (project)", ex);
			}
		}
		for (Avatar av : MANAGER.getAllSystemAvatars(IconType.USER_ICON_TYPE)) {
			AvatarDTO item = new AvatarDTO();
			try {
				item.setJiraObject(av, params);
				list.add(item); 
			} catch (Exception ex) {
				LOGGER.error("Error loading user avatar", ex);
			}
 		}
		// Find among custom avatars of owner
		String avatarOwner = null;
		if (params != null && params.length == 1) {
			avatarOwner = (String) params[0];
			for (Avatar av : MANAGER.getCustomAvatarsForOwner(IconType.ISSUE_TYPE_ICON_TYPE, avatarOwner)) {
				AvatarDTO item = new AvatarDTO();
				try {
					item.setJiraObject(av, params);
					list.add(item); 
				} catch (Exception ex) {
					LOGGER.error("Error loading custom avatar (issue type)", ex);
				}
			}
			for (Avatar av : MANAGER.getCustomAvatarsForOwner(IconType.PROJECT_ICON_TYPE, avatarOwner)) {
				AvatarDTO item = new AvatarDTO();
				try {
					item.setJiraObject(av, params);
					list.add(item); 
				} catch (Exception ex) {
					LOGGER.error("Error loading custom avatar (project)", ex);
				}
			}
			for (Avatar av : MANAGER.getCustomAvatarsForOwner(IconType.USER_ICON_TYPE, avatarOwner)) {
				AvatarDTO item = new AvatarDTO();
				try {
					item.setJiraObject(av, params);
					list.add(item); 
				} catch (Exception ex) {
					LOGGER.error("Error loading custom avatar (user)", ex);
				}
			}
		}
		list.sort(new AvatarComparator());
		for (AvatarDTO dto : list) {
			result.put(dto.getUniqueKey(), dto);
		}
		return result;
	}

}
