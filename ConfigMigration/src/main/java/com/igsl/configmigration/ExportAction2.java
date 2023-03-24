package com.igsl.configmigration;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.igsl.configmigration.export.v1.ExportData;
import com.igsl.configmigration.project.ProjectUtil;
import com.igsl.configmigration.report.v1.MergeReport;
import com.igsl.configmigration.report.v1.MergeReportData;

import webwork.action.ServletActionContext;
import webwork.multipart.MultiPartRequestWrapper;

public class ExportAction2 extends JiraWebActionSupport {

	@ComponentImport
	private final ActiveObjects ao;

	// Selection data class.
	// UI will pass an array of SelectionData for updated items
	public static class SelectionData {
		public boolean exportStore;
		public String utilName;
		public String uniqueKey;
		public boolean selected;
	}

	// Data stored in HTTP session
	public static class SessionData {
		public DTOStore exportStore = new DTOStore();	// Store for data in server
		public DTOStore importStore = new DTOStore(); // Store for imported data
		public String objectType = ProjectUtil.class.getCanonicalName();
		public String exportFilter = "";
		public String importFilter = "";
		public StringBuilder errorMessage = new StringBuilder();
		public Map<String, JiraConfigProperty> viewExport;
		public Stack<JiraConfigRef> viewExportHistory = new Stack<>();
		public Map<String, JiraConfigProperty> viewImport;
		public Stack<JiraConfigRef> viewImportHistory = new Stack<>();
		public boolean selectNested = true;
		public boolean showAllUtils = true;
		public String downloadAction = null;
		public Map<String, String> downloadParameters = new HashMap<>();
	}

	private static final long serialVersionUID = 1L;

	// Custom field configuration URL
	private static final String PAGE_URL = "/secure/admin/plugins/handler/ExportAction2.jspa";
	private static final String DOWNLOAD_URL = "/plugins/servlet/configmigrationdownload";
	private static final String REPORT_URL = "/plugins/servlet/configmigrationreport";
	private static final String NEWLINE = "\r\n";

	// Session variable
	private static final String SESSION_DATA = "ExportAction2SessionData";

	// Actions
	public static final String ACTION_OBJECT_TYPE = "objectType";
	public static final String ACTION_EXPORT_FILTER = "exportFilter";
	public static final String ACTION_IMPORT_FILTER = "importFilter";
	public static final String ACTION_VIEW = "view";
	public static final String ACTION_VIEW_ADD = "viewAdd";
	public static final String ACTION_VIEW_JUMP = "viewJump";
	public static final String ACTION_VIEW_CLEAR = "viewClear";
	public static final String ACTION_EXPORT = "export";
	public static final String ACTION_RELOAD = "reload";
	public static final String ACTION_IMPORT = "import";
	public static final String ACTION_MERGE = "merge";

	// Form fields
	public static final String PARAM_ACTION = "action";
	public static final String PARAM_SELECT_NESTED = "selectNested";
	public static final String PARAM_SHOW_ALL_UTILS = "showAllUtils";
	public static final String PARAM_OBJECT_TYPE = "objectType";
	public static final String PARAM_EXPORT_FILTER = "exportFilter";
	public static final String PARAM_IMPORT_FILTER = "importFilter";
	public static final String PARAM_SELECTED_OBJECTS = "selectedObjects";
	public static final String PARAM_VIEW_TYPE = "viewType";
	public static final String PARAM_VIEW_OBJECT = "viewObject";
	public static final String PARAM_EXPORT_DESC = "exportDesc";
	public static final String PARAM_IMPORT_FILE = "importFile";
	public static final String PARAM_MERGE_DESC = "mergeDesc";
	
	// Form field values
	public static final String PARAM_VIEW_TYPE_EXPORT = "viewExport";
	public static final String PARAM_VIEW_TYPE_IMPORT = "viewImport";
	
	private static Logger LOGGER = LoggerFactory.getLogger(ExportAction2.class);
	private static ObjectMapper OM;
	private static ObjectReader OR_SELECTION;
	private static ObjectReader OR_IMPORT_DATA;
	
	private SessionData data = new SessionData();

	static {
		OM = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
		OR_SELECTION = OM.readerFor(SelectionData.class);
		TypeReference<Map<String, List<JiraConfigDTO>>> importType = 
				new TypeReference<Map<String,List<JiraConfigDTO>>>(){};
		OR_IMPORT_DATA = OM.readerFor(importType);
	}

	public ExportAction2(@ComponentImport ActiveObjects ao) {
		LOGGER.debug("Inject ActiveObjects: " + ao);
		this.ao = ao;
	}
	
	public String getDownloadAction() {
		return this.data.downloadAction;
	}
	
