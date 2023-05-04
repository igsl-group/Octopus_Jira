package com.igsl.configmigration.notificationscheme;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.notification.type.AllWatchers;
import com.atlassian.jira.notification.type.CurrentAssignee;
import com.atlassian.jira.notification.type.CurrentReporter;
import com.atlassian.jira.notification.type.GroupCFValue;
import com.atlassian.jira.notification.type.GroupDropdown;
import com.atlassian.jira.notification.type.ProjectLead;
import com.atlassian.jira.notification.type.ProjectRoleSecurityAndNotificationType;
import com.atlassian.jira.notification.type.RemoteUser;
import com.atlassian.jira.notification.type.SingleEmailAddress;
import com.atlassian.jira.notification.type.SingleUser;
import com.atlassian.jira.notification.type.UserCFValue;
import com.atlassian.jira.notification.type.enterprise.ComponentLead;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class NotificationTypeDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(NotificationTypeDTO.class);
	
	private String displayName;
	private String type;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		NotificationTypeUtil util = (NotificationTypeUtil) JiraConfigTypeRegistry.getConfigUtil(NotificationTypeUtil.class);
		NotificationType ev = (NotificationType) o;
		this.displayName = ev.getDisplayName();
		/*
		 * NotificationType.getType() can return null. 
		 * The implementations with non-null .getType() returns something that is not what SchemeEntity expects for .setType().
		 * 
		 * The actual string needed is found inside file notification-event-types.xml in WEB-INF\classes.
		 */
		if (ev instanceof CurrentAssignee) {
			this.type = "Current_Assignee";
		} else if (ev instanceof CurrentReporter) {
			this.type = "Current_Reporter";
		} else if (ev instanceof RemoteUser) {
			this.type = "Remote_User";
		} else if (ev instanceof ProjectLead) {
			this.type = "Project_Lead";
		} else if (ev instanceof ComponentLead) {
			this.type = "Component_Lead";
		} else if (ev instanceof SingleUser) {
			this.type = "Single_User";
		} else if (ev instanceof GroupDropdown) {
			this.type = "Group_Dropdown";
		} else if (ev instanceof ProjectRoleSecurityAndNotificationType) {
			this.type = "Project_Role";
		} else if (ev instanceof SingleEmailAddress) {
			this.type = "Single_Email_Address";
		} else if (ev instanceof AllWatchers) {
			this.type = "All_Watchers";
		} else if (ev instanceof UserCFValue) {
			this.type = "User_Custom_Field_Value";
		} else if (ev instanceof GroupCFValue) {
			this.type = "Group_Custom_Field_Value";
		}
		this.uniqueKey = util.makeUniqueKey(this.displayName, this.type);
	}
	
	@Override
	public String getConfigName() {
		return this.displayName;
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Display Name", new JiraConfigProperty(this.displayName));
		r.put("Type", new JiraConfigProperty(this.type));
		return r;
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getDescription",
				"getCompleteIconUrl",
				"getStatusColor",
				"getSvgIconUrl",
				"getSequence",
				"getIconUrl",
				"getRasterIconUrl");
	}

	@Override
	public String getInternalId() {
		return Integer.toString(this.hashCode());
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return NotificationTypeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return NotificationType.class;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
