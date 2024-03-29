package com.igsl.configmigration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class UploadServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(UploadServlet.class);
	private static final long MAX_SIZE = 100L * 1024 * 1024 * 1024;
	private static final int BUFFER_SIZE = 10240;
	
	private String getJsonUpload(InputStream in) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int cnt = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		do {
			cnt = in.read(buffer);
			if (cnt > 0) {
				baos.write(buffer, 0, cnt);
			}
		} while (cnt > 0);
		return baos.toString("UTF8");
	}
	
	private String getZipUpload(InputStream in) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipInputStream zin = new ZipInputStream(in);
		// Assume there's only a single entry
		ZipEntry entry = zin.getNextEntry();
		if (entry != null) {
			int cnt = 0;
			byte[] buffer = new byte[BUFFER_SIZE];
			do {
				cnt = zin.read(buffer);
				if (cnt > 0) {
					baos.write(buffer, 0, cnt);
				}
			} while (cnt > 0);
		}
		return baos.toString("UTF8");
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(MAX_SIZE);
		try {
			List<FileItem> fileItems = upload.parseRequest(req);
			if (fileItems.size() == 1) {
				FileItem fi = fileItems.get(0);
				String fileName = fi.getName().toUpperCase();
				LOGGER.debug("File Item: " + fileName);
				Object data = req.getSession().getAttribute(ExportAction2.SESSION_DATA);
				if (data != null && data instanceof ExportAction2SessionData) {
					ExportAction2SessionData d = (ExportAction2SessionData) data;
					InputStream in = fi.getInputStream();
					if (fileName.endsWith(".ZIP")) {
						d.upload = getZipUpload(in);
					} else {
						d.upload = getJsonUpload(in);
					}
					LOGGER.debug("Received: <" + d.upload + ">");
				}
				// Redirect back to ExportAction2
				resp.sendRedirect(
						req.getContextPath() + ExportAction2.PAGE_URL + 
						"?" + ExportAction2.PARAM_ACTION + "=" + ExportAction2.ACTION_IMPORT);
				LOGGER.debug("ApacheCommons FileUpload: File received");
			} else {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				LOGGER.error("ApacheCommons FileUpload Error: More than 1 uploaded file found");
			}
		} catch (Exception ex) {
			LOGGER.error("ApacheCommons FileUpload Error", ex);
		}
	}

}
