package com.igsl.configmigration;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * Store a JiraConfigUtil associated with its data (and selection status)
 */
public class SessionData {
	
	private static final Logger LOGGER = Logger.getLogger(SessionData.class);
	
	public static class ImportData {
		private JiraConfigDTO server;
		private JiraConfigDTO data;
		private boolean identical;
		private List<String> differences;
		private String importResult;
		private void checkIdentical() {
			differences = JiraConfigDTO.getDifferences("", this.server, this.data);
			identical = (differences.size() == 0);
		}
		public boolean isKeyDifferent(String key) {
			boolean result = false;
			for (String s : this.differences) {
				if (s.endsWith(JiraConfigDTO.DIFFERENCE_INDEX)) {
					if (key.startsWith(s.substring(0, s.length() - JiraConfigDTO.DIFFERENCE_INDEX.length()))) {
						return true;
					}
				} else if (s.endsWith(JiraConfigDTO.DIFFERENCE_WILDCARD)) {
					if (key.startsWith(s.substring(0, s.length() - JiraConfigDTO.DIFFERENCE_WILDCARD.length()))) {
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
		public JiraConfigDTO getServer() {
			return server;
		}
		public void setServer(JiraConfigDTO server) {
			this.server = server;
			checkIdentical();
		}
		public JiraConfigDTO getData() {
			return data;
		}
		public void setData(JiraConfigDTO data) {
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
	private Map<String, JiraConfigDTO> exportData = new TreeMap<>();
	private Map<String, ImportData> importData = new TreeMap<>();
	
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
	
	public Map<String, JiraConfigDTO> getExportData() {
		return exportData;
	}
	
	public Map<String, ImportData> getImportData() {
		return importData;
	}

}
