package com.igsl.customapproval.delegation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.igsl.customapproval.data.DelegationSetting;
import com.opensymphony.module.propertyset.PropertySet;

public class DelegationUtil {
	
	private static final long HISTORY_THRESHOLD_DAYS = 365;	// TOOD Move to config page? properties?
	
	private static final Logger LOGGER = Logger.getLogger(DelegationUtil.class);
	private static final UserPropertyManager UPM = ComponentAccessor.getUserPropertyManager();

	private static final String PROPERTY_DELEGATION = "customApprovalDelegation";

	private static long dateDiff(Date d1, Date d2) {
		long diffInMillies = Math.abs(d2.getTime() - d1.getTime());
	    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
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
					if (dateDiff(ds.getStartDate(), approvalDate) >= 0) {
						LOGGER.debug("isDelegate: " + user + " of " + delegatingUser + " = true");
						return true;
					}
				} else {
					if (dateDiff(ds.getStartDate(), approvalDate) >= 0 && 
						dateDiff(ds.getEndDate(), approvalDate) < 0) {
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
	
	public static void saveData(String userKey, List<DelegationSetting> list) {
		String data = DelegationSetting.format(list);
		LOGGER.debug("Saving property: " + data);
		PropertySet ps = getPropertySet(userKey);
		ps.setText(PROPERTY_DELEGATION, data);
	}
	
	public static List<DelegationSetting> loadData(String userKey, boolean removeOldRecords) {
		List<DelegationSetting> result = new ArrayList<>();
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
						long diff = dateDiff(today, end);
						LOGGER.debug("End date: " + end + ", diff: " + diff);
						if (diff <= HISTORY_THRESHOLD_DAYS) {
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
		}
		return result;
	}
}
