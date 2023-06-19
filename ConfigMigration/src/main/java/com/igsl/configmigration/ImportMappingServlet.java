package com.igsl.configmigration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.igsl.configmigration.workflow.mapper.MapperConfigUtil;
import com.igsl.configmigration.workflow.mapper.WorkflowMappingManager;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigWrapper;

public class ImportMappingServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ImportMappingServlet.class);
	private static final long MAX_SIZE = 100L * 1024 * 1024 * 1024;
	private static final int BUFFER_SIZE = 10240;
	private static final ObjectMapper OM = new ObjectMapper();
	
	private ActiveObjects ao;
	
	public ImportMappingServlet(@ComponentImport ActiveObjects ao) {
		this.ao = ao;
	}
	
	private List<MapperConfigWrapper> getJsonUpload(InputStream in) throws Exception {
		List<MapperConfigWrapper> result = new ArrayList<>();
		ObjectReader or = OM.readerFor(MapperConfigWrapper.class);
		MapperConfigWrapper wrapper = or.readValue(in);
		if (wrapper != null) {
			result.add(wrapper);
		}
		return result;
	}
	
	private List<MapperConfigWrapper> getZipUpload(InputStream in) throws Exception {
		List<MapperConfigWrapper> result = new ArrayList<>();
		ObjectReader or = OM.readerFor(MapperConfigWrapper.class);
		ZipInputStream zis = new ZipInputStream(in);
		ZipEntry entry = zis.getNextEntry();
		while (entry != null) {
			MapperConfigWrapper wrapper = or.readValue(zis);
			if (wrapper != null) {
				result.add(wrapper);
			}
			// Next
			entry = zis.getNextEntry();
		}
		return result;
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(MAX_SIZE);
		try {
			ObjectReader or = OM.readerFor(MapperConfigWrapper.class);
			List<FileItem> fileItems = upload.parseRequest(req);
			if (fileItems.size() == 1) {
				FileItem fi = fileItems.get(0);
				List<MapperConfigWrapper> list;
				String fileName = fi.getName().toLowerCase();
				if (fileName.endsWith(".json")) {
					list = getJsonUpload(fi.getInputStream());
				} else if (fileName.endsWith(".zip")) {
					list = getZipUpload(fi.getInputStream());
				} else {
					list = new ArrayList<>();
				}
				for (MapperConfigWrapper wrapper : list) {
					MapperConfigWrapper existing = MapperConfigUtil.getMapperConfigByName(ao, wrapper.getDescription());
					if (existing != null) {
						wrapper.copyTo(existing);
						MapperConfigUtil.saveMapperConfig(ao, existing);
					} else {
						MapperConfigUtil.saveMapperConfig(ao, wrapper);
					}
				}
			}
		} catch (Exception ex) {
			LOGGER.error("ApacheCommons FileUpload Error", ex);
		}
		// Redirect back 
		resp.sendRedirect(req.getContextPath() + WorkflowMappingManager.PAGE_URL);
	}

}
