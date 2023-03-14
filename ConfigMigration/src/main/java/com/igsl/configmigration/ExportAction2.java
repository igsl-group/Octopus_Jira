package com.igsl.configmigration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.igsl.configmigration.export.v1.ExportData;
import com.igsl.configmigration.project.ProjectUtil;

public class ExportAction2 extends JiraWebActionSupport {

	@ComponentImport
	private final ActiveObjects ao;

	// Selection data class.
	// UI will pass an array of SelectionData for updated items
	public static class SelectionData {
		public String utilName;
		public String uniqueKey;
		public boolean selected;
	}

	// Data stored in HTTP session
	public static class SessionData {
		public DTOStore exportStore = new DTOStore();
		public DTOStore importDataStore = new DTOStore(); // Store for deserialized data
		public DTOStore importServerStore = new DTOStore(); // Store for existing server data
		public String objectType = ProjectUtil.class.getCanonicalName();
		public String exportFilter = "";
		public String importFilter = "";
		public StringBuilder errorMessage = new StringBuilder();
		public Map<String, JiraConfigProperty> view;
		public Stack<JiraConfigRef> viewHistory = new Stack<>();
		public boolean selectNested = true;
	}

	private static final long serialVersionUID = 1L;

	// Custom field configuration URL
	private static final String PAGE_URL = "/secure/admin/plugins/handler/ExportAction2.jspa";
	private static final String DOWNLOAD_URL = "/secure/admin/plugins/handler/ManageExport.jspa?action=download&idList=";
	private static final String NEWLINE = "\r\n";

	// Session variable
	private static final String SESSION_DATA = "ExportAction2SessionData";

	// Actions
	private static final String ACTION_OBJECT_TYPE = "objectType";
	private static final String ACTION_EXPORT_FILTER = "exportFilter";
	private static final String ACTION_EXPORT_VIEW = "viewExport";
	private static final String ACTION_EXPORT_VIEW_ADD = "viewExportAdd";
	private static final String ACTION_EXPORT_VIEW_JUMP = "viewExportJump";
	private static final String ACTION_EXPORT = "export";

	private static final String ACTION_RELOAD = "reload";
	private static final String ACTION_IMPORT = "import";
	private static final String ACTION_UPLOAD = "upload";

	// Form data
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_SELECT_NESTED = "selectNested";
	private static final String PARAM_OBJECT_TYPE = "objectType";
	private static final String PARAM_EXPORT_FILTER = "exportFilter";
	private static final String PARAM_SELECTED_EXPORTS = "selectedExports";
	private static final String PARAM_VIEW_EXPORT = "viewExport";
	private static final String PARAM_EXPORT_DESC = "exportDesc";
	
	private static Logger LOGGER = LoggerFactory.getLogger(ExportAction2.class);
	private static ObjectMapper OM;
	private static ObjectReader OR;

	private SessionData data = new SessionData();

	static {
		OM = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
		OR = OM.readerFor(SelectionData.class);
	}

	public ExportAction2(@ComponentImport ActiveObjects ao) {
		LOGGER.debug("Inject ActiveObjects: " + ao);
		this.ao = ao;
	}

	public Map<String, JiraConfigProperty> getView() {
		return this.data.view;
	}

	public List<JiraConfigRef> getViewHistory() {
		return this.data.viewHistory;
	}

	public DTOStore getExportStore() {
		return this.data.exportStore;
	}

	public DTOStore getImportDataStore() {
		return this.data.importDataStore;
	}

	public DTOStore getImportServerStore() {
		return this.data.importServerStore;
	}

	public boolean getSelectNested() {
		return this.data.selectNested;
	}

	public String getObjectType() {
		return this.data.objectType;
	}

