package com.igsl.configmigration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.igsl.configmigration.project.ProjectUtil;

// Data stored in HTTP session
public class ExportAction2SessionData {
	public DTOStore exportStore = new DTOStore();	// Store for data in server
	public DTOStore importStore = new DTOStore(); // Store for imported data
	public String objectType = null;
	public String exportFilter = "";
	public String importFilter = "";
	public String upload = null;
	public List<String> errorMessage = new ArrayList<>();
	public String viewExportUniqueKey;
	public Map<String, JiraConfigProperty> viewExport;
	public Stack<JiraConfigRef> viewExportHistory = new Stack<>();
	public String viewImportUniqueKey;
	public Map<String, JiraConfigProperty> viewImport;
	public Stack<JiraConfigRef> viewImportHistory = new Stack<>();
	public boolean selectNested = true;
	public boolean showAllUtils = false;
	public String downloadAction = null;
	public Map<String, String> downloadParameters = new HashMap<>();
}