package com.igsl.customapproval.delegation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.igsl.customapproval.CustomApprovalSetup;
import com.igsl.customapproval.CustomApprovalUtil;
import com.igsl.customapproval.data.DelegationSetting;
import com.opensymphony.module.propertyset.PropertySet;

public class DelegationUtil {
	
	private static final Logger LOGGER = Logger.getLogger(DelegationUtil.class);
	private static final UserPropertyManager UPM = ComponentAccessor.getUserPropertyManager();

	private static final String PROPERTY_DELEGATION = "customApprovalDelegation";

	public static final String DELEGATE_FROM_USER = "From";
	public static final String DELEGATE_ADDED = "Added";
	public static final String DELEGATE_REMOVED = "Removed";
	
	/**
	 * Calculate no. of days in d1 - d2.
	 * @param d1
	 * @param d2
	 * @return long
	 */
	private static long dateDiff(Date d1, Date d2) {
		long d1v = d1.getTime();
		long d2v = d2.getTime();
		long diff = d1.getTime() - d2.getTime();
	    return diff;
	}
	
	private static PropertySet getPropertySet(String userKey) {
		PropertySet ps = UPM.getPropertySetForUserKey(userKey);
		return ps;
	}
	
	/**
	 * Check if user is delegate of delegating user. 
	 * @param user User to check
	 * @param delegatingUser Delegating user
	 * @param approvalDate Approval date. If null, defaults to today
	 * @return boolean
	 */
	public static boolean isDelegate(ApplicationUser user, ApplicationUser delegatingUser, Date approvalDate) {
		return isDelegate(user.getKey(), delegatingUser.getKey(), approvalDate);
	}
	
	public static boolean isDelegate(String user, String delegatingUser, Date approvalDate) {
		LOGGER.debug("isDelegate: " + user + " of " + delegatingUser + "?");
		List<DelegationSetting> list = loadData(delegatingUser, false);
		if (approvalDate == null) {
			approvalDate = new Date();
		}
		for (DelegationSetting ds : list) {
			LOGGER.debug("isDelegate item: " + ds);
			if (ds.getDelegateToUser().equals(user)) {
				// Check start/end date
				LOGGER.debug("isDelegate approval date: " + approvalDate);
				LOGGER.debug("isDelegate start: " + ds.getStartDate());
				LOGGER.debug("isDelegate end: " + ds.getEndDate());
				if (ds.getEndDate() == null) {
					if (dateDiff(ds.getStartDate(), approvalDate) <= 0) {
						LOGGER.debug("isDelegate: " + user + " of " + delegatingUser + " = true");
						return true;
					}
				} else {
					if (dateDiff(ds.getStartDate(), approvalDate) <= 0 && 
						dateDiff(ds.getEndDate(), approvalDate) > 0) {
						LOGGER.debug("isDelegate: " + user + " of " + delegatingUser + " = true");
						return true;
					}
				}
				LOGGER.debug("isDelegate item: date range not match");
			}
		}
		LOGGER.debug("isDelegate: " + user + " of " + delegatingUser + " = false");
		return false;
	}
	
	public static void removeData(String userKey, DelegationSetting setting) {
		List<DelegationSetting> list = loadData(userKey, false);
		DelegationSetting remove = null;
		for (DelegationSetting ds : list) {
			if (ds.getId().equals(setting.getId())) {
				remove = ds;
				break;
			}
		}
		if (remove != null) {
			list.remove(remove);
			saveData(userKey, list, remove, false);
		}
	}
	
	public static void addData(String userKey, DelegationSetting setting) {
		List<DelegationSetting> list = loadData(userKey, false);
		list.add(setting);
		saveData(userKey, list, setting, true);
	}
	
