package com.igsl.customapproval.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom field content to store:
 * - Approval settings
 * - Approval history
 *
 * Allows multiple approval steps.
 */
public class ApprovalData {
	
	private static final ObjectMapper OM = new ObjectMapper();
	
	/**
	 * Key is name of an approval process.
	 */
	private Map<String, ApprovalSettings> settings = new HashMap<>();
	
	/**
	 * Key is name of an approval process.
	 */
	private Map<String, List<ApprovalHistory>> history = new HashMap<>();

	public static ApprovalData parse(String s) {
		try {
			return OM.readValue(s, ApprovalData.class);
		} catch (Exception ex) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		try {
			return OM.writeValueAsString(this);
		} catch (Exception ex) {
			return null;
		}
	}
	
	public Map<String, ApprovalSettings> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, ApprovalSettings> settings) {
		this.settings = settings;
	}

	public Map<String, List<ApprovalHistory>> getHistory() {
		return history;
	}

	public void setHistory(Map<String, List<ApprovalHistory>> history) {
		this.history = history;
	}
	
}
