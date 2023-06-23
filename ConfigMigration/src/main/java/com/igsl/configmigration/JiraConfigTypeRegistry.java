package com.igsl.configmigration;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.atlassian.mail.server.MailServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.igsl.configmigration.applicationrole.ApplicationRoleUtil;
import com.igsl.configmigration.applicationuser.ApplicationUserUtil;
import com.igsl.configmigration.avatar.AvatarUtil;
import com.igsl.configmigration.customfield.CustomFieldUtil;
import com.igsl.configmigration.customfieldsearcher.CustomFieldSearcherUtil;
import com.igsl.configmigration.customfieldtype.CustomFieldTypeUtil;
import com.igsl.configmigration.defaultvalueoperations.DefaultValueOperationsUtil;
import com.igsl.configmigration.eventtype.EventTypeUtil;
import com.igsl.configmigration.field.FieldUtil;
import com.igsl.configmigration.fieldconfig.FieldConfigUtil;
import com.igsl.configmigration.fieldconfigscheme.FieldConfigSchemeUtil;
import com.igsl.configmigration.fieldlayout.FieldLayoutUtil;
import com.igsl.configmigration.fieldlayoutscheme.FieldLayoutSchemeUtil;
import com.igsl.configmigration.fieldscreen.FieldScreenUtil;
import com.igsl.configmigration.globalpermission.GlobalPermissionsUtil;
import com.igsl.configmigration.group.GroupUtil;
import com.igsl.configmigration.insight.ObjectBeanUtil;
import com.igsl.configmigration.insight.ObjectSchemaBeanUtil;
import com.igsl.configmigration.issuesecuritylevelscheme.IssueSecurityLevelSchemeUtil;
import com.igsl.configmigration.issuetype.IssueTypeUtil;
import com.igsl.configmigration.issuetypescheme.IssueTypeSchemeUtil;
import com.igsl.configmigration.issuetypescreencheme.IssueTypeScreenSchemeUtil;
import com.igsl.configmigration.mailserver.MailServerUtil;
import com.igsl.configmigration.notificationscheme.NotificationSchemeUtil;
import com.igsl.configmigration.permissionscheme.PermissionSchemeUtil;
import com.igsl.configmigration.plugin.PluginUtil;
import com.igsl.configmigration.priority.PriorityUtil;
import com.igsl.configmigration.priorityscheme.PrioritySchemeUtil;
import com.igsl.configmigration.project.ProjectUtil;
import com.igsl.configmigration.projectcategory.ProjectCategoryUtil;
import com.igsl.configmigration.projectcomponent.ProjectComponentUtil;
import com.igsl.configmigration.projectrole.ProjectRoleUtil;
import com.igsl.configmigration.resolution.ResolutionUtil;
import com.igsl.configmigration.status.StatusUtil;
import com.igsl.configmigration.version.VersionUtil;
import com.igsl.configmigration.workflow.WorkflowUtil;
import com.igsl.configmigration.workflowscheme.WorkflowSchemeUtil;

@SuppressWarnings("unchecked")
public class JiraConfigTypeRegistry {