	public static void saveData(String userKey, List<DelegationSetting> list, DelegationSetting delta, boolean added) {
		String data = DelegationSetting.format(list);
		LOGGER.debug("Saving property: " + data);
		PropertySet ps = getPropertySet(userKey);
		ps.setText(PROPERTY_DELEGATION, data);
		// Get event type id
		Long eventTypeId = CustomApprovalSetup.getCustomEventType();
		if (eventTypeId != null) {
			// Find a random issue to use
			ProjectManager pm = ComponentAccessor.getProjectManager();
			IssueManager im = ComponentAccessor.getIssueManager();
			MutableIssue randomIssue = null;
			for (Project p : pm.getProjects()) {
				try {
					Collection<Long> issueIds = im.getIssueIdsForProject(p.getId());
					for (Long issueId : issueIds) {
						randomIssue = im.getIssueObject(issueId);
						if (randomIssue != null) {
							break;
						}
					}
				} catch (GenericEntityException e) {
					// Ignored
				}
			}
			if (randomIssue != null) {
				ApplicationUser admin = CustomApprovalUtil.getAdminUser();
				Map<String, Object> map = new HashMap<>();
				map.put(DELEGATE_FROM_USER, delta.getFromUser());
				if (added) {
					map.put(DELEGATE_ADDED, delta.getDelegateToUser());
				} else {
					map.put(DELEGATE_REMOVED, delta.getDelegateToUser());
				}
				// Create issue event bundle
				IssueEventBundleFactory bundleFactory = ComponentAccessor.getComponent(IssueEventBundleFactory.class);
				IssueEventBundle bundle = bundleFactory.createWorkflowEventBundle(eventTypeId, randomIssue, admin, null, null, map, false, null);
				// Raise event
				IssueEventManager iem = ComponentAccessor.getIssueEventManager();
				iem.dispatchEvent(bundle);
				LOGGER.debug("Event triggered");
			} else {
				LOGGER.error("No issue found for use with event");
			}
		} else {
			LOGGER.debug("Event type not found");
		}
	}
	
	/**
	 * Load delegation data that are effective for provided date
	 * @param userKey Delegator user key
	 * @param checkDate Date to check against. If null, all records are included.
	 * @return List<DelegationSetting>
	 */
	public static List<DelegationSetting> loadData(String userKey, Date checkDate) {
		List<DelegationSetting> result = new ArrayList<>();
		PropertySet ps = getPropertySet(userKey);
		String s = ps.getText(PROPERTY_DELEGATION);
		LOGGER.debug("Loaded property: " + s);
		if (s != null) {
			List<DelegationSetting> list = DelegationSetting.parse(s);
			for (DelegationSetting setting : list) {
				if (checkDate != null) {
					Date start = setting.getStartDate();
					Date end = setting.getEndDate();
					// Compare start date
					if (start.after(checkDate)) {
						continue;
					}
					if (end != null) {
						// Compare end date
						if (end.before(checkDate)) {
							continue;
						}
					}
				}
				result.add(setting);
			}
		}
		return result;
	}
	
	/**
	 * Load delegation data
	 * @param userKey Delegator user key
	 * @param removeOldRecords Remove records already expired from database
	 * @return List<DelegationSetting>
	 */
	public static List<DelegationSetting> loadData(String userKey, boolean removeOldRecords) {
		List<DelegationSetting> result = new ArrayList<>();
		long threshold = CustomApprovalUtil.getDelegationHistoryRetainDays() * 1000 * 60 * 60 * 24;
		PropertySet ps = getPropertySet(userKey);
		String s = ps.getText(PROPERTY_DELEGATION);
		LOGGER.debug("Loaded property: " + s);
		if (s != null) {
			Date today = new Date();
			List<DelegationSetting> list = DelegationSetting.parse(s);
			for (DelegationSetting setting : list) {
				if (removeOldRecords) {
					// Check if end date already passed threshold
					Date end = setting.getEndDate();
					if (end != null) {
						long diff = dateDiff(end, today);
						LOGGER.debug("End date: " + end + ", diff: " + diff);
						if (diff < 0 && Math.abs(diff) > threshold) {
							LOGGER.debug("Record outside threshold, removing: " + setting);
						} else {
							LOGGER.debug("Record within threshold, adding: " + setting);
							result.add(setting);
						}
					} else {
						// No end date, add
						LOGGER.debug("Adding non-expiring: " + setting);
						result.add(setting);
					}
				} else {
					LOGGER.debug("Adding no end date check: " + setting);
					result.add(setting);
				}
			}
			if (removeOldRecords) {
				String out = DelegationSetting.format(result);
				LOGGER.debug("Removed old records: " + out);
				ps.setText(PROPERTY_DELEGATION, out);
			}
		}
		return result;
	}
}
