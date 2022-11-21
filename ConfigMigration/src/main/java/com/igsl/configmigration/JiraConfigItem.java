package com.igsl.configmigration;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/*
 * Class to represent a Jira configuration item to be exported/imported.
 * 
 * The purpose is to encapsulate a Jira object class to: 
 * 1. Make serialization possible (through the use of implementation member and custom deserializer).
 * 2. Make comparison (without internal ID) possible.
 * 3. Make generic display possible (through getMap() API).
 * 
 * Generics is not used for the Jira object class to simplify deserialization.
 * If generics is used, we will need custom deserialization all around.
 */
@JsonDeserialize(using = JiraConfigItemDeserializer.class)
@JsonIgnoreProperties(value={"implementation"}, allowGetters=true)
public abstract class JiraConfigItem {

	private static final Logger LOGGER = Logger.getLogger(JiraConfigItem.class);
	protected static final ObjectMapper OM = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	private static final List<String> EXCLUDE_METHODS = Arrays.asList(
			"getClass",
			"getMap",
			"getJiraObject",
			"isSelected",
			"getUniqueKey",
			"getInternalId"
		);
	
	protected abstract List<String> getCompareMethods();
	
	private static final String DIFFERENCE_DELIMITER = ".";
	public static final String DIFFERENCE_WILDCARD = DIFFERENCE_DELIMITER + "*";
	public static final String DIFFERENCE_INDEX = DIFFERENCE_DELIMITER + "#";
	
	public static final List<String> getDifferences(String title, JiraConfigItem o1, JiraConfigItem o2) {
		List<String> result = new ArrayList<>();
		if (o1 != null && 
			o2 != null && 
			o1.getClass().equals(o2.getClass())) {
			List<String> compareMethods = o1.getCompareMethods();
			for (Method m : o1.getClass().getMethods()) {
				if (Modifier.isPublic(m.getModifiers()) && 
					m.getParameterCount() == 0 && 
					(m.getName().startsWith("get") || m.getName().startsWith("is")) && 
					compareMethods.contains(m.getName()) && 
					!EXCLUDE_METHODS.contains(m.getName())) {
					try {
						Object v1 = m.invoke(o1);
						Object v2 = m.invoke(o2);
						result.addAll(getDifferences(title + DIFFERENCE_DELIMITER + m.getName(), v1, v2));
					} catch (Exception ex) {
						LOGGER.error("Failed to compare JiraConfigItem", ex);
						result.add(title + DIFFERENCE_DELIMITER + m.getName());
					}
				}
			}
		} else if ((o1 != null && o2 == null) || (o1 == null && o2 != null)) {
			result.add(title + DIFFERENCE_WILDCARD);
		}
		return result;
	}
	
	public static final List<String> getDifferences(String title, Map<?, ?> m1, Map<?, ?> m2) {
		List<String> result = new ArrayList<>();
		if (m1 != null && m2 != null) {
			for (Map.Entry<?, ?> entry : m1.entrySet()) {
				if (!m2.containsKey(entry.getKey())) {
					result.add(title + DIFFERENCE_DELIMITER + String.valueOf(entry.getKey()));
				} else {
					result.addAll(
							getDifferences(
									title + DIFFERENCE_DELIMITER + entry.getKey(), 
									entry.getValue(), 
									m2.get(entry.getKey())));
				}
			}
		} else if ((m1 != null && m2 == null) || (m1 == null && m2 != null)) {
			result.add(title + DIFFERENCE_WILDCARD);
		}
		return result;
	}
	
	public static final List<String> getDifferences(String title, Object[] o1, Object[] o2) {
		List<String> result = new ArrayList<>();
		if (o1 != null && o2 != null) {
			if (o1.length != o2.length) {
				result.add(title + DIFFERENCE_INDEX); 
			} else {
				for (int i = 0; i < o1.length; i++) {
					result.addAll(getDifferences(title + DIFFERENCE_INDEX + i, o1[i], o2[i]));
				}
			}
		} else if ((o1 != null && o2 == null) || (o1 == null && o2 != null)) {
			result.add(title + DIFFERENCE_WILDCARD);
		}
		return result;
	}
	