	private static ObjectMapper OM = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	/**
	 * This list is to mandate the order of the utils during export and import.
	 * Any Util class not listed here will be sorted to the bottom of the list in ascending alphabetical order.
	 */
	private static List<Class<? extends JiraConfigUtil>> UTIL_ORDER = Arrays.asList(
			// Simpler objects
			GroupUtil.class,
			AvatarUtil.class,
			StatusUtil.class,
			IssueTypeUtil.class,
			PriorityUtil.class,
			ResolutionUtil.class,
			GlobalPermissionsUtil.class,
			MailServerUtil.class,
			
			// Insight
			ObjectBeanUtil.class,
			ObjectSchemaBeanUtil.class,
			
			// Plugin
			PluginUtil.class,

			// Role and User - later than Group
			ApplicationRoleUtil.class,
			ApplicationUserUtil.class,
			
			// Custom field
			CustomFieldUtil.class,
			
			EventTypeUtil.class,
			FieldUtil.class,
			FieldConfigUtil.class,
			
			// Screen - later then fields and custom fields
			FieldScreenUtil.class,
			FieldLayoutUtil.class, 
			
			// Project - later than most
			VersionUtil.class,
			ProjectRoleUtil.class,
			ProjectCategoryUtil.class,
			ProjectComponentUtil.class,
			ProjectUtil.class,

			// Schemes - come last
			FieldConfigSchemeUtil.class,
			IssueSecurityLevelSchemeUtil.class,
			FieldLayoutSchemeUtil.class,
			IssueTypeSchemeUtil.class,
			IssueTypeScreenSchemeUtil.class,
			NotificationSchemeUtil.class,
			PermissionSchemeUtil.class,
			PrioritySchemeUtil.class,
			
			// Workflow - after most, before workflow scheme
			WorkflowUtil.class,
			WorkflowSchemeUtil.class
			);
	private static List<String> orderedList = new ArrayList<>();
	private static List<String> unorderedList = new ArrayList<>();
	
	private static int getOrder(String s) {
		if (orderedList.contains(s)) {
			return orderedList.indexOf(s);
		}
		if (unorderedList.contains(s)) {
			return orderedList.size() + unorderedList.indexOf(s);
		}
		return Integer.MAX_VALUE;
	}
	
