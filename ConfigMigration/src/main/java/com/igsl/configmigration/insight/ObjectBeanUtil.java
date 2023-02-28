package com.igsl.configmigration.insight;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

/*
 * REST API to export Insight objects and schema
 * https://community.atlassian.com/t5/Jira-Service-Management/Automation-of-Object-schema-export/td-p/750997
 */

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ObjectBeanUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ObjectBeanUtil.class);
	private static final String CLASSNAME_OBJECT_FACADE = "com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade";
	private static final String CLASSNAME_OBJECT_SCHEMA_FACADE = "com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectSchemaFacade";
	private static final String CLASSNAME_OBJECT_SCHEMA_BEAN = "com.riadalabs.jira.plugins.insight.services.model.ObjectSchemaBean";
	private static final String CLASSNAME_OBJECT_TYPE_FACADE = "com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeFacade";
	private static final String CLASSNAME_OBJECT_TYPE_BEAN = "com.riadalabs.jira.plugins.insight.services.model.ObjectTypeBean";
	private static final String CLASSNAME_IQL_FACADE = "com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade";
	
	private static Class<?> loadClass(String className) {
		try {
			return ComponentAccessor.getPluginAccessor().getClassLoader().loadClass(className);
		} catch (Exception ex) {
			LOGGER.warn("Failed to load Insight class " + className, ex);
			return null;
		}
	}
	
	public static boolean checkInsight() {
		Class<?> cls = loadClass(CLASSNAME_OBJECT_FACADE);
		if (cls != null) {
			Object facade = ComponentAccessor.getOSGiComponentInstanceOfType(cls);
			return (facade != null);
		}
		return false;
	}
	
	public static List<Object> findObjectSchemaBeans() {
		List<Object> result = new ArrayList<>();
		Class<?> cls = loadClass(CLASSNAME_OBJECT_SCHEMA_FACADE);
		if (cls != null) {
			Object facade = ComponentAccessor.getOSGiComponentInstanceOfType(cls);
			try {
				Method m = cls.getMethod("findObjectSchemaBeans");
				Object r = m.invoke(facade);
				if (r instanceof List) {
					List<?> list = (List<?>) r;
					for (Object item : list) {
						result.add(item);
					}
				}
			} catch (Exception nfex) {
				LOGGER.error("Failed to get ObjectSchemaBeans", nfex);
			}
		}
		return result;
	}
	
	@JsonIgnore
	public static Integer getObjectSchemaId(Object o) {
		Class<?> cls = loadClass(CLASSNAME_OBJECT_SCHEMA_BEAN);
		if (cls != null) {
			try {
				Method m = cls.getMethod("getId");
				Object r = m.invoke(o);
				if (r instanceof Integer) {
					return (Integer) r;
				}
			} catch (Exception nfex) {
				LOGGER.error("Failed to get ObjectSchemaBean getId", nfex);
			}
		}
		return null;
	}
	
	@JsonIgnore
	public static String getObjectSchemaKey(Object o) {
		Class<?> cls = loadClass(CLASSNAME_OBJECT_SCHEMA_BEAN);
		if (cls != null) {
			try {
				Method m = cls.getMethod("getObjectSchemaKey");
				Object r = m.invoke(o);
				if (r instanceof String) {
					return (String) r;
				}
			} catch (Exception nfex) {
				LOGGER.error("Failed to get ObjectSchemaBean getObjectSchemaKey", nfex);
			}
		}
		return null;
	}

	public static List<Object> findObjectTypeBeansFlat(int schemaId) {
		List<Object> result = new ArrayList<>();
		Class<?> cls = loadClass(CLASSNAME_OBJECT_TYPE_FACADE);
		if (cls != null) {
			Object facade = ComponentAccessor.getOSGiComponentInstanceOfType(cls);
			try {
				Method m = cls.getMethod("findObjectTypeBeansFlat", int.class);
				Object r = m.invoke(facade, schemaId);
				if (r instanceof List) {
					List<?> list = (List<?>) r;
					for (Object item : list) {
						result.add(item);
					}
				}
			} catch (Exception nfex) {
				LOGGER.error("Failed to get getObjectTypeBeans", nfex);
			}
		}
		return result;
	}
	
	@JsonIgnore
	public static Integer getObjectTypeId(Object o) {
		Class<?> cls = loadClass(CLASSNAME_OBJECT_TYPE_BEAN);
		if (cls != null) {
			try {
				Method m = cls.getMethod("getId");
				Object r = m.invoke(o);
				if (r instanceof Integer) {
					return (Integer) r;
				}
			} catch (Exception nfex) {
				LOGGER.error("Failed to get getObjectTypeBean getId", nfex);
			}
		}
		return null;
	}

	@JsonIgnore
	public static String getObjectTypeName(Object o) {
		Class<?> cls = loadClass(CLASSNAME_OBJECT_TYPE_BEAN);
		if (cls != null) {
			try {
				Method m = cls.getMethod("getName");
				Object r = m.invoke(o);
				if (r instanceof String) {
					return (String) r;
				}
			} catch (Exception nfex) {
				LOGGER.error("Failed to get getObjectTypeBean getName", nfex);
			}
		}
		return null;
	}

	public static List<Object> findObjectsByIQL(String iql) {
		List<Object> result = new ArrayList<>();
		Class<?> cls = loadClass(CLASSNAME_IQL_FACADE);
		if (cls != null) {
			Object facade = ComponentAccessor.getOSGiComponentInstanceOfType(cls);
			try {
				Method m = cls.getMethod("findObjects", String.class);
				Object r = m.invoke(facade, iql);
				if (r instanceof List) {
					List<?> list = (List<?>) r;
					for (Object item : list) {
						result.add(item);
					}
				}
			} catch (Exception nfex) {
				LOGGER.error("Failed to get IQLFacade findObjects", nfex);
			}
		}
		return result;
	}
	
	@Override
	public String getName() {
		return "Insight";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		return findByUniqueKey(id, params);
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		Map<String, JiraConfigDTO> list = search(null, params);
		return list.get(uniqueKey);
	}

	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		ObjectBeanDTO original = null;
		if (oldItem != null) {
			original = (ObjectBeanDTO) oldItem;
		} else {
			original = (ObjectBeanDTO) findByDTO(newItem);
		}		
		// TODO Implement using Insight REST API
		throw new Exception("Not implemented");
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ObjectBeanDTO.class;
	}

	@Override
	public boolean isVisible() {
		// Visible only when ObjectFacade is available
		return checkInsight();
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		if (checkInsight()) {
			List<?> objects = findObjectsByIQL("Name like \"\"");
			for (Object ob : objects) {
				ObjectBeanDTO item = new ObjectBeanDTO();
				item.setJiraObject(ob);
				if (!matchFilter(item, filter)) {
					continue;
				}
				result.put(item.getUniqueKey(), item);
			}
		}
		return result;
	}

}
