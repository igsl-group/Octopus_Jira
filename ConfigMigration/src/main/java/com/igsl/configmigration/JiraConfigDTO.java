package com.igsl.configmigration;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.general.GeneralDTO;

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
public abstract class JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(JiraConfigDTO.class);
	protected static final ObjectMapper OM = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	// Methods not to be included in getMap(), i.e. display
	private static final List<String> MAP_EXCLUDE_METHODS = Arrays.asList(
			"getClass",
			"getJiraObject",
			"isSelected",
			"isReferenced",
			"getUniqueKey",
			"getInternalId",
			"getImplementation",
			"getMap",
			"getMapIgnoredMethods",
			"getUtilClass",
			"getJiraClass",
			"getObjectParameters"
		);
	
	/**
	 * Return list of method names to be used for comparing JiraConfigDTO.
	 * MAP_EXCLUDE_METHODS contains method names that are excluded by default.
	 * @return List of String
	 */
	protected abstract List<String> getCompareMethods();
	
	private static final String DIFFERENCE_DELIMITER = ".";
	public static final String DIFFERENCE_WILDCARD = DIFFERENCE_DELIMITER + "*";
	public static final String DIFFERENCE_INDEX = DIFFERENCE_DELIMITER + "#";
	public static final String DIFFERENCE_KEYS = DIFFERENCE_DELIMITER + "@";
	
	/**
	 * Return Jira object class that this DTO encapsulates.
	 */
	@JsonIgnore
	public abstract Class<?> getJiraClass();
	
	/**
	 * Return JiraConfigUtil class associated with this DTO.
	 */
	@JsonIgnore
	public abstract Class<? extends JiraConfigUtil> getUtilClass();
	
	/**
	 * Get differences between two JiraConfigDTO objects.
	 * Recursively compares nested objects. 
	 * 
	 * @param title Name. If different, this value gets added to the return value.
	 * @param o1
	 * @param o2
	 * @return List<String> containing the titles of the differences, same as those from getMap().
	 */
	public static final List<String> getDifferences(String title, JiraConfigDTO o1, JiraConfigDTO o2) {
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
					!MAP_EXCLUDE_METHODS.contains(m.getName())) {
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
	
	/**
	 * Get differences between two maps from JiraConfigDTO.getMap()
	 */
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
	
	/**
	 * Get differences between two object arrays
	 */
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
	
	/**
	 * Get differences between two collections
	 */
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
	
	/**
	 * Get differences between two objects.
	 * This is the generic entry point for getDifferences() methods, 
	 * it will delegate to overloaded versions according to object class
	 */
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
			} else if (	JiraConfigDTO.class.isAssignableFrom(cls1) && 
						JiraConfigDTO.class.isAssignableFrom(cls2)) {
				LOGGER.debug("Compare: " + title + " as JiraConfigItem");
				result.addAll(getDifferences(title, (JiraConfigDTO) o1, (JiraConfigDTO) o2));
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
	
	/**
	 * Overload this method to not display specific methods but still include in exported JSON
	 * @return
	 */
	@JsonIgnore
	protected List<String> getMapIgnoredMethods() {
		return Collections.emptyList();
	}
	
	/**
	 * Get all object properties as a TreeMap.
	 * Recurse into nested objects. 
	 * 
	 * Methods will be have method name as key.
	 * e.g. Object.Method1
	 * 
	 * Nested objects will have key prefixed by parent object. 
	 * e.g. Parent.Child
	 * 
	 * Arrays will include index as key.
	 * e.g. Parent.Array.#
	 * 
	 * Maps will use key.
	 * e.g. Parent.Map.Key
	 * 
	 * Collections will include index same as array. 
	 */
	@JsonIgnore
	public final Map<String, String> getMap() {
		return JiraConfigDTO.getMap("", this);
	}
	
	/**
	 * Overloaded version of getMap() for collection
	 */
	public static final Map<String, String> getMap(String title, Collection<?> o) {
		Map<String, String> result = new TreeMap<>();
		if (o != null) {
			result.put(title + DIFFERENCE_INDEX, Integer.toString(o.size()));
			int count = 0;
			for (Object item : o) {
				result.putAll(getMap(title + DIFFERENCE_INDEX + count, item));
				count++;
			}
		} else {
			result.put(title, "null");
		}
		return result;
	}

	/**
	 * Overloaded version of getMap() for map
	 */
	public static final Map<String, String> getMap(String title, Map<?, ?> o) {
		Map<String, String> result = new TreeMap<>();
		if (o != null) {
			result.put(title + DIFFERENCE_INDEX, Integer.toString(o.size()));
			String keySet = "";
			try {
				keySet = OM.writeValueAsString(o.keySet());
			} catch (Exception ex) {
				keySet = ex.getClass().getCanonicalName() + ": " + ex.getMessage();
			}
			result.put(title + DIFFERENCE_KEYS, keySet);
			for (Map.Entry<?, ?> entry : o.entrySet()) {
				result.putAll(getMap(title + DIFFERENCE_DELIMITER + entry.getKey(), entry.getValue()));
			}
		} else {
			result.put(title, "null");
		}
		return result;
	}
	
	/**
	 * Overloaded version of getMap() for object array
	 */
	public static final Map<String, String> getMap(String title, Object[] o) {
		Map<String, String> result = new TreeMap<>();
		if (o != null) {
			result.put(title + DIFFERENCE_INDEX, Integer.toString(o.length));
			for (int i = 0; i < o.length; i++) {
				result.putAll(getMap(title + DIFFERENCE_INDEX + i, o[i]));
			}
		} else {
			result.put(title, "null");
		}
		return result;
	}
	
	/**
	 * Overloaded version of getMap() for JiraConfigDTO
	 */
	public static final Map<String, String> getMap(String title, JiraConfigDTO o) {
		Map<String, String> result = new TreeMap<>();
		if (o != null) {
			for (Method method : o.getClass().getMethods()) {
				if (Modifier.isPublic(method.getModifiers()) && 
					method.getParameterCount() == 0 && 
					(method.getName().startsWith("is") || method.getName().startsWith("get")) &&
					!MAP_EXCLUDE_METHODS.contains(method.getName()) && 
					!o.getMapIgnoredMethods().contains(method.getName())
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
		} else {
			result.put(title, "null");
		}
		return result;
	}

	/**
	 * Generic entry point for getMap()
	 * Delegates to overloaded version according to object class
	 */
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
			} else if (JiraConfigDTO.class.isAssignableFrom(cls1)) {
				LOGGER.debug("getMap: " + title + " as JiraConfigItem");
				result.putAll(getMap(title, (JiraConfigDTO) o1));
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
		} else {
			result.put(title, "null");
		}
		return result;
	}
	
	/**
	 * Override this if implementation requires additional parameters. 
	 * This is used to perform integrity check on no. of parameters received in setJiraObject().
	 * @return No. of parameters.
	 */
	@JsonIgnore
	protected int getObjectParameterCount() {
		return 0;
	}
	
	/**
	 * Object parameter. Some Jira objects cannot retrieve a full list without supplying parent object(s).
	 * objectParameters stores the parent objects if needed.
	 */
	@JsonIgnore
	protected Object[] objectParameters;
	public final Object[] getObjectParameters() {
		return objectParameters;
	}
	
	@JsonIgnore
	protected Object jiraObject;
	/**
	 * For deserialized JiraConfigDTO, this will return null.
	 * 
	 * To locate existing object on server, call the associated JiraConfigUtil's findXXX() methods, then call 
	 * its .getJiraObject() method.
	 * 
	 * Otherwise you need knowledge of the type of JiraConfigDTO to access its data fields, and use 
	 * associated JiraConfigUtil's merge() method to create it. 
	 * 
	 * Jira's API does not have a usable class to create in-memory instance for everything. 
	 * e.g. ApplicationUser cannot be created directly, it is always loaded through a UserManager instance.
	 * 
	 * Hence the API is designed this way. 
	 * 
	 * @return
	 * @throws Exception
	 */
	public final Object getJiraObject() throws Exception {
		return jiraObject;
	}
	
	/**
	 * Stores Jira object and search parameters. 
	 * @param obj Jira object. Can be null.
	 * @param params Search parameters. Can be null.
	 * @throws Exception
	 */
	public final void setJiraObject(Object obj, Object... params) throws Exception {
		int expectedCount = this.getObjectParameterCount();
		if (expectedCount != 0) {
			if (params == null || params.length != expectedCount) {
				throw new Exception("Parameter count does not match expectation: " + expectedCount);
			}
			objectParameters = new Object[params.length];
			for (int i = 0; i < params.length; i++) {
				objectParameters[i] = params[i];
			}
		} else {
			objectParameters = new Object[0];
		}
		if (obj != null) {
			this.jiraObject = obj;
			this.fromJiraObject(obj);
		}
	}
	
	/**
	 * To store mapping between import and export JiraConfigDTOs. 
	 * 
	 * Only used for import. 
	 * JiraConfigDTO deserialized from file will be mapped to new or existing JiraConfigDTO from current instance.
	 * We need an object for new items so the new items can be referenced.
	 */
	@JsonIgnore
	protected JiraConfigDTO mappedObject;
	public final void setMappedObject(JiraConfigDTO dto) {
		this.mappedObject = dto;
	}
	public final JiraConfigDTO getMappedObject() {
		return this.mappedObject;
	}
	
	/**
	 * To store JiraConfigDTO referencer (the parent, while this object is the child)
	 */
	protected Set<JiraConfigRef> references = new HashSet<>();
	public final void addReference(JiraConfigRef ref) {
		this.references.add(ref);
	}
	public final void removeReference(JiraConfigRef ref) {
		this.references.remove(ref);
	}
	
	/**
	 * Selection status
	 */
	@JsonIgnore
	protected boolean selected;
	public final boolean isSelected() {
		return selected;
	}
	public final void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * This is included to be used for deserialization.
	 */
	public final String getImplementation() {
		return this.getClass().getCanonicalName();
	}
	
	/**
	 * Return a string-based unique key (e.g. issue key, project key) that is not internal ID (which can change across environments)
	 */
	@JsonIgnore
	public abstract String getUniqueKey();
	
	/**
	 * Return a string-based internal ID that is specific to a server instance. 
	 */
	@JsonIgnore
	public abstract String getInternalId();
	
	/**
	 * Display name for UI, defaults to unique key.
	 * Override as needed.
	 * @return String
	 */
	@JsonIgnore
	public String getConfigName() {
		return getUniqueKey();
	}
	
	/**
	 * Stores data from provided object.
	 * JiraConfigUtil should call setJiraObject() instead of this method.
	 * Additional parameters needed can be found in parameters member. The count will match getParameterCount().
	 * 
	 * @param obj Jira object.
	 */
	protected abstract void fromJiraObject(Object obj) throws Exception;
}
