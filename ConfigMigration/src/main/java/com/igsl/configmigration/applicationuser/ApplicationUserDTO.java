package com.igsl.configmigration.applicationuser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.GroupView;
import com.atlassian.jira.bc.user.UserApplicationHelper;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.general.GeneralDTO;
import com.igsl.configmigration.general.GeneralUtil;
import com.igsl.configmigration.group.GroupDTO;
import com.igsl.configmigration.group.GroupUtil;
import com.opensymphony.module.propertyset.PropertySet;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ApplicationUserDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(ApplicationUserDTO.class);
	private static final UserManager USER_MANAGER = ComponentAccessor.getUserManager();
	private static final UserPropertyManager PROPERTY_MANAGER = ComponentAccessor.getUserPropertyManager();	
	private static final CrowdService CROWD_SERVICE = ComponentAccessor.getCrowdService();
	private static final UserApplicationHelper USER_APPLICATION_HELPER = ComponentAccessor.getComponent(UserApplicationHelper.class);
	
	/*
	 * 	SQL for checking user property values
		SELECT 
			e.*, 
			CASE e.PROPERTYTYPE
				WHEN 1 THEN 'Boolean'
				WHEN 2 THEN 'Int'
				WHEN 3 THEN 'Long'
				WHEN 4 THEN 'Double'
				WHEN 5 THEN 'String 255'
				WHEN 6 THEN 'String unlimited'
				WHEN 7 THEN 'Date'
				WHEN 8 THEN 'Object'
				WHEN 9 THEN 'XML'
				WHEN 10 THEN 'Data'
			END as Type,
			CASE e.PROPERTYTYPE
				WHEN 1 OR 2 OR 3 THEN CONVERT(number.PROPERTYVALUE, VARCHAR)
				WHEN 4 THEN CONVERT(decimal.PROPERTYVALUE, VARCHAR)
				WHEN 5 THEN CONVERT(string.PROPERTYVALUE, VARCHAR)
				WHEN 6 THEN CONVERT(text.PROPERTYVALUE, VARCHAR)
				WHEN 7 THEN CONVERT(date.PROPERTYVALUE, VARCHAR)
				WHEN 8 OR 9 OR 10 THEN CONVERT(data.PROPERTYVALUE, VARCHAR)
			END as Value
		FROM PROPERTYENTRY e 
			LEFT JOIN PROPERTYNUMBER number ON number.ID = e.ID AND e.PROPERTYTYPE IN (1, 2, 3)
			LEFT JOIN PROPERTYDECIMAL decimal on decimal.ID = e.ID and e.PROPERTYTYPE IN (4)
			LEFT JOIN PROPERTYSTRING string on string.ID = e.ID and e.PROPERTYTYPE IN (5)
			LEFT JOIN PROPERTYTEXT text on text.ID = e.ID and e.PROPERTYTYPE IN (6)
			LEFT JOIN PROPERTYDATE date ON date.ID = e.ID and e.PROPERTYTYPE IN (7)
			LEFT JOIN PROPERTYDATA data ON data.ID = e.ID AND e.PROPERTYTYPE IN (8, 9, 10)
		WHERE e.ENTITY_NAME = 'ApplicationUser' AND e.ENTITY_ID LIKE '%';
	 */
	
	// TODO Better way to identify internal directory?
	private static final String JIRA_INTERNAL_DIRECTORY = "Jira Internal Directory";	
	private Long id;
	private String key;
	private String name;
	private String userName;
	private String emailAddress;
	private String displayName;
	private boolean jiraUser;	// If user is in Jira, i.e. not in external directory
	private Map<String, GeneralDTO> properties;
	private List<GroupDTO> groups;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		ApplicationUser o = (ApplicationUser) obj;
		this.id = o.getId();
		this.key = o.getKey();
		this.name = o.getName();
		this.displayName = o.getDisplayName();
		this.emailAddress = o.getEmailAddress();
		this.userName = o.getUsername();
		this.uniqueKey = o.getEmailAddress();
		Directory dir = USER_MANAGER.getDirectory(o.getDirectoryId());
		if (dir != null) {
			jiraUser = dir.getName().equals(JIRA_INTERNAL_DIRECTORY);
		}
		// Properties
		this.properties = new HashMap<>();
		PropertySet ps = PROPERTY_MANAGER.getPropertySet(o);
		LOGGER.debug("User: " + this.name + " Properties count: " + ps.getKeys().size());
		for (Object k : ps.getKeys()) {
			String key = String.valueOf(k);
			Object value = ps.getAsActualType(key);
			LOGGER.debug("Property [" + key + "][" + value + "]" + ((value != null)? "(" + value.getClass().getCanonicalName() + ")" : ""));
			GeneralDTO dto = new GeneralDTO();
			dto.setJiraObject(value);
			this.properties.put(key, dto);
		}
		// Group membership
		// GroupManager is not used here because it seems to be caching information and has a delay in update
		// CrowdService is used instead
		this.groups = new ArrayList<>();
		for (GroupView gv : USER_APPLICATION_HELPER.getUserGroups(o)) {
			Group grp = CROWD_SERVICE.getGroup(gv.getName());
			if (grp != null) {
				User user = CROWD_SERVICE.getUser(o.getName());
				if (CROWD_SERVICE.isUserDirectGroupMember(user, grp)) {
					GroupDTO dto = new GroupDTO();
					dto.setJiraObject(grp);
					this.groups.add(dto);
				}
			}
		}
	}

	@Override
	public void setupRelatedObjects() {
		for (GroupDTO dto : this.groups) {
			this.addRelatedObject(dto);
			dto.addReferencedObject(this);
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
		r.put("Properties", new JiraConfigProperty(GeneralUtil.class, this.properties));
		r.put("Groups", new JiraConfigProperty(GroupUtil.class, this.groups));
		r.put("Internal Directory User", new JiraConfigProperty(this.jiraUser));
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

	public Map<String, GeneralDTO> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, GeneralDTO> properties) {
		this.properties = properties;
	}

	public List<GroupDTO> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupDTO> groups) {
		this.groups = groups;
	}

}
