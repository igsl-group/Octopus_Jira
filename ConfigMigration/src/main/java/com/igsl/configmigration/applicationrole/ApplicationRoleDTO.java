package com.igsl.configmigration.applicationrole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.application.ApplicationRole;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.group.GroupDTO;
import com.igsl.configmigration.group.GroupUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ApplicationRoleDTO extends JiraConfigDTO {

	private List<GroupDTO> defaultGroups;
	private List<GroupDTO> groups;
	private String key;
	private String name;
	private int numberOfSeats;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		ApplicationRole o = (ApplicationRole) obj;
		if (o.getDefaultGroups() != null) {
			this.defaultGroups = new ArrayList<>();
			for (Group g : o.getDefaultGroups()) {
				GroupDTO dto = new GroupDTO();
				dto.setJiraObject(g);
				this.defaultGroups.add(dto);
			}
		}
		if (o.getGroups() != null) {
			this.groups = new ArrayList<>();
			for (Group g : o.getGroups()) {
				GroupDTO dto = new GroupDTO();
				dto.setJiraObject(g);
				this.groups.add(dto);
			}
		}
		this.key = o.getKey().toString();
		this.name = o.getName();
		this.numberOfSeats = o.getNumberOfSeats();
		this.uniqueKey = this.key;
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Default Groups", new JiraConfigProperty(GroupUtil.class, this.defaultGroups));
		r.put("Groups", new JiraConfigProperty(GroupUtil.class, this.groups));
		r.put("Application Key", new JiraConfigProperty(this.key));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("No. of Seats", new JiraConfigProperty(this.numberOfSeats));
		return r;
	}
	
	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return ApplicationRoleUtil.class;
	}
	
	@Override
	public String getInternalId() {
		return Long.toString(this.hashCode());
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
		return ApplicationRole.class;
	}

	public List<GroupDTO> getDefaultGroups() {
		return defaultGroups;
	}

	public void setDefaultGroups(List<GroupDTO> defaultGroups) {
		this.defaultGroups = defaultGroups;
	}

	public List<GroupDTO> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupDTO> groups) {
		this.groups = groups;
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

	public int getNumberOfSeats() {
		return numberOfSeats;
	}

	public void setNumberOfSeats(int numberOfSeats) {
		this.numberOfSeats = numberOfSeats;
	}

}