	private static class UtilComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			Logger logger = Logger.getLogger(LOGGER_NAME);
			int i1 = getOrder(o1);
			int i2 = getOrder(o2);
			int r = Integer.compare(i1, i2);
			return r;
		}
	}
	
	private static final String LOGGER_NAME = "com.igsl.configmigration.JiraConfigTypeRegistry";
	
	private static final String CLASS_SUFFIX = ".class";
	
	// Full list of DTO class names
	private static List<String> ITEM_NAMES = new ArrayList<>();
	
	// Full list of Util class names
	private static List<String> UTIL_NAMES = new ArrayList<>();
	
	// Maps canonical name to Class for deserialization
	private static Map<String, Class<? extends JiraConfigDTO>> DTO_MAP = new LinkedHashMap<>();
	private static Map<String, Class<? extends JiraConfigUtil>> UTIL_MAP = new HashMap<>();
	
	// Stores list of Util instances, key is Util class name
	// Order of key is maintained by UTIL_ORDER
	private static Map<String, JiraConfigUtil> UTIL_INSTANCE_MAP = new TreeMap<>(new UtilComparator());

	// Stores list of DTO class names, key is corresponding Jira class name
	private static Map<Class<?>, Class<? extends JiraConfigDTO>> DTO_INSTANCE_MAP = new HashMap<>(); 
	
	/**
	 * Get JiraConfigDTO class corresponding to a Jira class.
	 * @param jiraClassName Jira class name, e.g. ApplicationUser.class.getCanonicalName().
	 * @return JiraConfigDTO class, null if no match.
	 */
	public static Class<? extends JiraConfigDTO> getDTOClass(Class<?> jiraClass) {
		if (jiraClass != null) {
			for (Map.Entry<Class<?>, Class<? extends JiraConfigDTO>> entry : DTO_INSTANCE_MAP.entrySet()) {
				if (entry.getKey().isAssignableFrom(jiraClass)) {
					return entry.getValue();
				}
			}
		}
		return null;
	}
	
	public static Map<Class<?>, Class<? extends JiraConfigDTO>> getConfigDTOMap() {
		return Collections.unmodifiableMap(DTO_INSTANCE_MAP);
	}
	
	/**
	 * Get Util class name to DTO class.
	 * @return
	 */
	public static Map<String, Class<? extends JiraConfigDTO>> getUtilToDTOMap() {
		return Collections.unmodifiableMap(DTO_MAP);
	}
	
	/**
	 * Get Util class name to Util class.
	 * @return
	 */
	public static Map<String, Class<? extends JiraConfigUtil>> getConfigUtilMap() {
		return Collections.unmodifiableMap(UTIL_MAP);
	}
	
	/**
	 * Get Util instance list.
	 * @param publicOnly Get public Util only.
	 * @return Collection of JiraConfigUtil instances.
	 */
	public static Collection<JiraConfigUtil> getConfigUtilList(boolean publicOnly) {
		Logger logger = Logger.getLogger(LOGGER_NAME);
		Collection<JiraConfigUtil> result = new ArrayList<JiraConfigUtil>();
		for (Map.Entry<String, JiraConfigUtil> entry : UTIL_INSTANCE_MAP.entrySet()) {
			if (publicOnly) {
				if (entry.getValue().isVisible()) {
					result.add(entry.getValue());
				}
			} else {
				result.add(entry.getValue());
			}
		}
		return Collections.unmodifiableCollection(result);
	}
	
	/**
	 * Get JiraConfigUtil based on Util class name.
	 * @param className
	 * @return
	 */
	public static JiraConfigUtil getConfigUtil(String className) {
		if (UTIL_INSTANCE_MAP.containsKey(className)) {
			return UTIL_INSTANCE_MAP.get(className);
		}
		return null;
	}
	public static JiraConfigUtil getConfigUtil(Class<? extends JiraConfigUtil> utilClass) {
		if (utilClass != null) {
			return getConfigUtil(utilClass.getCanonicalName());
		}
		return null;
	}
	
	/**
	 * Get JiraConfigUtil class for provided object (if it is JiraConfigDTO). 
	 * Caller should check if item is array, Collection or Map first, if so, handle each item separately.
	 * Note that not all JiraConfigDTO has a JiraConfigUtil, some are only referenced by other DTOs. 
	 * 
	 * @param item
	 * @return JiraConfigUtil instance for provided JiraConfigDTO object. Null if no match is found.
	 */
	public static JiraConfigUtil checkConfigUtil(Object item) {
		if (item != null && item instanceof JiraConfigDTO) {
			JiraConfigDTO dto = (JiraConfigDTO) item;
			return getConfigUtil(dto.getUtilClass().getCanonicalName());
		}
		return null;
	}
	
	static {
		Logger logger = Logger.getLogger(LOGGER_NAME);
		// Convert class objects into canonical names
		for (Class<? extends JiraConfigUtil> cls : UTIL_ORDER) {
			orderedList.add(cls.getCanonicalName());
		}
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
									if (!Modifier.isAbstract(cls.getModifiers())) {
										if (configItemClass.isAssignableFrom(cls) && 
											!className.equals(JiraConfigDTO.class.getCanonicalName())) {
											ITEM_NAMES.add(cls.getCanonicalName());
										} else if (
											utilClass.isAssignableFrom(cls) && 
											!className.equals(JiraConfigUtil.class.getCanonicalName())) {
											UTIL_NAMES.add(cls.getCanonicalName());
										}
									}
								} catch (Throwable ex) {
									// Ignore
									logger.error(
											"Registry error: " + 
											ex.getClass().getCanonicalName() + ": " + 
											ex.getMessage());
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
				DTO_MAP.put(cls.getCanonicalName(), (Class<? extends JiraConfigDTO>) cls); 
				JiraConfigDTO dto = (JiraConfigDTO) cls.newInstance();
				if (dto.getJiraClass() != null) {
					DTO_INSTANCE_MAP.put(dto.getJiraClass(), (Class<? extends JiraConfigDTO>) cls);
				}
			} catch (Throwable t) {
				logger.error("Failed to load JiraConfigItem for " + s, t);
			}
		}
		// Determine util order before adding items to UTIL_MAP 
		for (String s : UTIL_NAMES) {
			if (!orderedList.contains(s)) {
				unorderedList.add(s);
			}
		}
		Collections.sort(unorderedList);
		for (String s : UTIL_NAMES) {
			try {
				Class<?> cls = cloader.loadClass(s);
				UTIL_MAP.put(s, (Class<? extends JiraConfigUtil>) cls); 
				UTIL_INSTANCE_MAP.put(s, (JiraConfigUtil) cls.newInstance());
			} catch (Throwable t) {
				logger.error("Failed to load JiraConfigUtil for " + s, t);
			}
		}
	}
}
