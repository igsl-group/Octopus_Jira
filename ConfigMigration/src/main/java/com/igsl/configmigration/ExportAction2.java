package com.igsl.configmigration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.igsl.configmigration.status.StatusUtil;

public class ExportAction2 extends JiraWebActionSupport {

	// Selection data class.
	// UI will pass an array of SelectionData for updated items
	public static class SelectionData {
		public String utilName;
		public String uniqueKey;
		public boolean selected;
	}
	
	// Data stored in HTTP session
	public static class SessionData {
		public DTOStore exportStore = new DTOStore(true);
		public DTOStore importStore = new DTOStore(false);
		public String objectType = StatusUtil.class.getCanonicalName();
		public String exportFilter = "";
		public String importFilter = "";
		public StringBuilder errorMessage = new StringBuilder();
		public Map<String, String> view;
		public String viewName;
		public String viewType;
	}
	
	private static final long serialVersionUID = 1L;
	
	// Custom field configuration URL
	private static final String PAGE_URL = "/secure/admin/plugins/handler/ExportAction2.jspa";
	private static final String NEWLINE = "\r\n";
	
	// Session variable
	private static final String SESSION_DATA = "ExportAction2SessionData";
	
	// Actions
	private static final String ACTION_OBJECT_TYPE = "objectType";
	private static final String ACTION_EXPORT_FILTER = "exportFilter";
	private static final String ACTION_IMPORT_FILTER = "importFilter";
	private static final String ACTION_IMPORT_VIEW = "viewImport";
	private static final String ACTION_EXPORT_VIEW = "viewExport";
	private static final String ACTION_EXPORT = "export";
	
	private static final String ACTION_RELOAD = "reload";
	private static final String ACTION_IMPORT = "import";
	private static final String ACTION_UPLOAD = "upload";
	
	// Form data
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_OBJECT_TYPE = "objectType";
	private static final String PARAM_EXPORT_FILTER = "exportFilter";
	private static final String PARAM_SELECTED_IMPORTS = "selectedImports";
	private static final String PARAM_SELECTED_EXPORTS = "selectedExports";
	private static final String PARAM_VIEW_IMPORT = "viewImport";
	private static final String PARAM_VIEW_EXPORT = "viewExport";	
	
	private static Logger LOGGER = LoggerFactory.getLogger(ExportAction2.class);
	private static ObjectMapper OM = new ObjectMapper();
	private static ObjectReader OR = OM.readerFor(SelectionData.class);
	
	private SessionData data = new SessionData();
	
	public Map<String, String> getView() {
		return this.data.view;
	}
	
	public String getViewName() {
		return this.data.viewName;
	}
	
	public String getViewType() {
		return this.data.viewType;
	}
	
	public DTOStore getExportStore() {
		return this.data.exportStore;
	}
	
	public DTOStore getImportStore() {
		return this.data.importStore;
	}
	
	public String getObjectType() {
		return this.data.objectType;
	}
	
	public String getImportFilter() {
		return this.data.importFilter;
	}
	
	public String getExportFilter() {
		return this.data.exportFilter;
	}
	
	private String formatException(Throwable t) {
		StringBuilder sb = new StringBuilder();
		if (t != null) {
			sb.append(t.getClass().getCanonicalName()).append(": ").append(NEWLINE);
			StackTraceElement[] stack = t.getStackTrace();
			for (StackTraceElement e : stack) {
				sb	.append(e.getClassName()).append(".")
					.append(e.getMethodName()).append("()[")
					.append(e.getFileName()).append("@")
					.append(e.getLineNumber()).append("]")
					.append(NEWLINE);
			}
			if (t.getCause() != null) {
				sb.append("Caused by").append(NEWLINE);
				sb.append(formatException(t.getCause()));
			}
		}
		return sb.toString();
	}
	
