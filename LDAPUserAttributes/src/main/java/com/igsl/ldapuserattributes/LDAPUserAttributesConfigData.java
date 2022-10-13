package com.igsl.ldapuserattributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LDAPUserAttributesConfigData {
	private List<String> lastTestResults = new ArrayList<String>();
	private boolean ignoreExpiredUser = true;
	private String expiresAttribute = "accountExpires";
	private boolean referral = false;
	private String providerURL;
	private String baseDN;
	private String hour = "00";
	private String minute = "00";
	private String second = "00";
	private int scope = 2;
	private int pageSize = 1000;
	private long frequency = 0L;
	private long frequencyMultiplier = 86400000L;
	private String filter;
	private String userNameAttribute = "sAMAccountName";
	private String userName;
	private String encryptedPassword;
	private Map<String, String> attributeMap = new HashMap<String, String>();
	// Generated
	public String getProviderURL() {
		return providerURL;
	}
	public void setProviderURL(String providerURL) {
		this.providerURL = providerURL;
	}
	public String getBaseDN() {
		return baseDN;
	}
	public void setBaseDN(String baseDN) {
		this.baseDN = baseDN;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getEncryptedPassword() {
		return encryptedPassword;
	}
	public void setEncryptedPassword(String encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
	}
	public Map<String, String> getAttributeMap() {
		return attributeMap;
	}
	public void setAttributeMap(Map<String, String> attributeMap) {
		this.attributeMap = attributeMap;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public int getScope() {
		return scope;
	}
	public void setScope(int scope) {
		this.scope = scope;
	}
	public long getFrequency() {
		return frequency;
	}
	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}
	public String getUserNameAttribute() {
		return userNameAttribute;
	}
	public void setUserNameAttribute(String userNameAttribute) {
		this.userNameAttribute = userNameAttribute;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public long getFrequencyMultiplier() {
		return frequencyMultiplier;
	}
	public void setFrequencyMultiplier(long frequencyMultiplier) {
		this.frequencyMultiplier = frequencyMultiplier;
	}
	public String getHour() {
		return hour;
	}
	public void setHour(String hour) {
		this.hour = hour;
	}
	public String getMinute() {
		return minute;
	}
	public void setMinute(String minute) {
		this.minute = minute;
	}
	public String getSecond() {
		return second;
	}
	public void setSecond(String second) {
		this.second = second;
	}
	public List<String> getLastTestResults() {
		return lastTestResults;
	}
	public void setLastTestResults(List<String> lastTestResults) {
		this.lastTestResults = lastTestResults;
	}
	public boolean isReferral() {
		return referral;
	}
	public void setReferral(boolean referral) {
		this.referral = referral;
	}
	public String getExpiresAttribute() {
		return expiresAttribute;
	}
	public void setExpiresAttribute(String expiresAttribute) {
		this.expiresAttribute = expiresAttribute;
	}
	public boolean isIgnoreExpiredUser() {
		return ignoreExpiredUser;
	}
	public void setIgnoreExpiredUser(boolean ignoreExpiredUser) {
		this.ignoreExpiredUser = ignoreExpiredUser;
	}
}