	public static final List<String> getDifferences(String title, Collection<?> c1, Collection<?> c2) {
		List<String> result = new ArrayList<>();
		if (c1 != null && c2 != null) {
			if (c1.size() != c2.size()) {
				result.add(title + DIFFERENCE_INDEX); 
			} else {
				// Compare items (in order)
				Object[] a1 = c1.toArray();
				Object[] a2 = c2.toArray();
				for (int i = 0; i < c1.size(); i++) {
					result.addAll(getDifferences(title + DIFFERENCE_INDEX + i, a1[i], a2[i]));
				}
			}
		} else if ((c1 != null && c2 == null) || (c1 == null && c2 != null)) {
			result.add(title + DIFFERENCE_WILDCARD);
		}
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final List<String> getDifferences(String title, Object o1, Object o2) {
		List<String> result = new ArrayList<>();
		if (o1 != null && o2 != null) {
			Class<?> cls1 = o1.getClass();
			Class<?> cls2 = o2.getClass();
			if (String.class.isAssignableFrom(cls1) && 
				String.class.isAssignableFrom(cls2)) {
				LOGGER.debug("Compare: " + title + " as string");
				if (((String) o1).compareTo((String) o2) != 0) {
					result.add(title);
				}
			} else if (	Long.class.isAssignableFrom(cls1) && 
						Long.class.isAssignableFrom(cls2)) {
				LOGGER.debug("Compare: " + title + " as long");
				if (((Long) o1).compareTo((Long) o2) != 0) {
					result.add(title);
				}
			} else if (	JiraConfigItem.class.isAssignableFrom(cls1) && 
						JiraConfigItem.class.isAssignableFrom(cls2)) {
				LOGGER.debug("Compare: " + title + " as JiraConfigItem");
				result.addAll(getDifferences(title, (JiraConfigItem) o1, (JiraConfigItem) o2));
			} else if (	cls1.isArray() && 
						cls2.isArray()) {
				LOGGER.debug("Compare: " + title + " as array");
				result.addAll(getDifferences(title, (Object[]) o1, (Object[]) o2));
			} else if (	Collection.class.isAssignableFrom(cls1) && 
						Collection.class.isAssignableFrom(cls2)) {
				LOGGER.debug("Compare: " + title + " as collection");
				result.addAll(getDifferences(title, (Collection) o1, (Collection) o2));
			} else if (	Map.class.isAssignableFrom(cls1) && 
						Map.class.isAssignableFrom(cls2)) {
				LOGGER.debug("Compare: " + title + " as map");
				result.addAll(getDifferences(title, (Map) o1, (Map) o2));
			} else if (	Comparable.class.isAssignableFrom(cls1) && 
						Comparable.class.isAssignableFrom(cls2)) {
				LOGGER.debug("Compare: " + title + " as comparable");
				if (((Comparable) o1).compareTo((Comparable) o2) != 0) {
					result.add(title);
				}
			} else {
				LOGGER.debug("Compare: " + title + " as string tokens");
				if ((String.valueOf(o1)).compareTo(String.valueOf(o2)) != 0) {
					result.add(title);
				}
			}
		} else if ((o1 != null && o2 == null) || (o1 == null && o2 != null)) {
			LOGGER.debug("Compare: " + title + " one item is null");
			result.add(title + DIFFERENCE_WILDCARD);
		}
		return result;
	}
	
	public final Map<String, String> getMap() {
		return JiraConfigItem.getMap("", this);
	}
	
	public static final Map<String, String> getMap(String title, Collection<?> o) {
		Map<String, String> result = new TreeMap<>();
		if (o != null) {
			int count = 0;
			for (Object item : o) {
				result.putAll(getMap(title + DIFFERENCE_INDEX + count, item));
				count++;
			}
		}
		return result;
	}

	public static final Map<String, String> getMap(String title, Map<?, ?> o) {
		Map<String, String> result = new TreeMap<>();
		if (o != null) {
			for (Map.Entry<?, ?> entry : o.entrySet()) {
				result.putAll(getMap(title + DIFFERENCE_DELIMITER + entry.getKey(), entry.getValue()));
			}
		}
		return result;
	}
	
	public static final Map<String, String> getMap(String title, Object[] o) {
		Map<String, String> result = new TreeMap<>();
		if (o != null) {
			for (int i = 0; i < o.length; i++) {
				result.putAll(getMap(title + DIFFERENCE_INDEX + i, o[i]));
			}
		}
		return result;
	}
	
	public static final Map<String, String> getMap(String title, JiraConfigItem o) {
		Map<String, String> result = new TreeMap<>();
		if (o != null) {
			for (Method method : o.getClass().getMethods()) {
				if (Modifier.isPublic(method.getModifiers()) && 
					method.getParameterCount() == 0 && 
					(method.getName().startsWith("is") || method.getName().startsWith("get")) &&
					!EXCLUDE_METHODS.contains(method.getName())
				) {
					try {
						Object v = method.invoke(o);
						result.putAll(getMap(title + DIFFERENCE_DELIMITER + method.getName(), v));
					} catch (Exception e) {
						LOGGER.error("Failed to construct JiraConfigItem map", e);
						result.put(
								title + DIFFERENCE_DELIMITER + method.getName(), 
								e.getClass().getCanonicalName() + ": " + e.getMessage());
					}
				}
			}
		}
		return result;
	}

	public static final Map<String, String> getMap(String title, Object o1) {
		Map<String, String> result = new TreeMap<>();
		if (o1 != null) {
			Class<?> cls1 = o1.getClass();
			if (String.class.isAssignableFrom(cls1)) {
				LOGGER.debug("getMap: " + title + " as string");
				result.put(title, (String) o1);
			} else if (Long.class.isAssignableFrom(cls1)) {
				LOGGER.debug("getMap: " + title + " as long");
				result.put(title, Long.toString((Long) o1));
			} else if (JiraConfigItem.class.isAssignableFrom(cls1)) {
				LOGGER.debug("getMap: " + title + " as JiraConfigItem");
				result.putAll(getMap(title, (JiraConfigItem) o1));
			} else if (cls1.isArray()) {
				LOGGER.debug("getMap: " + title + " as array");
				result.putAll(getMap(title, (Object[]) o1));
			} else if (Collection.class.isAssignableFrom(cls1)) {
				LOGGER.debug("getMap: " + title + " as collection");
				result.putAll(getMap(title, (Collection<?>) o1));
			} else if (Map.class.isAssignableFrom(cls1)) {
				LOGGER.debug("getMap: " + title + " as map");
				result.putAll(getMap(title, (Map<?, ?>) o1));
			} else {
				LOGGER.debug("getMap: " + title + " as JSON");
				try {
					result.put(title, OM.writeValueAsString(o1));
				} catch (JsonProcessingException e) {
					LOGGER.error("Failed to getMap()", e);
					result.put(title, e.getClass().getCanonicalName() + ": " + e.getMessage());
				}
			}
		}
		return result;
	}
	
	@JsonIgnore
	protected Object jiraObject;
	public final Object getJiraObject() {
		return jiraObject;
	}
	public final void setJiraObject(Object obj) throws Exception {
		this.jiraObject = obj;
		this.fromJiraObject(obj);
	}
	
	@JsonIgnore
	protected boolean selected;
	public final boolean isSelected() {
		return selected;
	}
	public final void setSelected(boolean selected) {
		this.selected = selected;
	}

	public final String getImplementation() {
		return this.getClass().getCanonicalName();
	}
	
	/*
	 * Return a string-based unique key (e.g. issue key, project key) that is not internal ID (which can change across environments)
	 */
	@JsonIgnore
	public abstract String getUniqueKey();
	
	/*
	 * Return a string-based internal ID that is specific to a server instance. 
	 */
	@JsonIgnore
	public abstract String getInternalId();
	
	/**
	 * Stores data from provided object into map.
	 * JiraConfigUtil should call setJiraObject() instead of this method.
	 * @param obj Jira object.
	 * @param params Implementation specific data.
	 */
	protected abstract void fromJiraObject(Object obj, Object... params) throws Exception;
}