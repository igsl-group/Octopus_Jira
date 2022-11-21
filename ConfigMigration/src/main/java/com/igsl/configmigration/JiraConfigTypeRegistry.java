package com.igsl.configmigration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.igsl.configmigration.avatar.AvatarConfigItem;
import com.igsl.configmigration.issuetype.IssueTypeConfigItem;
import com.igsl.configmigration.issuetype.IssueTypeConfigUtil;
import com.igsl.configmigration.issuetypescheme.IssueTypeSchemeConfigItem;
import com.igsl.configmigration.issuetypescheme.IssueTypeSchemeConfigUtil;
import com.igsl.configmigration.priority.PriorityConfigItem;
import com.igsl.configmigration.priority.PriorityConfigUtil;
import com.igsl.configmigration.resolution.ResolutionConfigItem;
import com.igsl.configmigration.resolution.ResolutionConfigUtil;
import com.igsl.configmigration.status.StatusConfigItem;
import com.igsl.configmigration.status.StatusConfigUtil;
import com.igsl.configmigration.statuscategory.StatusCategoryConfigItem;

@SuppressWarnings("unchecked")
public class JiraConfigTypeRegistry {

	private static final Logger LOGGER = Logger.getLogger(JiraConfigTypeRegistry.class);
	
	// TODO Get this information from class loader? Spring?
	private static final Class<?>[] CLASS_LIST = new Class[] {
		// Avatar
		// Used when referenced, so Util is not added
		AvatarConfigItem.class,
			
		// StatusCategory
		// Used when referenced, so Util is not added
		StatusCategoryConfigItem.class,

		// Status
		StatusConfigItem.class,
		StatusConfigUtil.class,
		
		// IssueType
		IssueTypeConfigItem.class, 
		IssueTypeConfigUtil.class,
		
		// Priority
		PriorityConfigItem.class,
		PriorityConfigUtil.class,
		
		// Resolution
		ResolutionConfigItem.class,
		ResolutionConfigUtil.class,
		
		// Project
		// TODO
		
		// PriorityScheme
		// TODO
		
		// IssueTypeScheme
		IssueTypeSchemeConfigItem.class,
		IssueTypeSchemeConfigUtil.class
	};
	
	private static Map<String, Class<? extends JiraConfigItem>> CONFIG_ITEM = new LinkedHashMap<>();
	private static Map<String, Class<? extends JiraConfigUtil>> CONFIG_UTIL = new LinkedHashMap<>();
	private static Map<String, JiraConfigUtil> CONFIG_UTIL_LIST = new LinkedHashMap<>();
	
	public static Map<String, Class<? extends JiraConfigItem>> getConfigItemMap() {
		return Collections.unmodifiableMap(CONFIG_ITEM);
	}
	
	public static Map<String, Class<? extends JiraConfigUtil>> getConfigUtilMap() {
		return Collections.unmodifiableMap(CONFIG_UTIL);
	}
	
	public static Collection<JiraConfigUtil> getConfigUtilList() {
		return Collections.unmodifiableCollection(CONFIG_UTIL_LIST.values());
	}
	
	public static JiraConfigUtil getConfigUtil(String key) {
		if (CONFIG_UTIL.containsKey(key)) {
			try {
				return CONFIG_UTIL.get(key).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				LOGGER.error("Failed to instantiate JiraConfigUtil instance for " + key, e);
			}
		}
		return null;
	}
	
	static {
		try {
			for (Class<?> cls : CLASS_LIST) {
				if (JiraConfigItem.class.isAssignableFrom(cls)) {
					CONFIG_ITEM.put(cls.getCanonicalName(), (Class<? extends JiraConfigItem>) cls);
				} else if (JiraConfigUtil.class.isAssignableFrom(cls)) {
					CONFIG_UTIL.put(cls.getCanonicalName(), (Class<? extends JiraConfigUtil>) cls);
					try {
						CONFIG_UTIL_LIST.put(cls.getCanonicalName(), (JiraConfigUtil) cls.newInstance());
					} catch (InstantiationException | IllegalAccessException e) {
						LOGGER.error("Failed to instantiate JiraConfigUtil instance for " + cls, e);
					}
				}
			}
		} catch (Exception ex) {
			// TODO
			PrintStream ps = null;
			try {
				ps = new PrintStream(new FileOutputStream("C:\\KC\\log.txt", true));
				ex.printStackTrace(ps);
			} catch (IOException ioex) {
				// Ignore
			} finally {
				if (ps != null) {
					ps.close();
				}
			}
		}
	}
}
