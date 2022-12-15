package com.igsl.customapproval.delegation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.igsl.customapproval.PluginUtil;
import com.igsl.customapproval.data.DelegationSetting;
import com.opensymphony.module.propertyset.PropertySet;

public class UserDelegation extends JiraWebActionSupport {

	/* 
SQL to check property set data: 
SELECT u.USER_NAME, u.DISPLAY_NAME, pt.*
FROM PROPERTYTEXT pt 
JOIN PROPERTYENTRY pe 
	JOIN CWD_USER u 
		ON u.ID = pe.ENTITY_ID
	ON pe.ID = pt.ID 
WHERE pe.PROPERTY_KEY = 'customApprovalDelegation';

SQL to delete all property data we set: 
DELETE PROPERTYTEXT WHERE ID IN (
	SELECT ID FROM PROPERTYENTRY WHERE PROPERTY_KEY = 'customApprovalDelegation'
);
DELETE PROPERTYENTRY WHERE PROPERTY_KEY = 'customApprovalDelegation';
	 */
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(UserDelegation.class);
	
	private static final String[] ADMIN_GROUPS = {"jira-administrators"};	// TODO config page
	
	private static final GroupManager GM = ComponentAccessor.getGroupManager();
	
	private static final String PARAM_ADMIN = "admin";
	private static final String PARAM_FROM_USER = "fromUserKey";
	private static final String PARAM_ADD = "add";
	private static final String PARAM_DELETE = "delete";
	private static final String PARAM_USER = "userKey";
	private static final String PARAM_START = "startDate";
	private static final String PARAM_END = "endDate";
	private static final String PARAM_ID = "id";
	
	private List<DelegationSetting> settings = new ArrayList<>();
	private String selectedUserKey = null;
	private String selectedUserDisplayName = null;

	public String getCurrentUserKey() {
		return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getKey();
	}
	
	public String getCurrentUserDisplayName() {
		return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getDisplayName();
	}
	
	public String getSelectedUserKey() {
		return this.selectedUserKey;
	}
	
	public String getSelectedUserDisplayName() {
		return this.selectedUserDisplayName;
	}
	
	public List<DelegationSetting> getSettings() {
		LOGGER.debug("getSettings: " + this.hashCode());
		LOGGER.debug("Data: " + DelegationSetting.format(this.settings));
		return this.settings;
	}
	
	public boolean isAdmin() {
		if (getHttpRequest().getParameter(PARAM_ADMIN) != null) {
			for (String s : ADMIN_GROUPS) {
				Group g = GM.getGroup(s);
				if (GM.isUserInGroup(getLoggedInUser(), g)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public String doExecute() throws Exception {
		LOGGER.debug("doExecute(): " + this.hashCode());		
		HttpServletRequest req = getHttpRequest();

		// Load data from user property; defaults to current user
		String fromUser = req.getParameter(PARAM_FROM_USER);
		if (fromUser == null) {
			fromUser = getCurrentUserKey();
		}
		this.settings = DelegationUtil.loadData(fromUser, true);
		this.selectedUserKey = fromUser;
		this.selectedUserDisplayName = PluginUtil.getUserByKey(fromUser).getDisplayName();
		
		if (req.getParameter(PARAM_ADD) != null) {
			// Add
			String toUser = req.getParameter(PARAM_USER);
			String start = req.getParameter(PARAM_START);
			String end = req.getParameter(PARAM_END);
			DelegationSetting ds = new DelegationSetting();
			ds.setDelegateToUser(toUser);
			if (start != null && !start.isEmpty()) {
				Date startDate = DelegationSetting.SDF.parse(start);
				ds.setStartDate(startDate);
			}
			if (end != null && !end.isEmpty()) {
				Date endDate = DelegationSetting.SDF.parse(end);
				ds.setEndDate(endDate);
			}
			ds.setFromUser(fromUser);
			ds.setLastModifiedBy(getCurrentUserKey());
			ds.setLastModifiedDate(new Date());
			ds.setId(UUID.randomUUID().toString());
			DelegationSetting.translate(ds);
			this.settings.add(ds);
			// Update property set
			DelegationUtil.saveData(fromUser, this.settings);
		} else if (req.getParameter(PARAM_DELETE) != null) {
			// Delete
			String id = req.getParameter(PARAM_ID);
			LOGGER.debug("Remove: " + id);
			DelegationSetting toRemove = null;
			for (DelegationSetting ds : this.settings) {
				LOGGER.debug("vs: " + ds.getId());
				if (ds.getId().equals(id)) {
					LOGGER.debug("To remove: " + ds);
					toRemove = ds;
					break;
				}
			}
			if (toRemove != null) {
				LOGGER.debug("Remove: " + toRemove);
				this.settings.remove(toRemove);
			}
			// Update property set
			DelegationUtil.saveData(fromUser, this.settings);
		}
		
		return JiraWebActionSupport.INPUT;
	}
}
