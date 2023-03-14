package com.igsl.configmigration;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.igsl.configmigration.export.v1.ExportData;

import net.java.ao.Query;

public class ManageExport extends JiraWebActionSupport {

	private static final long serialVersionUID = 1L;
	
	private static final String ACTION_DELETE = "delete";
	private static final String ACTION_DOWNLOAD = "download";
	
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_ID_LIST = "idList";
	
	private static final Logger LOGGER = Logger.getLogger(ManageExport.class);
	private ActiveObjects ao;
	
	public ManageExport(@ComponentImport ActiveObjects ao) {
		LOGGER.debug("Inject ActiveObjects: " + ao);
		this.ao = ao;
	}
	
	public List<ExportData> getExportData() {
		ExportData[] data = this.ao.find(ExportData.class);
		return Arrays.asList(data);
	}

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public String formatDate(Date d) {
		if (d != null) {
			return SDF.format(d);
		}
		return null;
	}

	public String getUserInfo(String name) {
		ApplicationUser user = ComponentAccessor.getUserManager().getUserByName(name);
		if (user != null) {
			return user.getDisplayName() + " (" + user.getName() + ")";
		}
		return null;
	}
	
	private String autoDownload = null;
	public String getAutoDownload() {
		return this.autoDownload;
	}
	
	@Override
	protected String doExecute() throws Exception {
		autoDownload = null;
		String action = getHttpRequest().getParameter(PARAM_ACTION);
		String id = getHttpRequest().getParameter(PARAM_ID_LIST);
		LOGGER.debug("Action: " + action + ", ID: " + id);
		ExportData[] data = ao.find(ExportData.class, Query.select().where("ID = ?", id));
		if (data != null && data.length == 1) {
			if (ACTION_DELETE.equals(action)) {
				ao.delete(data[0]);
			} else if (ACTION_DOWNLOAD.equals(action)) {
				autoDownload = Integer.toString(data[0].getID());
			}
		}
		return JiraWebActionSupport.INPUT;
	}
	
}
