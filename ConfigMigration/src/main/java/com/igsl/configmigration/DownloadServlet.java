package com.igsl.configmigration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
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
import com.igsl.configmigration.export.v1.ExportData;

import net.java.ao.Query;

public class DownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadServlet.class);
	private static final String PARAM_ID = "id";
	private static final int BUFFER_SIZE = 10240;
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd HHmmss");
	
	@JiraImport
	private ActiveObjects ao;
	
	public DownloadServlet(@ComponentImport ActiveObjects ao) {
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
		if (id != null && !id.isEmpty()) {
			int idAsInt = Integer.parseInt(id);
			ExportData[] data = ao.find(ExportData.class, Query.select().where("ID = ?", idAsInt));
			if (data != null && data.length == 1) {
				String filePath = data[0].getContent();
				Path path = Paths.get(filePath);
				if (!Files.exists(path) || Files.isDirectory(path)) {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File " + filePath + " not found");
					return;
				}				
				resp.setContentType("application/zip");
				resp.setHeader("Content-disposition", "attachment; filename=\"" + path.getFileName().toString() + "\"");
		        try (	InputStream in = new FileInputStream(path.toFile()); 
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