	/**
	 * Get map of object types (Util), sorted by name
	 * @return Map<String, String>. Key is JiraConfigUtil.getName(), value is JiraConfigUtil class name.
	 */
	public Map<String, String> getObjectTypes() {
		Map<String, String> result = new TreeMap<>();
		for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList()) {
			result.put(util.getName(), util.getClass().getCanonicalName());
		}
		return result;
	}
	
	/**
	 * Get error messages.
	 * @return String
	 */
	public String getErrorMessage() {
		return this.data.errorMessage.toString();
	}
	
	public DTOStore getExportData() {
		return this.data.exportStore;
	}
	
	public DTOStore getImportData() {
		return this.data.importStore;
	}
	
	private void loadData(HttpServletRequest req) {
		// Load all objects
		boolean doInit = true;
		HttpSession session = req.getSession();
		Object data = session.getAttribute(SESSION_DATA);
		if (data != null) {
			try {
				this.data = (SessionData) data;
				doInit = false;
			} catch (Exception ex) {
				LOGGER.debug("Unable to cast session data, will reinitialze");
			}
		} 
		if (doInit) {
			// Initialize
			this.data.exportStore.clear();
			this.data.exportStore = new DTOStore(true);
			for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(true)) {
				try {
					LOGGER.debug("Loading data for " + util.getName());
					for (JiraConfigDTO dto : util.search(null).values()) {
						if (this.data.exportStore.check(dto) == null) {
							LOGGER.debug("Adding item to store: " + dto.getConfigName());
							this.data.exportStore.register(dto);	
							LOGGER.debug("Added item to store: " + dto.getConfigName());
						} else {
							LOGGER.debug("Item already in store: " + dto.getConfigName());
						}
					}
//					if (util.isPostSequenced()) {
//						LOGGER.debug("Update sequence");
//						util.updateSequence(listToSort);
				} catch (Exception ex) {
					this.data.errorMessage.append("Unable to load data from " + util.getName() + ": ")
						.append(formatException(ex));
				}
			}
			session.setAttribute(SESSION_DATA, this.data);
		}
	}
	
	private void mapData() {
		// TODO Import logic
	}
	
	@Override
	protected void doValidation() {
	}

	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		
		HttpServletRequest req = this.getHttpRequest();
		loadData(req);
		
		String action = req.getParameter(PARAM_ACTION);
		
		// Update selected items
		String selectedExports = req.getParameter(PARAM_SELECTED_EXPORTS);
		LOGGER.debug("Update selection: " + selectedExports);
		if (selectedExports != null) {
			MappingIterator<SelectionData> it = OR.readValues(selectedExports);
			while (it.hasNext()) {
				SelectionData data = it.next();
				Map<String, JiraConfigDTO> typeStore = this.data.exportStore.getTypeStore(data.utilName);
				if (typeStore != null && typeStore.containsKey(data.uniqueKey)) {
					JiraConfigDTO dto = typeStore.get(data.uniqueKey);
					LOGGER.debug("Object: " + dto.getConfigName() + ": " + data.selected);
					dto.setSelected(data.selected);
				}
			}
		}
		
		LOGGER.debug("Action: " + action);		
		// Perform action
		if (ACTION_IMPORT.equals(action)) { 
			// Import selected items in import store
		} else if (ACTION_UPLOAD.equals(action)) {
			// Reload import items
		} else if (ACTION_RELOAD.equals(action)) {
			// Reload export items
			// Remap if file uploaded
		} else if (ACTION_EXPORT.equals(action)) {
			// Export selected items in export store
			Map<String, List<JiraConfigDTO>> output = new LinkedHashMap<>();
			for (JiraConfigUtil util : this.data.exportStore.getUtils()) {
				List<JiraConfigDTO> items = new ArrayList<>();
				output.put(util.getImplementation(), items);
				for (JiraConfigDTO dto : this.data.exportStore.getTypeStore(util).values()) {
					if (dto.isSelected()) {
						items.add(dto);
					}
				}
			}
			String out = OM.writeValueAsString(output);
			LOGGER.debug("ExportAction2: <" + out + ">");
			// TODO Save file on server, provide download link
		} else if (ACTION_OBJECT_TYPE.equals(action)) {
			this.data.objectType = req.getParameter(PARAM_OBJECT_TYPE);
		} else if (ACTION_EXPORT_FILTER.equals(action)) {
			this.data.exportFilter = req.getParameter(PARAM_EXPORT_FILTER);
		} else if (ACTION_EXPORT_VIEW.equals(action)) {
			String item = req.getParameter(PARAM_VIEW_EXPORT);
			SelectionData data = OR.readValue(item);
			if (data != null) {
				this.data.view = null;
				this.data.viewName = null;
				this.data.viewType = null;
				JiraConfigDTO dto = this.data.exportStore.getTypeStore(data.utilName).get(data.uniqueKey);
				if (dto != null) {
					this.data.view = dto.getMap();
					this.data.viewName = dto.getConfigName();
					this.data.viewType = JiraConfigTypeRegistry.getConfigUtil(dto.getUtilClass()).getName();
				} 
			}
		}
		return JiraWebActionSupport.INPUT;
	}
}
