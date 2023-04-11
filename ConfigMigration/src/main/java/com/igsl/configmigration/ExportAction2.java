package com.igsl.configmigration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.igsl.configmigration.export.v1.ExportData;
import com.igsl.configmigration.project.ProjectUtil;
import com.igsl.configmigration.report.v1.MergeReport;
import com.igsl.configmigration.report.v1.MergeReportData;

public class ExportAction2 extends JiraWebActionSupport {

	private static final String TITLE_CURRENT = "current";
	private static final String TITLE_IMPORTED = "imported";
	
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

	private static final long serialVersionUID = 1L;

	// Custom field configuration URL
	public static final String PAGE_URL = "/secure/admin/plugins/handler/ExportAction2.jspa";
	private static final String DOWNLOAD_URL = "/plugins/servlet/configmigrationdownload";
	private static final String REPORT_URL = "/plugins/servlet/configmigrationreport";
	private static final String UPLOAD_URL = "/plugins/servlet/configmigrationupload";
	private static final String NEWLINE = "\r\n";

	// Session variable
	public static final String SESSION_DATA = "ExportAction2SessionData";

	// Actions
	public static final String ACTION_OBJECT_TYPE = "objectType";
	public static final String ACTION_EXPORT_FILTER = "exportFilter";
	public static final String ACTION_IMPORT_FILTER = "importFilter";
	public static final String ACTION_CLEAR_IMPORT = "clearImport";
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
	private static ObjectMapper OM_INDENT;
	private static ObjectReader OR_SELECTION;
	private static ObjectReader OR_IMPORT_DATA;
	
	private ExportAction2SessionData data = new ExportAction2SessionData();

