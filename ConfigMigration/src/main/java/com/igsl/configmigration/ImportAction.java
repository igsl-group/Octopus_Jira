package com.igsl.configmigration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.internal.LinkedTreeMap;
import com.igsl.configmigration.SessionData.ImportData;
import com.igsl.configmigration.licensedapplication.LicensedApplicationDTO;
import com.igsl.configmigration.licensedapplication.LicensedApplicationUtil;
import com.igsl.configmigration.options.OptionDTO;
import com.igsl.configmigration.priority.PriorityUtil;
import com.igsl.configmigration.resolution.ResolutionUtil;

import webwork.action.ServletActionContext;
import webwork.multipart.MultiPartRequestWrapper;

@Named
public class ImportAction extends JiraWebActionSupport {
	
	private static final long serialVersionUID = 1L;
	
	// Custom field configuration URL
	private static final String PAGE_URL = "/secure/admin/plugins/handler/ImportAction.jspa";
	
	// Form constants
	private static final String FORM_SECTION = "section";
	private static final String FORM_ACTION = "action";
	private static final String FORM_SELECT = "select";
	private static final String FORM_FILE = "file";
	
	private static final String FORM_ACTION_IMPORT = "import";
	private static final String FORM_ACTION_CLEAR = "clear";
	
	private static final ObjectMapper OM = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	private static Logger LOGGER = LoggerFactory.getLogger(ImportAction.class);
	
	private static final String SESSION_DATA = "importData";
	
	private Map<String, SessionData> sessionData = null;
	public Map<String, SessionData> getSessionData() {
		return sessionData;
	}
	
	private String debug;
	public String getDebug() {
		return debug;
	}
	
	private String warning;
	public String getWarning() {
		return warning;
	}
	