	public String getObjectTypeName() {
		JiraConfigUtil util = JiraConfigTypeRegistry.getConfigUtil(this.data.objectType);
		if (util != null) {
			return util.getName();
		}
		return null;
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
			sb.append(t.getClass().getCanonicalName()).append("(").append(t.getMessage()).append("):").append(NEWLINE);
			StackTraceElement[] stack = t.getStackTrace();
			for (StackTraceElement e : stack) {
				sb.append(e.getClassName()).append(".").append(e.getMethodName()).append("()[").append(e.getFileName())
						.append("@").append(e.getLineNumber()).append("]").append(NEWLINE);
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
	 * 
	 * @return Map<String, String>. Key is JiraConfigUtil.getName(), value is
	 *         JiraConfigUtil class name.
	 */
	public Map<String, String> getObjectTypes() {
		Map<String, String> result = new TreeMap<>();
		for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(true)) {
			result.put(util.getName(), util.getClass().getCanonicalName());
		}
		return result;
	}

	/**
	 * Get error messages.
	 * 
	 * @return String
	 */
	public String getErrorMessage() {
		return this.data.errorMessage.toString();
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
			for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(true)) {
				int count = 0;
				try {
					LOGGER.debug("Loading data for " + util.getName());
					for (JiraConfigDTO dto : util.search(null).values()) {
						JiraConfigDTO r = util.register(this.data.exportStore, dto);
						if (r == dto) {
							count++;
						}
					}
				} catch (Exception ex) {
					LOGGER.error("Unable to load data from " + util.getName(), ex);
					this.data.errorMessage.append("Unable to load data from " + util.getName() + ": ")
							.append(formatException(ex));
				}
				LOGGER.debug("Distinct objects registered: " + count);
			}
			session.setAttribute(SESSION_DATA, this.data);
			LOGGER.debug("Export Store: ");
			for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(false)) {
				Map m = this.data.exportStore.getTypeStore(util);
				try {
					LOGGER.debug("[STORE]: " + util.getName() + "(" + m.size() + "): " + OM.writeValueAsString(m));
				} catch (Exception ex) {
					LOGGER.error("[STORE]: " + util.getName() + "(" + m.size() + "): " + "OM Error", ex);
				}
			}
			LOGGER.debug("End of Export Store");
		}
	}

	private void mapData() {
		// TODO Import logic
	}

	@Override
	protected void doValidation() {
	}

	/**
	 * Recursively select JiraConfigDTO and its related objects
	 */
	private void selectDTO(DTOStore store, JiraConfigDTO dto, boolean select, boolean nested) {
		if (dto != null) {
			dto.setSelected(select);
			if (nested) {
				// Select related objects
				for (JiraConfigRef ref : dto.getRelatedObjects()) {
					Map<String, JiraConfigDTO> s = this.data.exportStore.getTypeStore(ref.getUtil());
					if (s != null) {
						JiraConfigDTO nestedDTO = s.get(ref.getUniqueKey());
						if (nestedDTO != null && (nestedDTO.isSelected() != select)) {
							selectDTO(store, nestedDTO, select, nested);
						}
					}
				}
			}
		}
	}
	
	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");

		HttpServletRequest req = this.getHttpRequest();
		loadData(req);

		String action = req.getParameter(PARAM_ACTION);

		// Update selected items
		String nested = req.getParameter(PARAM_SELECT_NESTED);
		LOGGER.debug("Update nested: " + nested);
		if (nested != null && !nested.isEmpty()) {
			this.data.selectNested = Boolean.parseBoolean(nested);
		}
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
					selectDTO(this.data.exportStore, dto, data.selected, this.data.selectNested);
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
			req.getSession().removeAttribute(SESSION_DATA);
			loadData(req);
			// Restore settings
			this.data.objectType = req.getParameter(PARAM_OBJECT_TYPE);
			this.data.exportFilter = req.getParameter(PARAM_EXPORT_FILTER);
		} else if (ACTION_EXPORT.equals(action)) {
			// Export selected items in export store
			Map<String, List<JiraConfigDTO>> output = new LinkedHashMap<>();
			for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(false)) {
				List<JiraConfigDTO> items = new ArrayList<>();
				output.put(util.getImplementation(), items);
				for (JiraConfigDTO dto : this.data.exportStore.getTypeStore(util).values()) {
					if (dto.isSelected()) {
						items.add(dto);
					}
				}
			}
			String desc = req.getParameter(PARAM_EXPORT_DESC);
			String out = OM.writeValueAsString(output);
			LOGGER.debug("ExportAction2: <" + out + ">");
			ExportData data = ao.executeInTransaction(new TransactionCallback<ExportData>() {
				@Override
				public ExportData doInTransaction() {
					ExportData ed = ao.create(ExportData.class);
					ed.setContent(out);
					ed.setDescription(desc);
					ed.setExportDate(new Date());
					ed.setExportUser(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getName());
					ed.save();
					LOGGER.debug("ExportData saved: " + ed.getID());
					return ed;
				}
			});
			// Move to download page
			return getRedirect(DOWNLOAD_URL + data.getID());
		} else if (ACTION_OBJECT_TYPE.equals(action)) {
			// Change objectType
			this.data.objectType = req.getParameter(PARAM_OBJECT_TYPE);
		} else if (ACTION_EXPORT_FILTER.equals(action)) {
			// Update filter
			this.data.exportFilter = req.getParameter(PARAM_EXPORT_FILTER);
		} else if (ACTION_EXPORT_VIEW.equals(action)) {
			String item = req.getParameter(PARAM_VIEW_EXPORT);
			SelectionData data = OR.readValue(item);
			if (data != null) {
				this.data.view = null;
				this.data.viewHistory = new Stack<>();
				JiraConfigDTO dto = this.data.exportStore.getTypeStore(data.utilName).get(data.uniqueKey);
				if (dto != null) {
					this.data.view = dto.getConfigProperties();
					LOGGER.debug("View: " + OM.writeValueAsString(this.data.view));
					this.data.viewHistory.add(new JiraConfigRef(dto));
				}
			}
		} else if (ACTION_EXPORT_VIEW_ADD.equals(action)) {
			String item = req.getParameter(PARAM_VIEW_EXPORT);
			SelectionData data = OR.readValue(item);
			if (data != null) {
				JiraConfigDTO dto = this.data.exportStore.getTypeStore(data.utilName).get(data.uniqueKey);
				if (dto != null) {
					this.data.view = dto.getConfigProperties();
					LOGGER.debug("View add: " + OM.writeValueAsString(this.data.view));
					this.data.viewHistory.push(new JiraConfigRef(dto));
				}
			}
		} else if (ACTION_EXPORT_VIEW_JUMP.equals(action)) {
			String item = req.getParameter(PARAM_VIEW_EXPORT);
			SelectionData data = OR.readValue(item);
			if (data != null) {
				JiraConfigDTO dto = this.data.exportStore.getTypeStore(data.utilName).get(data.uniqueKey);
				if (dto != null) {
					this.data.view = dto.getConfigProperties();
					while (!this.data.viewHistory.isEmpty()) {
						JiraConfigRef ref = this.data.viewHistory.peek();
						if (ref == null) {
							// Add current item
							this.data.viewHistory.push(new JiraConfigRef(dto));
							break;
						} else if (dto.getUtilClass().getCanonicalName().equals(ref.getUtil())
								&& dto.getUniqueKey().equals(ref.getUniqueKey())) {
							// Stop processing
							break;
						} else {
							// Not requested item, remove it
							this.data.viewHistory.pop();
						}
					}
				}
			}
		}
		return JiraWebActionSupport.INPUT;
	}
}
