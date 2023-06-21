package com.igsl.configmigration.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtils;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtilsImpl;

/**
 * Utility class for writing/reading files from Jira data folder.
 * 
 * This is used for export data and merge report. 
 * The filename is determined by date and current user.
 * 
 * The file is a ZIP file. Assumes only contain one entry.
 */
public class PathManager {

	private static final Logger LOGGER = Logger.getLogger(PathManager.class);
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");
	
	private static Path jiraDataFolder;
	private static Path dataFolder;
	private static final String ROOT_DIRECTORY = "ConfigMigration";
	
	public static final String ZIP_EXTENSION = ".zip";
	
	public static enum Type {
		EXPORT("export"),
		MERGE_REPORT("report"),
		MERGE_DATA("report");
		private String folderName;
		private Type(String folderName) {
			this.folderName = folderName;
		}
		public String getFolderName() {
			return this.folderName;
		}
	}
	
	static {
		// Initialize
		// Note: jira.home is not reliable, can be missing
		// String homeFolder = System.getProperty("jira.home");
		ExtendedSystemInfoUtils utils = new ExtendedSystemInfoUtilsImpl(ComponentAccessor.getI18nHelperFactory().getInstance(Locale.getDefault()));
		String homeFolder = utils.getJiraHomeLocation();
		LOGGER.debug("Jira Home Folder: \"" + homeFolder + "\"");
		
		jiraDataFolder = Paths.get(homeFolder).resolve(JiraHome.DATA);
		dataFolder = jiraDataFolder.resolve(ROOT_DIRECTORY);
		try {
			Files.createDirectories(jiraDataFolder.resolve(ROOT_DIRECTORY));
			for (Type t : Type.values()) {
				Files.createDirectories(jiraDataFolder.resolve(ROOT_DIRECTORY).resolve(t.getFolderName()));
			}
		} catch (IOException ex) {
			LOGGER.error("Failed to initialize folder structure", ex);
		}
	}
	
	public static String getPathExtension(Path path) {
		if (path != null) {
			String s = path.getFileName().toString();
			int idx = s.lastIndexOf(".");
			if (idx != -1) {
				return s.substring(idx);
			}
		}
		return null;
	}

	public static String getPathFileName(Path path) {
		if (path != null) {
			String s = path.getFileName().toString();
			int idx = s.lastIndexOf(".");
			if (idx != -1) {
				s = s.substring(0, idx);
			}
			return s;
		}
		return null;
	}

	/**
	 * Create a Path that does not exist yet
	 */
	public static Path createZipFileName(Type type, String description) {
		Path result = dataFolder.resolve(type.getFolderName());
		String userName = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getDisplayName();
		String title = "";
		switch (type) {
		case EXPORT:
			title = "ExportData";
			break;
		case MERGE_REPORT:
			title = "MergeReport";
			break;
		case MERGE_DATA:
			title = "MergeData";
			break;
		}
		String desc = "";
		if (description != null && description.length() != 0) {
			desc = " - " + description;
		}
		boolean exists = false;
		long startTime = new Date().getTime();
		do {
			String dateString = SDF.format(new Date(startTime));
			result = result.resolve(title + " " + dateString + " by " + userName + desc + ".zip");
			exists = Files.exists(result);
			startTime += 1000;
		} while (exists); 
		return result;
	}
	
}
