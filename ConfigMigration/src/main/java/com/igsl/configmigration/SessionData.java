package com.igsl.configmigration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Store a JiraConfigUtil associated with its data (and selection status)
 */
public class SessionData {
	
	private static final Logger LOGGER = Logger.getLogger(SessionData.class);
	
	public static class ImportData {
		private JiraConfigItem server;
		private JiraConfigItem data;
		private boolean identical;
		private List<String> differences;
		private String importResult;
		private void checkIdentical() {
			differences = JiraConfigItem.getDifferences("", this.server, this.data);
			identical = (differences.size() == 0);
		}
		public boolean isKeyDifferent(String key) {
			boolean result = false;
			for (String s : this.differences) {
				if (s.endsWith(JiraConfigItem.DIFFERENCE_INDEX)) {
					if (key.startsWith(s.substring(0, s.length() - JiraConfigItem.DIFFERENCE_INDEX.length()))) {
						return true;
					}
				} else if (s.endsWith(JiraConfigItem.DIFFERENCE_WILDCARD)) {
					if (key.startsWith(s.substring(0, s.length() - JiraConfigItem.DIFFERENCE_WILDCARD.length()))) {
						return true;
					}
				} else {
					if (key.equals(s)) {
						return true;
					}
				}
			}
			return result;
		}
		public boolean isIdentical() {
			return identical;
		}
		public JiraConfigItem getServer() {
			return server;
		}
		public void setServer(JiraConfigItem server) {
			this.server = server;
			checkIdentical();
		}
		public JiraConfigItem getData() {
			return data;
		}
		public void setData(JiraConfigItem data) {
			this.data = data;
			checkIdentical();
		}
		public void setImportResult(String importResult) {
			this.importResult = importResult;
		}
		public String getImportResult() {
			return importResult;
		}
		public List<String> getDifferences() {
			return differences;
		}
	}
	
	private JiraConfigUtil util;
	private Map<String, JiraConfigItem> exportData = new HashMap<>();
	private Map<String, ImportData> importData = new HashMap<>();
	
	public SessionData() {
	}
	
	public SessionData(JiraConfigUtil util) {
		this.util = util;
	}

	public void setUtil(JiraConfigUtil util) {
		this.util = util;
	}
	
	public JiraConfigUtil getUtil() {
		return util;
	}
	
	public Map<String, JiraConfigItem> getExportData() {
		return exportData;
	}
	
	public Map<String, ImportData> getImportData() {
		return importData;
	}

}
