package com.igsl.configmigration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.igsl.configmigration.workflow.mapper.MapperConfigUtil;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigWrapper;

public class MappingServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MappingServlet.class);
	private static final String PARAM_ID = "id";
	private static final int BUFFER_SIZE = 10240;
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd HHmmss");
	private static final ObjectMapper OM = new ObjectMapper();
	
	@JiraImport
	private ActiveObjects ao;
	
	public MappingServlet(@ComponentImport ActiveObjects ao) {
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
		String[] idList = req.getParameterValues(PARAM_ID);
		ObjectWriter ow = OM.writerFor(MapperConfigWrapper.class);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		if (idList != null) {
			for (String id : idList) {
				MapperConfigWrapper wrapper = MapperConfigUtil.getMapperConfigById(this.ao, id);
				if (wrapper != null) {
					zos.putNextEntry(new ZipEntry(wrapper.getDescription() + ".json"));
					byte[] data = ow.writeValueAsBytes(wrapper);
					zos.write(data);
					zos.closeEntry();
				}
			}
		}
		zos.close();
		String fileName = "CustomApproval Workflow Mapping " + SDF.format(new Date()) + ".zip";
		byte[] content = baos.toByteArray();
		resp.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
		resp.setContentType("application/json; charset=UTF-8");
		try (	InputStream in = new ByteArrayInputStream(content); 
		   		OutputStream out = resp.getOutputStream()) {
        	byte[] buffer = new byte[BUFFER_SIZE];
            int numBytesRead;
            while ((numBytesRead = in.read(buffer)) > 0) {
            	out.write(buffer, 0, numBytesRead);
            }
        }
	}
}