	private void initSessionData() throws Exception {
		sessionData = new LinkedTreeMap<>();
		for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(true)) {
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
	
	private HttpServletRequest req = null;
	private MultiPartRequestWrapper wrapper = null;
	private HttpServletRequest getRequest() {
		this.req = this.getHttpRequest();
		HttpServletRequest wrapperReq = ServletActionContext.getRequest();
		if (wrapperReq instanceof MultiPartRequestWrapper) {	
			this.wrapper = (MultiPartRequestWrapper) wrapperReq;
			this.debug += "Multipart request found\n";
		}
		this.debug += "HTTP request found\n";
		return this.req;
	}
	
	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		this.debug = "";
		HttpServletRequest req = getRequest();
		String section = req.getParameter(FORM_SECTION);
		String action = req.getParameter(FORM_ACTION);
		this.debug += "Section: " + section + "\n";
		this.debug += "Action: " + action + "\n";
		
		readSessionData();
		if (action == null || sessionData == null) {
			initSessionData();
		}
		
		// Save selection
		for (Map.Entry<String, SessionData> entry : sessionData.entrySet()) {
			SessionData data = sessionData.get(entry.getKey());
			String[] selected = req.getParameterValues(entry.getKey() + "." + FORM_SELECT);
			int count = 0;
			if (selected != null) {
				for (String item : selected) {
					data.getImportData().get(item).getData().setSelected(true);
					count++;
				}
			}
		}
		
		if (wrapper != null) {
			// Parse uploaded file
			File uploaded = wrapper.getFile(FORM_FILE);
			Map<String, SessionData> imported = OM.readValue(uploaded, new TypeReference<Map<String, SessionData>>() {});
			// Store in importData.data
			for (Map.Entry<String, SessionData> entry : imported.entrySet()) {
				SessionData sd = this.sessionData.get(entry.getKey());
				if (sd != null) {
					for (Map.Entry<String, JiraConfigDTO> item : entry.getValue().getExportData().entrySet()) {
						ImportData pair = new ImportData();
						pair.setData(item.getValue());
						sd.getImportData().put(item.getKey(), pair);
					}
				} else {
					this.debug += "Util " + entry.getKey() + " not found, data ignored.\n";
				}
			}
			// Retrieve server data and match against importData.data using key
			for (Map.Entry<String, SessionData> entry : imported.entrySet()) {
				SessionData sd = this.sessionData.get(entry.getKey());
				if (sd != null) {
					Map<String, JiraConfigDTO> serverDataList = sd.getUtil().search(null, null);
					for (Map.Entry<String, ImportData> item : sd.getImportData().entrySet()) {
						String itemKey = item.getValue().getData().getUniqueKey();
						this.debug += "Looking for " + itemKey + "\n";
						if (serverDataList.containsKey(itemKey)) {
							this.debug += "Found " + itemKey + " in server data\n";
							this.debug += "Server data: " + serverDataList.get(itemKey).getUniqueKey() + "\n";
							// Matching server item found, store in importData.server
							item.getValue().setServer(serverDataList.get(itemKey));
						} else {
							this.debug += "Server data not found\n";
							this.debug += "Server data: " + item.getValue().getServer() + "\n";
							item.getValue().setServer(null);
						}
					}
				}
			}
			// Compare applications
			SessionData apps = imported.get(LicensedApplicationUtil.class.getCanonicalName());
			Set<String> importKeys = apps.getExportData().keySet();
			LicensedApplicationUtil appUtil = (LicensedApplicationUtil) 
					JiraConfigTypeRegistry.getConfigUtil(LicensedApplicationUtil.class);
			Set<String> currentKeys = new HashSet<>();
			for (JiraConfigDTO item : appUtil.search(null).values()) {
				LicensedApplicationDTO dto = (LicensedApplicationDTO) item;
				currentKeys.add(dto.getValue());
			}
			LOGGER.debug("importKeys: " + importKeys);
			LOGGER.debug("currentKeys: " + currentKeys);
			if (!currentKeys.containsAll(importKeys)) {
				// Warning message
				this.warning = "Missing applications detected. Please ensure applications are synchronized before importing.";
			} else {
				this.warning = "";
			}
			
			this.debug += "Data: " + OM.writeValueAsString(this.sessionData) + "\n";
		} else {
			if (FORM_ACTION_CLEAR.equals(action)) {
				for (Map.Entry<String, SessionData> entry : sessionData.entrySet()) {
					SessionData data = entry.getValue();
					data.getExportData().clear();
					data.getImportData().clear();
				}
				this.debug += "Data cleared\n";
			} else if (FORM_ACTION_IMPORT.equals(action)) {
				for (Map.Entry<String, SessionData> entry : sessionData.entrySet()) {
					SessionData data = entry.getValue();
					JiraConfigUtil util = data.getUtil();
					Map<String, ImportData> itemList = new HashMap<>();
					List<JiraConfigDTO> listToSort = new ArrayList<>();
					for (Map.Entry<String, ImportData> item : data.getImportData().entrySet()) {
						if (item.getValue().getData().isSelected()) {
							itemList.put(item.getKey(), item.getValue());
							listToSort.add(item.getValue().getData());
							LOGGER.debug("Sort item: " + item.getValue().getData());
						}
					}
					if (itemList.size() != 0) {
						this.debug += util.getName() + " importing: " + OM.writeValueAsString(itemList) + "\n";
						try {
							util.merge(null, null, itemList);
						} catch (Exception ex) {
							this.debug += JiraConfigUtil.printException(ex);
						}
					} else {
						this.debug += "Nothing to import for " + util.getName() + "\n";
					}
					if (util.isPostSequenced()) {
						LOGGER.debug("Update sequence");
						util.updateSequence(listToSort);
						// Refresh export data
						data.getExportData().clear();
						for (Map.Entry<String, ImportData> item : data.getImportData().entrySet()) {
							if (item.getValue().getData().isSelected()) {
								JiraConfigDTO updateItem = util.findByDTO(item.getValue().getData());
								item.getValue().setServer(updateItem);
							}
						}
					}
				}
			}
		}
		
		// Return to form
    	return JiraWebActionSupport.INPUT;
	}
}
