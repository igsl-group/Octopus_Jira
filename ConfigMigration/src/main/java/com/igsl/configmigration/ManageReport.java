package com.igsl.configmigration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.igsl.configmigration.report.v1.MergeReport;
import com.igsl.configmigration.report.v1.MergeReportComparator;

import net.java.ao.Query;

public class ManageReport extends JiraWebActionSupport {

	private static final long serialVersionUID = 1L;
	
	private static final String ACTION_DELETE = "delete";
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_ID_LIST = "idList";
	
	private static final Logger LOGGER = Logger.getLogger(ManageReport.class);
	private ActiveObjects ao;
	
	public ManageReport(@ComponentImport ActiveObjects ao) {
		LOGGER.debug("Inject ActiveObjects: " + ao);
		this.ao = ao;
	}
	
	public List<MergeReport> getReportData() {
		MergeReport[] data = this.ao.find(MergeReport.class);
		Arrays.sort(data, new MergeReportComparator());
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
	
	@Override
	protected String doExecute() throws Exception {
		String action = getHttpRequest().getParameter(PARAM_ACTION);
		String[] idList = getHttpRequest().getParameterValues(PARAM_ID_LIST);
		if (idList != null) {
			for (String id : idList) {
				int idAsInt = Integer.parseInt(id);
				MergeReport[] data = ao.find(MergeReport.class, Query.select().where("ID = ?", idAsInt));
				if (data != null && data.length == 1) {
					if (ACTION_DELETE.equals(action)) {
						Path p = Paths.get(data[0].getReport());
						if (Files.exists(p) && !Files.isDirectory(p)) {
							Files.delete(p);
						}
						p = Paths.get(data[0].getImportData());
						if (Files.exists(p) && !Files.isDirectory(p)) {
							Files.delete(p);
						}
						ao.delete(data[0]);
					}
				}
			}
		}
		return JiraWebActionSupport.INPUT;
	}
	
}
