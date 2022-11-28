package com.igsl.configmigration;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igsl.configmigration.customfield.CustomFieldUtil;
import com.igsl.configmigration.insight.ObjectBeanUtil;
import com.igsl.configmigration.insight.ObjectSchemaBeanUtil;
import com.igsl.configmigration.issuesecuritylevelscheme.IssueSecurityLevelSchemeUtil;
import com.igsl.configmigration.issuetype.IssueTypeUtil;
import com.igsl.configmigration.issuetypescheme.IssueTypeSchemeUtil;
import com.igsl.configmigration.plugin.PluginUtil;
import com.igsl.configmigration.priority.PriorityUtil;
import com.igsl.configmigration.resolution.ResolutionUtil;
import com.igsl.configmigration.status.StatusUtil;

@SuppressWarnings("unchecked")
public class JiraConfigTypeRegistry {

	/**
	 * This list is to mandate the order of the utils during export and import.
	 */
	private static List<String> UTIL_ORDER = Arrays.asList(
			StatusUtil.class.getCanonicalName(),
			IssueTypeUtil.class.getCanonicalName(),
			PriorityUtil.class.getCanonicalName(),
			ResolutionUtil.class.getCanonicalName(),
			ObjectSchemaBeanUtil.class.getCanonicalName(),
			ObjectBeanUtil.class.getCanonicalName(),
			IssueSecurityLevelSchemeUtil.class.getCanonicalName(),
			PluginUtil.class.getCanonicalName(),
			CustomFieldUtil.class.getCanonicalName(),
			IssueTypeSchemeUtil.class.getCanonicalName()
			);
	
	private static class UtilComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			int i1 = -1;
			int i2 = -1;
			if (UTIL_ORDER.contains(o1)) {
				i1 = UTIL_ORDER.indexOf(o1);
			}
			if (UTIL_ORDER.contains(o2)) {
				i2 = UTIL_ORDER.indexOf(o2);
			}
			return Integer.compare(i1, i2);
		}
	}
	
	private static final String LOGGER_NAME = "com.igsl.configmigration.JiraConfigTypeRegistry";
	
	private static final String CLASS_SUFFIX = ".class";
	
	// Used to store class names
	private static List<String> ITEM_NAMES = new ArrayList<>();
	private static List<String> UTIL_NAMES = new ArrayList<>();
	
	// Maps canonical name to Class for deserialization
	private static Map<String, Class<? extends JiraConfigDTO>> ITEM_LIST = new LinkedHashMap<>();
	private static Map<String, Class<? extends JiraConfigUtil>> UTIL_LIST = new TreeMap<>(new UtilComparator());
	
	// Stores list of Util instances
	private static Map<String, JiraConfigUtil> UTIL_INSTANCE_LIST = new LinkedHashMap<>();

	public static Map<String, Class<? extends JiraConfigDTO>> getConfigItemMap() {
		return Collections.unmodifiableMap(ITEM_LIST);
	}
	
	public static Map<String, Class<? extends JiraConfigUtil>> getConfigUtilMap() {
		return Collections.unmodifiableMap(UTIL_LIST);
	}
	
	public static Collection<JiraConfigUtil> getConfigUtilList() {
		return getConfigUtilList(true);
	}
	public static Collection<JiraConfigUtil> getConfigUtilList(boolean publicOnly) {
		if (publicOnly) {
			Collection<JiraConfigUtil> result = new ArrayList<JiraConfigUtil>();
			for (JiraConfigUtil util : UTIL_INSTANCE_LIST.values()) {
				if (util.isPublic()) {
					result.add(util);
				}
			}
			return Collections.unmodifiableCollection(result);
		} else {
			return Collections.unmodifiableCollection(UTIL_INSTANCE_LIST.values());
		}
	}
	
	public static JiraConfigUtil getConfigUtil(String className) {
		if (UTIL_INSTANCE_LIST.containsKey(className)) {
			return UTIL_INSTANCE_LIST.get(className);
		}
		return null;
	}

	// Caller should check if item is array, Collection or Map first, if so, handle each item separately.
	public static JiraConfigUtil checkConfigUtil(Object item) {
		if (item != null && item instanceof JiraConfigDTO) {
			JiraConfigDTO dto = (JiraConfigDTO) item;
			return getConfigUtil(dto.getUtilClass().getCanonicalName());
		}
		return null;
	}
	
	static {
		Logger logger = Logger.getLogger(LOGGER_NAME);
		// Get JiraConfigItme and JiraConfigUtil class list from JAR file
		try {
			ProtectionDomain pd = ExportAction.class.getProtectionDomain();
			if (pd != null) {
				CodeSource cs = pd.getCodeSource();
				if (cs != null) {
					URL url = cs.getLocation();
					URL[] urls = new URL[] {url};
					ObjectMapper OM = new ObjectMapper();
					ClassLoader cloader = new URLClassLoader(urls);
					Class<?> configItemClass = cloader.loadClass(JiraConfigDTO.class.getCanonicalName());
					Class<?> utilClass = cloader.loadClass(JiraConfigUtil.class.getCanonicalName());
					try (ZipInputStream in = new ZipInputStream(url.openStream())) {
						while (true) {
							ZipEntry entry = in.getNextEntry();
							if (entry == null) {
								break;
							}
							String entryName = entry.getName();
							if (entryName.toLowerCase().endsWith(CLASS_SUFFIX)) {
								String className = entryName
										.substring(0, entryName.length() - CLASS_SUFFIX.length())
										.replaceAll("/", ".");
								try {
									Class<?> cls = cloader.loadClass(className);
									if (configItemClass.isAssignableFrom(cls) && 
										!className.equals(JiraConfigDTO.class.getCanonicalName())) {
										ITEM_NAMES.add(cls.getCanonicalName());
									} else if (
										utilClass.isAssignableFrom(cls) && 
										!className.equals(JiraConfigUtil.class.getCanonicalName())) {
										UTIL_NAMES.add(cls.getCanonicalName());
									}
								} catch (Throwable ex) {
									// Ignore
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error: " + ex.getClass().getCanonicalName() + ": " + ex.getMessage() + "\n");
		}	
		// Load the class with real classloader
		ClassLoader cloader = JiraConfigTypeRegistry.class.getClassLoader();
		for (String s : ITEM_NAMES) {
			try {
				Class<?> cls = cloader.loadClass(s);
				ITEM_LIST.put(cls.getCanonicalName(), (Class<? extends JiraConfigDTO>) cls); 
			} catch (Throwable t) {
				logger.error("Failed to load JiraConfigItem for " + s, t);
			}
		}
		for (String s : UTIL_NAMES) {
			try {
				Class<?> cls = cloader.loadClass(s);
				UTIL_LIST.put(cls.getCanonicalName(), (Class<? extends JiraConfigUtil>) cls); 
			} catch (Throwable t) {
				logger.error("Failed to load JiraConfigUtil for " + s, t);
			}
		}
		for (Class<? extends JiraConfigUtil> cls : UTIL_LIST.values()) {
			try {
				UTIL_INSTANCE_LIST.put(
						cls.getCanonicalName(), 
						(JiraConfigUtil) cls.newInstance());
			} catch (Throwable ex) {
				logger.error(
						"Failed to instantiate JiraConfigUtil instance for " + cls, 
						ex);
			}
		}
	}
}