	public Map<String, String> getDownloadParameters() {
		return this.data.downloadParameters;
	}

	public Map<String, JiraConfigProperty> getViewExport() {
		return this.data.viewExport;
	}

	public Map<String, JiraConfigProperty> getViewImport() {
		return this.data.viewImport;
	}
	
	public List<JiraConfigRef> getViewExportHistory() {
		return this.data.viewExportHistory;
	}

	public List<JiraConfigRef> getViewImportHistory() {
		return this.data.viewImportHistory;
	}
	
	public DTOStore getExportStore() {
		return this.data.exportStore;
	}

	public DTOStore getImportStore() {
		return this.data.importStore;
	}

	public boolean getSelectNested() {
		return this.data.selectNested;
	}
	
	public boolean getShowAllUtils() {
		return this.data.showAllUtils;
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
	 * Get a compare key guide between export and import stores.
	 * Velocity template can then use the key list to list both stores with matching items aligned.
	 * @return Map<String, Set<String>>
	 */
	public Map<JiraConfigUtil, Set<String>> getCompareKeyGuide() {
		Map<JiraConfigUtil, Set<String>> result = new LinkedHashMap<>();
		for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(false)) {		
			Set<String> keySet = new HashSet<>();
			Map<String, JiraConfigDTO> exportStore = this.data.exportStore.getTypeStore(util);
			if (exportStore != null) {
				keySet.addAll(exportStore.keySet());
			}
			Map<String, JiraConfigDTO> importStore = this.data.importStore.getTypeStore(util);
			if (importStore != null) {
				keySet.addAll(importStore.keySet());
			}
			LOGGER.debug("getCompareKeyGuide: " + util.getName() + ": " + keySet);
			result.put(util, keySet);
		}
		return result;
	}
	
