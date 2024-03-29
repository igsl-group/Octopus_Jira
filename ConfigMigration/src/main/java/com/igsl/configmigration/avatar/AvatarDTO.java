package com.igsl.configmigration.avatar;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.icon.IconType;
import com.atlassian.jira.util.Base64InputStreamConsumer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class AvatarDTO extends JiraConfigDTO {

	private static AvatarManager AVATAR_MANAGER = ComponentAccessor.getComponent(AvatarManager.class);

	protected Long id;
	protected String owner;
	protected String fileName;
	protected String iconType;
	protected String contentType;
	protected String imageData;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		Avatar o = (Avatar) obj;
		this.contentType = o.getContentType();
		this.fileName = o.getFileName();
		this.iconType = o.getIconType().toString();
		this.id = o.getId();
		this.owner = o.getOwner();
		Avatar.Size size = Avatar.Size.defaultSize();
		Base64InputStreamConsumer stream = new Base64InputStreamConsumer(false);
		AVATAR_MANAGER.readAvatarData(o, size, stream);
		this.imageData = stream.getEncoded();
		// Avatar upload always changes the filename and image content
		// So it is impossible to map it using anything but ID
		// During migration avatars will always be created
		// So for unique name, we just make sure it won't conflict with another avatar on the current server
		this.uniqueKey = Long.toString(this.id);
	}
	
	@Override
	public String getConfigName() {
		return this.fileName + " (" + this.id + ")";
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Owner", new JiraConfigProperty(owner));
		r.put("File Name", new JiraConfigProperty(fileName));
		r.put("Content Type", new JiraConfigProperty(contentType));
		r.put("Icon Type", new JiraConfigProperty(iconType));
		r.put("Image", new JiraConfigProperty(this.contentType, this.imageData));
		return r;
	}
	
	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return AvatarUtil.class;
	}
	
	@JsonIgnore
	public IconType getIconTypeObject() {
		return IconType.of(this.getIconType());
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getIconType() {
		return iconType;
	}

	public void setIconType(String iconType) {
		this.iconType = iconType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public String getImageData() {
		return imageData;
	}

	public void setImageData(String imageData) {
		this.imageData = imageData;
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getOwner", 
				"getFileName", 
				"getContentType",
				"getIconType",
				"getImageData");
	}

	@Override
	public Class<?> getJiraClass() {
		return Avatar.class;
	}

}
