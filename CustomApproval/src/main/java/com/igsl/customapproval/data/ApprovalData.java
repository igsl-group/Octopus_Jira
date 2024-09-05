package com.igsl.customapproval.data;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Custom field content to store:
 * - Approval settings
 * - Approval history
 *
 * Allows multiple approval steps.
 */
public class ApprovalData {
	private static final Logger LOGGER = Logger.getLogger(ApprovalData.class);
	private static final ObjectMapper OM = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	/**
	 * Key is name of an approval process.
	 */
	private Map<String, ApprovalSettings> settings = new LinkedHashMap<>();
	
	/**
	 * Top level key is name of an approval process.
	 * Value map key is user key.
	 */
	private Map<String, Map<String, ApprovalHistory>> history = new LinkedHashMap<>();

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
			LOGGER.error("Unable to convert ApprovalData to string", ex);
			return null;
		}
	}
	
	public Map<String, ApprovalSettings> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, ApprovalSettings> settings) {
		this.settings = settings;
	}

	public Map<String, Map<String, ApprovalHistory>> getHistory() {
		return history;
	}

	public void setHistory(Map<String, Map<String, ApprovalHistory>> history) {
		this.history = history;
	}
	
}