	/**
	 * Get map of object types (Util), sorted by name
	 * 
	 * @return Map<String, String>. Key is JiraConfigUtil.getName(), value is
	 *         JiraConfigUtil class name.
	 */
	public Map<String, String> getObjectTypes(boolean showAll) {
		Map<String, String> result = new TreeMap<>();
		for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(!showAll)) {
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

	private void loadData(HttpServletRequest req, boolean reload) {
		// Load all objects
		boolean doInit = true;
		HttpSession session = req.getSession();
		Object data = session.getAttribute(SESSION_DATA);
		if (data != null && !reload) {
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
		}
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
					Map<String, JiraConfigDTO> s = store.getTypeStore(ref.getUtil());
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
	
	private JiraConfigDTO merge(JiraConfigUtil util, JiraConfigDTO newObj) throws Exception {
		JiraConfigDTO oldObj = this.data.exportStore.getTypeStore(util).get(newObj.getUniqueKey());
		return util.merge(oldObj, newObj);
	}

	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		
		HttpServletRequest req = this.getHttpRequest();
		loadData(req, false);
		
		// Clear download
		this.data.downloadAction = null;
		// Check if it is file upload
		String action = req.getParameter(PARAM_ACTION);
		File uploaded = null;
		HttpServletRequest wrapperReq = ServletActionContext.getRequest();
		if (wrapperReq != null && wrapperReq instanceof MultiPartRequestWrapper) {
			action = ACTION_IMPORT;
			MultiPartRequestWrapper mpr = (MultiPartRequestWrapper) wrapperReq;
			uploaded = mpr.getFile(PARAM_IMPORT_FILE);
		}
		// Update show all utils flag
		String showAll = req.getParameter(PARAM_SHOW_ALL_UTILS);
		if (showAll != null && !showAll.isEmpty()) {
			this.data.showAllUtils = Boolean.parseBoolean(showAll);
		}
		// Update selected items
		String nested = req.getParameter(PARAM_SELECT_NESTED);
		LOGGER.debug("Update nested: " + nested);
		if (nested != null && !nested.isEmpty()) {
			this.data.selectNested = Boolean.parseBoolean(nested);
		}
		String selectedObjects = req.getParameter(PARAM_SELECTED_OBJECTS);
		LOGGER.debug("Update selection: " + selectedObjects);
		if (selectedObjects != null) {
			MappingIterator<SelectionData> it = OR_SELECTION.readValues(selectedObjects);
			while (it.hasNext()) {
				SelectionData data = it.next();
				if (data.exportStore) {
					// Select objects in export store
					Map<String, JiraConfigDTO> typeStore = this.data.exportStore.getTypeStore(data.utilName);
					if (typeStore != null && typeStore.containsKey(data.uniqueKey)) {
						JiraConfigDTO dto = typeStore.get(data.uniqueKey);
						LOGGER.debug("Object: " + dto.getConfigName() + ": " + data.selected);
						selectDTO(this.data.exportStore, dto, data.selected, this.data.selectNested);
					}
				} else {
					// Select objects in import store
					Map<String, JiraConfigDTO> typeStore = this.data.importStore.getTypeStore(data.utilName);
					if (typeStore != null && typeStore.containsKey(data.uniqueKey)) {
						JiraConfigDTO dto = typeStore.get(data.uniqueKey);
						LOGGER.debug("Object: " + dto.getConfigName() + ": " + data.selected);
						selectDTO(this.data.importStore, dto, data.selected, this.data.selectNested);
					}
				}				
			}
		}

		LOGGER.debug("Action: " + action);
		// Perform action
		if (ACTION_MERGE.equals(action)) {
			// Create report
			String mergeDesc = req.getParameter(PARAM_MERGE_DESC);
			final MergeReport mr = ao.create(MergeReport.class);
			mr.setDescription(mergeDesc);
			mr.setMergeDate(new Date());
			Map<String, List<JiraConfigDTO>> importDataMap = new LinkedHashMap<>();
			for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(false)) {
				List<JiraConfigDTO> items = new ArrayList<>();
				importDataMap.put(util.getImplementation(), items);
				for (JiraConfigDTO dto : this.data.importStore.getTypeStore(util).values()) {
					if (dto.isSelected()) {
						items.add(dto);
					}
				}
			}
			String importData = OM.writeValueAsString(importDataMap);
			mr.setImportData(importData);
			mr.setMergeUser(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getName());
			long objCountTotal = 0;
			long objCountSuccess = 0;
			long objCountFailed = 0;
			long projCountTotal = 0;
			long projCountSuccess = 0;
			long projCountFailed = 0;
			List<MergeReportData> reportData = new ArrayList<>();
			// Merge selected objects
			for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(false)) {
				for (Map.Entry<String, JiraConfigDTO> entry : this.data.importStore.getTypeStore(util).entrySet()) {
					JiraConfigDTO dto = entry.getValue();
					if (dto.isSelected()) {
						objCountTotal++;
						if (util instanceof ProjectUtil) {
							projCountTotal++;
						}
						MergeReportData rd = new MergeReportData(dto);
						reportData.add(rd);
						try {
							JiraConfigDTO newDTO = merge(util, dto);
							if (newDTO != null) {
								rd.setResult(true);
								rd.setNewDTO(newDTO);
								objCountSuccess++;
								if (util instanceof ProjectUtil) {
									projCountSuccess++;
								}
							} else {
								throw new Exception("Unable to create object");
							}
						} catch (Exception ex) {
							rd.setResult(false);
							rd.addError(ex.getMessage());
							objCountFailed++;
							if (util instanceof ProjectUtil) {
								projCountFailed++;
							}
						}
					}
				}
			}
			// Save report
			mr.setReport(OM.writeValueAsString(reportData));
			mr.setTotalObjectCount(objCountTotal);
			mr.setSuccessObjectCount(objCountSuccess);
			mr.setFailedObjectCount(objCountFailed);
			mr.setTotalProjectCount(projCountTotal);
			mr.setSuccessProjectCount(projCountSuccess);
			mr.setFailedProjectCount(projCountFailed);
			ao.executeInTransaction(new TransactionCallback<MergeReport>() {
				@Override
				public MergeReport doInTransaction() {
					mr.save();
					return mr;
				}
			});
			// Reload export items
			loadData(req, true);
			this.data.downloadAction = REPORT_URL;
			this.data.downloadParameters.clear();
			this.data.downloadParameters.put("id", Integer.toString(mr.getID()));
		} else if (ACTION_IMPORT.equals(action)) {
			// Import selected items in import store
			if (uploaded != null) {
				LOGGER.debug("Uploaded file: " + uploaded.getAbsolutePath());
				try {
					this.data.importStore.clear();
					Map<String, List<JiraConfigDTO>> importData = OR_IMPORT_DATA.readValue(uploaded);
					LOGGER.debug("Import data parsed, size: " + importData.size());
					for (Map.Entry<String, List<JiraConfigDTO>> entry : importData.entrySet()) {
						JiraConfigUtil util = JiraConfigTypeRegistry.getConfigUtil(entry.getKey());
						if (util != null) {
							for (JiraConfigDTO dto : entry.getValue()) {
								util.register(this.data.importStore, dto);
							}
							for (JiraConfigDTO dto : entry.getValue()) {
								dto.setupRelatedObjects();
							}
						}
					}
				} catch (Exception ex) {
					LOGGER.error("Failed to parse import data", ex);
				}
			} else {
				// Display error
				LOGGER.debug("File upload failed");
			}
		} else if (ACTION_VIEW_CLEAR.equals(action)) {
			// Clear export view
			this.data.viewExport = null;
			this.data.viewExportHistory.clear();
			this.data.viewImport = null;
			this.data.viewImportHistory.clear();
		} else if (ACTION_RELOAD.equals(action)) {
			// Reload export items
			loadData(req, true);
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
			this.data.downloadAction = DOWNLOAD_URL;
			this.data.downloadParameters.clear();
			this.data.downloadParameters.put("type", "report");
			this.data.downloadParameters.put("id", Integer.toString(data.getID()));
		} else if (ACTION_OBJECT_TYPE.equals(action)) {
			// Change objectType
			this.data.objectType = req.getParameter(PARAM_OBJECT_TYPE);
		} else if (ACTION_EXPORT_FILTER.equals(action)) {
			// Update filter
			this.data.exportFilter = req.getParameter(PARAM_EXPORT_FILTER);
		} else if (ACTION_IMPORT_FILTER.equals(action)) {
			// Update filter
			this.data.importFilter = req.getParameter(PARAM_IMPORT_FILTER);
		} else if (ACTION_VIEW.equals(action)) {
			// View both export and import, if possible
			String item = req.getParameter(PARAM_VIEW_OBJECT);
			SelectionData viewObject = OR_SELECTION.readValue(item);
			if (viewObject != null) {
				this.data.viewExport = null;
				this.data.viewExportHistory = new Stack<>();
				JiraConfigDTO exportDTO = this.data.exportStore.getTypeStore(viewObject.utilName)
						.get(viewObject.uniqueKey);
				if (exportDTO != null) {
					this.data.viewExport = exportDTO.getConfigProperties();
					this.data.viewExportHistory.add(new JiraConfigRef(exportDTO));
				}
				this.data.viewImport = null;
				this.data.viewImportHistory = new Stack<>();
				JiraConfigDTO importDTO = this.data.importStore.getTypeStore(viewObject.utilName)
						.get(viewObject.uniqueKey);
				if (importDTO != null) {
					this.data.viewImport = importDTO.getConfigProperties();
					this.data.viewImportHistory.add(new JiraConfigRef(importDTO));
				}
			}
			LOGGER.debug("Current export view: " + this.data.viewExport);
			LOGGER.debug("Current import view: " + this.data.viewImport);
		} else if (ACTION_VIEW_ADD.equals(action)) {
			String item = req.getParameter(PARAM_VIEW_OBJECT);
			SelectionData data = OR_SELECTION.readValue(item);
			if (data != null) {
				if (data.exportStore) {
					JiraConfigDTO dto = this.data.exportStore.getTypeStore(data.utilName).get(data.uniqueKey);
					if (dto != null) {
						this.data.viewExport = dto.getConfigProperties();
						this.data.viewExportHistory.push(new JiraConfigRef(dto));
					}
				} else {
					JiraConfigDTO dto = this.data.importStore.getTypeStore(data.utilName).get(data.uniqueKey);
					if (dto != null) {
						this.data.viewImport = dto.getConfigProperties();
						this.data.viewImportHistory.push(new JiraConfigRef(dto));
					}
				}
			}
		} else if (ACTION_VIEW_JUMP.equals(action)) {
			String item = req.getParameter(PARAM_VIEW_OBJECT);
			SelectionData data = OR_SELECTION.readValue(item);
			if (data != null) {
				if (data.exportStore) {
					JiraConfigDTO dto = this.data.exportStore.getTypeStore(data.utilName).get(data.uniqueKey);
					if (dto != null) {
						this.data.viewExport = dto.getConfigProperties();
						adjustViewStack(dto, this.data.viewExportHistory);
					}
				} else {
					JiraConfigDTO dto = this.data.importStore.getTypeStore(data.utilName).get(data.uniqueKey);
					if (dto != null) {
						this.data.viewImport = dto.getConfigProperties();
						adjustViewStack(dto, this.data.viewImportHistory);
					}
				}
			}
		}
		return JiraWebActionSupport.INPUT;
	}

	// Adjust stack to find provided DTO
	private void adjustViewStack(JiraConfigDTO dto, Stack<JiraConfigRef> viewStack) {
		while (!viewStack.isEmpty()) {
			JiraConfigRef ref = viewStack.peek();
			if (ref == null) {
				// Add current item
				viewStack.push(new JiraConfigRef(dto));
				break;
			} else if (dto.getUtilClass().getCanonicalName().equals(ref.getUtil())
					&& dto.getUniqueKey().equals(ref.getUniqueKey())) {
				// Stop processing
				break;
			} else {
				// Not requested item, remove it
				viewStack.pop();
			}
		}
	}
	
}
