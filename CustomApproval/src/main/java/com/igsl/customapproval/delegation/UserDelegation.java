package com.igsl.customapproval.delegation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.application.ApplicationRoleManager;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserDetails;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.UserBrowserFilter;
import com.igsl.customapproval.CustomApprovalUtil;
import com.igsl.customapproval.data.DelegationSetting;

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
	
	private static final GroupManager GM = ComponentAccessor.getGroupManager();
	private static final UserSearchService USER_SEARCH_SERVICE = ComponentAccessor.getUserSearchService();
	
	private static final String PARAM_ADMIN = "admin";
	private static final String PARAM_FROM_USER = "fromUserKey";
	private static final String PARAM_ADD = "add";
	private static final String PARAM_DELETE = "delete";
	private static final String PARAM_USER = "userKey";
	private static final String PARAM_START = "startDate";
	private static final String PARAM_START_HOUR = "startHour";
	private static final String PARAM_START_MINUTE = "startMinute";
	private static final String PARAM_END = "endDate";
	private static final String PARAM_END_HOUR = "endHour";
	private static final String PARAM_END_MINUTE = "endMinute";
	private static final String PARAM_ID = "id";
	private static final String PARAM_DEL_USER = "delUser";
	
	private List<DelegationSetting> settings = new ArrayList<>();

	public String getCurrentUserKey() {
		return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getKey();
	}
	
	public String getCurrentUserDisplayName() {
		return ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getDisplayName();
	}
	
	public List<DelegationSetting> getSettings() {
		LOGGER.debug("getSettings: " + this.hashCode());
		LOGGER.debug("Data: " + DelegationSetting.format(this.settings));
		return this.settings;
	}
	
	public boolean isAdmin() {
		List<String> groups = CustomApprovalUtil.getDelegationAdminGroups();
		if (getHttpRequest().getParameter(PARAM_ADMIN) != null && groups != null) {
			for (String s : groups) {
				Group g = GM.getGroup(s);
				if (GM.isUserInGroup(getLoggedInUser(), g)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void refreshData(boolean cleanup) {
		if (isAdmin()) {
			this.settings = new ArrayList<>();
			ApplicationRoleManager arm = ComponentAccessor.getComponent(ApplicationRoleManager.class);
			UserBrowserFilter filter = new UserBrowserFilter(Locale.getDefault(), arm);
			try {
				List<ApplicationUser> list = filter.getFilteredUsers();
				for (ApplicationUser user : list) {
					this.settings.addAll(DelegationUtil.loadData(user.getKey(), cleanup));
				}
				LOGGER.debug("User list size: " + list.size());
			} catch (Exception ex) {
				LOGGER.error("Failed to get user list", ex);
			}
		} else {
			this.settings = DelegationUtil.loadData(getLoggedInUser().getKey(), cleanup);
		}
	}
	
	@Override
	public String doExecute() throws Exception {
		LOGGER.debug("doExecute(): " + this.hashCode());		
		HttpServletRequest req = getHttpRequest();
		refreshData(true);
		if (req.getParameter(PARAM_ADD) != null) {
			// Add
			String fromUser = getLoggedInUser().getKey();
			if (isAdmin()) {
				String s = req.getParameter(PARAM_FROM_USER);
				if (s != null) {
					fromUser = s;
				}
			}
			String toUser = req.getParameter(PARAM_USER);
			String start = req.getParameter(PARAM_START);
			String startHour = req.getParameter(PARAM_START_HOUR);
			String startMinute = req.getParameter(PARAM_START_MINUTE);
			String end = req.getParameter(PARAM_END);
			String endHour = req.getParameter(PARAM_END_HOUR);
			String endMinute = req.getParameter(PARAM_END_MINUTE);
			DelegationSetting ds = new DelegationSetting();
			ds.setDelegateToUser(toUser);
			if (start != null && !start.isEmpty()) {
				String s = start + " " + ((startHour != null)? startHour : "00") + ":" + ((startMinute != null)? startMinute : "00");
				Date startDate = DelegationSetting.SDF.parse(s);
				ds.setStartDate(startDate);
			}
			if (end != null && !end.isEmpty()) {
				String s = end + " " + ((endHour != null)? endHour : "23") + ":" + ((endMinute != null)? endMinute : "59");
				Date endDate = DelegationSetting.SDF.parse(s);
				ds.setEndDate(endDate);
			}
			ds.setFromUser(fromUser);
			ds.setLastModifiedBy(getCurrentUserKey());
			ds.setLastModifiedDate(new Date());
			ds.setId(UUID.randomUUID().toString());
			DelegationSetting.translate(ds);
			this.settings.add(ds);
			// Update property set
			DelegationUtil.addData(fromUser, ds);
		} else if (req.getParameter(PARAM_DELETE) != null) {
			// Delete
			String delUser = req.getParameter(PARAM_DEL_USER);
			String id = req.getParameter(PARAM_ID);
			DelegationSetting ds = new DelegationSetting();
			ds.setId(id);
			DelegationUtil.removeData(delUser, ds);
		}		
		// Refresh
		refreshData(false);
		return JiraWebActionSupport.INPUT;
	}
}