	static {
		OM = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
		OM_INDENT = new ObjectMapper()
							.setSerializationInclusion(Include.NON_NULL)
							.enable(SerializationFeature.INDENT_OUTPUT);
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
	
	public String getViewExportUniqueKey() {
		return this.data.viewExportUniqueKey;
	}

	public String getViewImportUniqueKey() {
		return this.data.viewImportUniqueKey;
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
	
	private List<String> formatException(Throwable t) {
		List<String> result = new ArrayList<>();
		if (t != null) {
			result.add(t.getClass().getCanonicalName() + "(" + t.getMessage() + "):");
			StackTraceElement[] stack = t.getStackTrace();
			for (StackTraceElement e : stack) {
				result.add(	e.getClassName() + "." + e.getMethodName() + "()" + 
							"[" + e.getFileName() + "@" + e.getLineNumber() + "]");
			}
			if (t.getCause() != null) {
				result.add("Caused by");
				result.addAll(formatException(t.getCause()));
			}
		}
		return result;
	}

	public String getUploadServlet() {
		return getServletContext().getContextPath() + UPLOAD_URL;
	}
	
	/**
	 * Get a compare key guide between export and import stores.
	 * Velocity template can then use the key list to list both stores with matching items aligned.
	 * @return Map<String, Set<String>>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<JiraConfigUtil, Set<String>> getCompareKeyGuide() {
		Map<JiraConfigUtil, Set<String>> result = new LinkedHashMap<>();
		for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(false)) {	
			List<JiraConfigDTO> list = new ArrayList<>();
			Comparator comparator = util.getComparator();
			LOGGER.debug("getCompareKeyGuide: " + util.getName() + " start");
			Map<String, JiraConfigDTO> exportStore = this.data.exportStore.getTypeStore(util);
			if (exportStore != null) {
				LOGGER.debug("export keyset: " + exportStore.keySet());
				for (JiraConfigDTO dto : exportStore.values()) {
					list.add(dto);
				}
			}
			Map<String, JiraConfigDTO> importStore = this.data.importStore.getTypeStore(util);
			if (importStore != null) {
				LOGGER.debug("import keyset: " + importStore.keySet());
				list.addAll(importStore.values());
			}
			if (comparator != null) {
				list.sort(comparator);
			}
			HashSet<String> keySet = new LinkedHashSet<>();
			for (JiraConfigDTO dto : list) {
				keySet.add(dto.getUniqueKey());
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
			String impl = util.getImplementation();
			String name = (util.isVisible())? util.getName() : util.getName() + "*";
			result.put(	name + 
						" - " + TITLE_CURRENT + " " + this.data.exportStore.getCounts(impl) + 
						" " + TITLE_IMPORTED + " " + this.data.importStore.getCounts(impl),
						util.getClass().getCanonicalName());
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
				this.data = (ExportAction2SessionData) data;
				doInit = false;
			} catch (Exception ex) {
				LOGGER.debug("Unable to cast session data, will reinitialze");
			}
		}
		if (doInit) {
			// Initialize
			this.data.exportStore.clear();
			for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(false)) {
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
					this.data.errorMessage.addAll(formatException(ex));
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
				for (JiraConfigRef ref : dto.getRelatedObjectList()) {
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
	
	private MergeResult merge(JiraConfigUtil util, JiraConfigDTO newObj) throws Exception {
		JiraConfigDTO oldObj = this.data.exportStore.getTypeStore(util).get(newObj.getUniqueKey());
		return util.merge(oldObj, newObj);
	}
	
	private void clearExportView() {
		this.data.viewExport = null;
		this.data.viewExportHistory.clear();
		this.data.viewExportUniqueKey = null;
	}

	private void clearImportView() {
		this.data.viewImport = null;
		this.data.viewImportHistory.clear();
		this.data.viewImportUniqueKey = null;
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
	
	private void setExportView(JiraConfigDTO dto) {
		setExportView(dto, true);
	}
	private void setExportView(JiraConfigDTO dto, boolean addToStack) {
		this.data.viewExport = dto.getConfigProperties();
		if (addToStack) {
			this.data.viewExportHistory.push(new JiraConfigRef(dto));
		} else {
			adjustViewStack(dto, this.data.viewExportHistory);
		}
		this.data.viewExportUniqueKey = dto.getUniqueKey();
	}
	
	private void setImportView(JiraConfigDTO dto) {
		setImportView(dto, true);
	}
	private void setImportView(JiraConfigDTO dto, boolean addToStack) {
		this.data.viewImport = dto.getConfigProperties();
		if (addToStack) {
			this.data.viewImportHistory.push(new JiraConfigRef(dto));
		} else {
			adjustViewStack(dto, this.data.viewImportHistory);
		}
		this.data.viewImportUniqueKey = dto.getUniqueKey();
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
		// Update show all utils flag
		String showAll = req.getParameter(PARAM_SHOW_ALL_UTILS);
		if (showAll != null && !showAll.isEmpty()) {
			this.data.showAllUtils = Boolean.parseBoolean(showAll);
			if (!this.data.showAllUtils) {
				// Check if current object type is hidden, if so reset to all
				if (!this.data.objectType.isEmpty()) {
					JiraConfigUtil util = JiraConfigTypeRegistry.getConfigUtil(this.data.objectType);
					if (!util.isVisible()) {
						this.data.objectType = "";
						clearExportView();
						clearImportView();
					}
				}
			}
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
							if (!util.isDefaultObject(dto)) {
								MergeResult r = merge(util, dto);
								for (String s : r.getWarnings()) {
									rd.addWarning(s);
								}
								JiraConfigDTO newDTO = r.getNewDTO();
								if (newDTO != null) {
									rd.setResult(true);
									rd.setNewDTO(newDTO);
									objCountSuccess++;
									if (util instanceof ProjectUtil) {
										projCountSuccess++;
									}
								} else {
									rd.setResult(false);
									rd.setNewDTO(null);
									rd.addError("Unable to create object \"" + dto.getConfigName() + "\" of " + util.getName());
									objCountFailed++;
									if (util instanceof ProjectUtil) {
										projCountFailed++;
									}
								}
							} else {
								// Default object is not updated, count as success
								objCountSuccess++;
								if (util instanceof ProjectUtil) {
									projCountSuccess++;
								}
								rd.setResult(true);
								rd.setNewDTO(null);
							}
						} catch (Exception ex) {
							rd.setResult(false);
							rd.addErrors(formatException(ex));
							objCountFailed++;
							if (util instanceof ProjectUtil) {
								projCountFailed++;
							}
						}
					}
				}
			}
			// Save report
			mr.setReport(OM_INDENT.writeValueAsString(reportData));
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
			// Close details
			clearExportView();
			clearImportView();
			// Reload export items
			loadData(req, true);
			this.data.downloadAction = REPORT_URL;
			this.data.downloadParameters.clear();
			this.data.downloadParameters.put("type", "report");
			this.data.downloadParameters.put("id", Integer.toString(mr.getID()));
		} else if (ACTION_CLEAR_IMPORT.equals(action)) {
			clearImportView();
			this.data.importStore.clear();
		} else if (ACTION_IMPORT.equals(action)) {
			// Import selected items in import store
			clearImportView();
			if (this.data.upload != null) {
				try {
					this.data.importStore.clear();
					Map<String, List<JiraConfigDTO>> importData = OR_IMPORT_DATA.readValue(this.data.upload);
					LOGGER.debug("Import data parsed, size: " + importData.size());
					for (Map.Entry<String, List<JiraConfigDTO>> entry : importData.entrySet()) {
						LOGGER.debug("Importing data for util: " + entry.getKey());
						JiraConfigUtil util = JiraConfigTypeRegistry.getConfigUtil(entry.getKey());
						if (util != null) {
							LOGGER.debug("Importing data for util: " + util.getName());
							for (JiraConfigDTO dto : entry.getValue()) {
								util.register(this.data.importStore, dto);
							}
//							for (JiraConfigDTO dto : entry.getValue()) {
//								dto.setupRelatedObjects();
//							}
						} else {
							LOGGER.debug("Importing data for util not found: " + entry.getKey());
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
			clearExportView();
			clearImportView();
		} else if (ACTION_RELOAD.equals(action)) {
			clearExportView();
			clearImportView();
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
			// Close details
			clearExportView();
			clearImportView();
		} else if (ACTION_EXPORT_FILTER.equals(action)) {
			// Update filter
			this.data.exportFilter = req.getParameter(PARAM_EXPORT_FILTER);
			// Close details
			clearExportView();
			clearImportView();
		} else if (ACTION_IMPORT_FILTER.equals(action)) {
			// Update filter
			this.data.importFilter = req.getParameter(PARAM_IMPORT_FILTER);
			// Close details
			clearExportView();
			clearImportView();
		} else if (ACTION_VIEW.equals(action)) {
			// View both export and import, if possible
			String item = req.getParameter(PARAM_VIEW_OBJECT);
			SelectionData viewObject = OR_SELECTION.readValue(item);
			if (viewObject != null) {
				clearExportView();
				JiraConfigDTO exportDTO = this.data.exportStore.getTypeStore(viewObject.utilName)
						.get(viewObject.uniqueKey);
				if (exportDTO != null) {
					setExportView(exportDTO);
				}
				clearImportView();
				JiraConfigDTO importDTO = this.data.importStore.getTypeStore(viewObject.utilName)
						.get(viewObject.uniqueKey);
				if (importDTO != null) {
					setImportView(importDTO);
				}
			}
		} else if (ACTION_VIEW_ADD.equals(action)) {
			String item = req.getParameter(PARAM_VIEW_OBJECT);
			SelectionData data = OR_SELECTION.readValue(item);
			if (data != null) {
				if (data.exportStore) {
					JiraConfigDTO dto = this.data.exportStore.getTypeStore(data.utilName).get(data.uniqueKey);
					if (dto != null) {
						setExportView(dto);
					}
				} else {
					JiraConfigDTO dto = this.data.importStore.getTypeStore(data.utilName).get(data.uniqueKey);
					if (dto != null) {
						setImportView(dto);
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
						setExportView(dto, false);
					}
				} else {
					JiraConfigDTO dto = this.data.importStore.getTypeStore(data.utilName).get(data.uniqueKey);
					if (dto != null) {
						setImportView(dto, false);
					}
				}
			}
		}
		return JiraWebActionSupport.INPUT;
	}

}
