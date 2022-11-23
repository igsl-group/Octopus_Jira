package com.igsl.configmigration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.igsl.configmigration.avatar.AvatarDTO;
import com.igsl.configmigration.customfield.CustomFieldDTO;
import com.igsl.configmigration.customfield.CustomFieldUtil;
import com.igsl.configmigration.issuesecuritylevelscheme.IssueSecurityLevelSchemeDTO;
import com.igsl.configmigration.issuesecuritylevelscheme.IssueSecurityLevelSchemeUtil;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.issuetype.IssueTypeUtil;
import com.igsl.configmigration.issuetypescheme.IssueTypeSchemeDTO;
import com.igsl.configmigration.issuetypescheme.IssueTypeSchemeUtil;
import com.igsl.configmigration.plugin.PluginDTO;
import com.igsl.configmigration.plugin.PluginUtil;
import com.igsl.configmigration.priority.PriorityDTO;
import com.igsl.configmigration.priority.PriorityUtil;
import com.igsl.configmigration.resolution.ResolutionDTO;
import com.igsl.configmigration.resolution.ResolutionUtil;
import com.igsl.configmigration.status.StatusDTO;
import com.igsl.configmigration.status.StatusUtil;
import com.igsl.configmigration.statuscategory.StatusCategoryDTO;

@SuppressWarnings("unchecked")
public class JiraConfigTypeRegistry {

	private static final Logger LOGGER = Logger.getLogger(JiraConfigTypeRegistry.class);
	
	// TODO Get this information from class loader? Spring?
	private static final Class<?>[] CLASS_LIST = new Class[] {
		// Avatar
		// Used when referenced, so Util is not added
		AvatarDTO.class,
			
		// StatusCategory
		// Used when referenced, so Util is not added
		StatusCategoryDTO.class,

		// Status
		StatusDTO.class,
		StatusUtil.class,
		
		// IssueType
		IssueTypeDTO.class, 
		IssueTypeUtil.class,
		
		// Priority
		PriorityDTO.class,
		PriorityUtil.class,
		
		// Resolution
		ResolutionDTO.class,
		ResolutionUtil.class,
		
		// IssueSecurityLevelScheme
		// IssueSecurityLevel is referenced from IssueSecurityLevelScheme
		IssueSecurityLevelSchemeDTO.class,
		IssueSecurityLevelSchemeUtil.class,
		
		// Plugin
		PluginDTO.class, 
		PluginUtil.class,
		
		// CustomFieldType		
		// CustomField
		CustomFieldDTO.class,
		CustomFieldUtil.class,
		
		// Project
		// TODO
		
		// PriorityScheme
		// TODO
		
		// IssueTypeScheme
		IssueTypeSchemeDTO.class,
		IssueTypeSchemeUtil.class,
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
