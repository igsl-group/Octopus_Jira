package com.igsl.configmigration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.igsl.configmigration.report.v1.MergeReport;

import net.java.ao.Query;

public class ReportServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportServlet.class);
	private static final String PARAM_ID = "id";
	private static final String PARAM_TYPE = "type";
	public static final String PARAM_TYPE_REPORT = "report";
	public static final String PARAM_TYPE_IMPORT_DATA = "importData";
	private static final int BUFFER_SIZE = 10240;
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd HHmmss");
	
	@JiraImport
	private ActiveObjects ao;
	
	public ReportServlet(@ComponentImport ActiveObjects ao) {
		this.ao = ao;
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// Check if user has system admin rights
		ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		GlobalPermissionManager gpm = ComponentAccessor.getGlobalPermissionManager();
		if (currentUser == null || !gpm.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, currentUser)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You must be system admin and logged in");
			return;
		}
		// Read content and construct download
		String id = req.getParameter(PARAM_ID);
		String type = req.getParameter(PARAM_TYPE);
		if (id != null && !id.isEmpty()) {
			int idAsInt = Integer.parseInt(id);
			MergeReport[] data = ao.find(MergeReport.class, Query.select().where("ID = ?", idAsInt));
			if (data != null && data.length == 1) {
				String fileName = "";
				if (PARAM_TYPE_REPORT.equals(type)) {				
					fileName = data[0].getReport();
				} else if (PARAM_TYPE_IMPORT_DATA.equals(type)) {
					fileName = data[0].getImportData();
				}
				Path p = Paths.get(fileName);;
				resp.setHeader("Content-disposition", "attachment; filename=\"" + p.getFileName() + "\"");
		        resp.setContentType("application/zip; charset=UTF-8");
		        try (	InputStream in = new FileInputStream(p.toFile()); 
		        		OutputStream out = resp.getOutputStream()) {
		        	byte[] buffer = new byte[BUFFER_SIZE];
		            int numBytesRead;
		            while ((numBytesRead = in.read(buffer)) > 0) {
		            	out.write(buffer, 0, numBytesRead);
		            }
		        }
			} else {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requested download (" + id + ") not found");
			}
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter id is not provided");
		}
	}
}
