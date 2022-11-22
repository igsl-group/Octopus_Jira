package com.igsl.configmigration;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.startup.PluginInfoProvider;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.internal.LinkedTreeMap;

@Named
public class ExportAction extends JiraWebActionSupport {
	
	private static final long serialVersionUID = 1L;
	
	// Custom field configuration URL
	private static final String PAGE_URL = "/secure/admin/plugins/handler/ExportAction.jspa";
	
	// Form constants
	private static final String FORM_SECTION = "section";
	private static final String FORM_ACTION = "action";
	private static final String FORM_SELECT = "select";
	
	private static final String FORM_ACTION_EXPORT = "export";
	private static final String FORM_ACTION_SEARCH = "search";
	private static final String FORM_ACTION_CLEAR = "clear";
	
	private static final ObjectMapper OM = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	private static Logger LOGGER = LoggerFactory.getLogger(ExportAction.class);
	
	private static final String SESSION_DATA = "exportData";
	
	private Map<String, SessionData> sessionData = null;
	public Map<String, SessionData> getSessionData() {
		return sessionData;
	}
	
	private String parsedData;
	public String getParsedData() {
		return parsedData;
	}
	
	private String compareResult;
	public String getCompareResult() {
		return compareResult;
	}
	
	private String mergeResult;
	public String getMergeResult() {
		return mergeResult;
	}
	
	private String issueTypeData;
	public String getIssueTypeData() {
		return issueTypeData;
	}
	
	private String debug;
	public String getDebug() {
		return debug;
	}
	
	private String exportData;
	public String getExportData() {
		return exportData;
	}
	
	private void initSessionData() throws Exception {
		sessionData = new LinkedTreeMap<>();
		for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList()) {
			SessionData sd = new SessionData(util);
			sessionData.put(util.getClass().getCanonicalName(), sd);
		}
		this.getHttpSession().setAttribute(SESSION_DATA, sessionData);
	}
	
	@SuppressWarnings("unchecked")
	private void readSessionData() {
		sessionData = (Map<String, SessionData>) this.getHttpSession().getAttribute(SESSION_DATA);
	}
	
	@Override
	protected void doValidation() {
	}
	
	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");

		this.debug = "";		
		HttpServletRequest req = this.getHttpRequest();
		String section = req.getParameter(FORM_SECTION);
		String action = req.getParameter(FORM_ACTION);
		
		readSessionData();
		
		if (action == null || sessionData == null) {
			initSessionData();
			
			// TODO Load all sections
			for (Map.Entry<String, SessionData> entry : sessionData.entrySet()) {
				SessionData data = sessionData.get(entry.getKey());
				data.getExportData().putAll(data.getUtil().readAllItems());
			}
		}
		
		// Save selection
		for (Map.Entry<String, SessionData> entry : sessionData.entrySet()) {
			SessionData data = sessionData.get(entry.getKey());
			for (Map.Entry<String, JiraConfigItem> item : data.getExportData().entrySet()) { 
				item.getValue().setSelected(false);
			}
			int count = 0;
			String[] selected = req.getParameterValues(entry.getKey() + "." + FORM_SELECT);
			if (selected != null) {
				for (String item : selected) {
					data.getExportData().get(item).setSelected(true);
					count++;
				}
			}
		}
		
		// TODO Separate selected items and search result
		// TODO Allow search parameters
		
		if (FORM_ACTION_SEARCH.equals(action)) {
			// Search all objects
			if (section != null && !section.isEmpty()) {
				SessionData data = sessionData.get(section);
				data.getExportData().putAll(data.getUtil().readAllItems());
			} else {
				for (Map.Entry<String, SessionData> entry : sessionData.entrySet()) {
					SessionData data = sessionData.get(entry.getKey());
					data.getExportData().putAll(data.getUtil().readAllItems());
				}
			}
		} else if (FORM_ACTION_CLEAR.equals(action)) {
			// Remove search results
			if (section != null && !section.isEmpty()) {
				SessionData data = sessionData.get(section);
				data.getExportData().clear();
			} else {
				for (Map.Entry<String, SessionData> entry : sessionData.entrySet()) {
					SessionData data = sessionData.get(entry.getKey());
					data.getExportData().clear();
				}
			}
		} else if (FORM_ACTION_EXPORT.equals(action)) {
			// Export
			Map<String, SessionData> export = new HashMap<String, SessionData>();
			for (Map.Entry<String, SessionData> entry : sessionData.entrySet()) {
				SessionData data = sessionData.get(entry.getKey());
				if (!data.getExportData().isEmpty()) {
					SessionData clone = new SessionData(data.getUtil());
					for (Map.Entry<String, JiraConfigItem> item : data.getExportData().entrySet()) {
						if (item.getValue().isSelected()) {
							clone.getExportData().put(item.getKey(), item.getValue());
						}
					}
					if (!clone.getExportData().isEmpty()) {
						export.put(entry.getKey(), clone);
					}
				}
			}
			if (export.size() != 0) {
				exportData = OM.writeValueAsString(export);
				this.debug += "Export: " + exportData + "\n";
//				Map<String, SessionData> test = OM.readValue(exportData, new TypeReference<Map<String, SessionData>>() {});
//				this.debug += "Deserialize Test: " + OM.writeValueAsString(test) + "\n";
			} else {
				this.debug += "Nothing to export\n";
			}
		}

		// Return to form
    	return JiraWebActionSupport.INPUT;
	}
}
