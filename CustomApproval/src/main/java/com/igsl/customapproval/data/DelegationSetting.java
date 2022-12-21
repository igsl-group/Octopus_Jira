package com.igsl.customapproval.data;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.user.ApplicationUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igsl.customapproval.CustomApprovalUtil;

/**
 * Delegation settings. 
 */
public class DelegationSetting {

	private static final Logger LOGGER = Logger.getLogger(DelegationSetting.class);
	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static final ObjectMapper OM = new ObjectMapper();
	private static final String DATE_NO_LIMIT = "Never Expires";
	
	@Override
	public String toString() {
		try {
			return OM.writeValueAsString(this);
		} catch (Exception ex) {
			LOGGER.error("Failed to serialize DelegationSetting", ex);
		}
		return null;
	}
	
	public static String format(List<DelegationSetting> list) {
		try {
			return OM.writeValueAsString(list);
		} catch (Exception ex) {
			LOGGER.error("Failed to serialize DelegationSetting", ex);
		}
		return null;
	}
	
	public static void translate(DelegationSetting ds) {
		if (ds != null) {
			ds.setDelegateToUserObject(CustomApprovalUtil.getUserByKey(ds.getDelegateToUser()));
			ds.setFromUserObject(CustomApprovalUtil.getUserByKey(ds.getFromUser()));
			ds.setLastModifiedByObject(CustomApprovalUtil.getUserByKey(ds.getLastModifiedBy()));
			ds.setStartDateString(
					(ds.getStartDate() == null)? DATE_NO_LIMIT : SDF.format(ds.getStartDate()));
			ds.setEndDateString(
					(ds.getEndDate() == null)? DATE_NO_LIMIT : SDF.format(ds.getEndDate()));
			ds.setLastModifiedDateString(
					(ds.getLastModifiedDate() == null)? DATE_NO_LIMIT : SDF.format(ds.getLastModifiedDate()));
		}
	}
	
	public static List<DelegationSetting> parse(String s) {
		try {
			// Fetch ApplicationUser objects
			List<DelegationSetting> list = OM.readValue(s, new TypeReference<List<DelegationSetting>>() {});
			for (DelegationSetting item : list) {
				translate(item);
			}
			return list;
		} catch (Exception ex) {
			LOGGER.error("Failed to deserialize DelegationSetting", ex);
		}
		return Collections.emptyList();
	}
	
	@JsonIgnore
	private ApplicationUser fromUserObject;
	@JsonIgnore
	private ApplicationUser delegateToUserObject;
	@JsonIgnore
	private String startDateString;
	@JsonIgnore
	private String endDateString;
	@JsonIgnore
	private ApplicationUser lastModifiedByObject;
	@JsonIgnore
	private String lastModifiedDateString;
	
	private String id;
	private String fromUser;
	private String delegateToUser;
	private Date startDate;
	private Date endDate;
	private String lastModifiedBy;
	private Date lastModifiedDate;
	
	public String getFromUser() {
		return fromUser;
	}
	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}
	public String getDelegateToUser() {
		return delegateToUser;
	}
	public void setDelegateToUser(String delegateToUser) {
		this.delegateToUser = delegateToUser;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public ApplicationUser getFromUserObject() {
		return fromUserObject;
	}
	public void setFromUserObject(ApplicationUser fromUserObject) {
		this.fromUserObject = fromUserObject;
	}
	public ApplicationUser getDelegateToUserObject() {
		return delegateToUserObject;
	}
	public void setDelegateToUserObject(ApplicationUser delegateToUserObject) {
		this.delegateToUserObject = delegateToUserObject;
	}
	public String getStartDateString() {
		return startDateString;
	}
	public void setStartDateString(String startDateString) {
		this.startDateString = startDateString;
	}
	public String getEndDateString() {
		return endDateString;
	}
	public void setEndDateString(String endDateString) {
		this.endDateString = endDateString;
	}
	public ApplicationUser getLastModifiedByObject() {
		return lastModifiedByObject;
	}
	public void setLastModifiedByObject(ApplicationUser lastModifiedByObject) {
		this.lastModifiedByObject = lastModifiedByObject;
	}
	public String getLastModifiedDateString() {
		return lastModifiedDateString;
	}
	public void setLastModifiedDateString(String lastModifiedDateString) {
		this.lastModifiedDateString = lastModifiedDateString;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
