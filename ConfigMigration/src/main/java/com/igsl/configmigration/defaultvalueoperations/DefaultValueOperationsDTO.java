package com.igsl.configmigration.defaultvalueoperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.DefaultValueOperations;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldconfig.FieldConfigDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class DefaultValueOperationsDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(DefaultValueOperationsDTO.class);
	private static final String NULL_KEY_REPLACEMENT = "[NULL_KEY]";
	
	private Object defaultValue;
	private List<String> valueClass = new ArrayList<>();
	
	@JsonIgnore
	public Object getRawValue() {
		return getRawValue(this.defaultValue);
	}
	@SuppressWarnings("unchecked")
	private static Object getRawValue(Object value) {
		Object result = null;
		// Reverse of parseValue
		if (value != null) {
			Class<?> cls = value.getClass();
			if (Map.class.isAssignableFrom(cls)) {
				Map<Object, Object> target = new LinkedHashMap<>();
				Map<Object, Object> src = (Map<Object, Object>) value;
				for (Map.Entry<Object, Object> entry : src.entrySet()) {
					Object key = entry.getKey();
					// Replace dummy key for null
					if (NULL_KEY_REPLACEMENT.equals(String.valueOf(key))) {
						key = null;
					}
					Object v = getRawValue(entry.getValue());				
					target.put(key, v);
				}
				result = target;
			} else if (Collection.class.isAssignableFrom(cls)) {
				Collection<Object> target = new ArrayList<>();
				Collection<Object> src = (Collection<Object>) value;
				for (Object entry : src) {
					Object v = getRawValue(entry);
					target.add(v);
				}
				result = target;
			} else if (cls.isArray()) {
				List<Object> target = new ArrayList<>();
				Object[] src = (Object[]) value;
				for (Object entry : src) {
					Object v = getRawValue(entry);
					target.add(v);
				}				
				result = target.toArray(new Object[0]);
			} else if (JiraConfigDTO.class.isAssignableFrom(cls)) {
				result = ((JiraConfigDTO) value).getJiraObject();
			} else {
				result = value;
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static Object parseValue(Object value, List<String> classList) throws Exception {
		Object result = null;
		if (value != null) {
			Class<?> cls = value.getClass();
			classList.add(cls.getCanonicalName());
			if (Map.class.isAssignableFrom(cls)) {
				Map<Object, Object> target = new LinkedHashMap<>();
				Map<Object, Object> src = (Map<Object, Object>) value;
				for (Map.Entry<Object, Object> entry : src.entrySet()) {
					// Replace null key with dummy, JSON map does not allow null key
					Object key = (entry.getKey() != null)? entry.getKey() : NULL_KEY_REPLACEMENT;
					Object v = parseValue(entry.getValue(), classList);					
					target.put(key, v);
				}
				result = target;
			} else if (Collection.class.isAssignableFrom(cls)) {
				Collection<Object> target = new ArrayList<>();
				Collection<Object> src = (Collection<Object>) value;
				for (Object entry : src) {
					Object v = parseValue(entry, classList);
					target.add(v);
				}
				result = target;
			} else if (cls.isArray()) {
				List<Object> target = new ArrayList<>();
				Object[] src = (Object[]) value;
				for (Object entry : src) {
					Object v = parseValue(entry, classList);
					target.add(v);
				}				
				result = target.toArray(new Object[0]);
			} else {
				Class<? extends JiraConfigDTO> dtoClass = 
						JiraConfigTypeRegistry.getDTOClassName(cls);
				if (dtoClass != null) {
					JiraConfigDTO dto = dtoClass.newInstance();
					dto.setJiraObject(value);
					classList.add(dtoClass.getCanonicalName());
					result = dto;
				} else {
					// Store as plain object
					classList.add(value.getClass().getCanonicalName());
					result = value;
				}
			}
		}
		return result;
	}
	
	/**
	 * #0: FieldConfig
	 */
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		DefaultValueOperations<?> obj = (DefaultValueOperations<?>) o;
		FieldConfigDTO fieldConfigDTO = (FieldConfigDTO) params[0];
		// TODO 
		// Need to find how "current date" is stored
		// For now current date/datetime will be convereted to a fixed value
		Object def = obj.getDefaultValue((FieldConfig) fieldConfigDTO.getJiraObject());
		this.defaultValue = parseValue(def, this.valueClass);
	}

	@Override
	public String getUniqueKey() {
		return Integer.toString(this.hashCode());
	}

	@Override
	public String getInternalId() {
		return Integer.toString(this.hashCode());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getDefaultValue");
	}

	@JsonIgnore
	protected List<String> getMapIgnoredMethods() {
		return Arrays.asList(
				"getRawValue");
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public Class<?> getJiraClass() {
		return DefaultValueOperations.class;
	}
	
	public List<String> getValueClass() {
		return valueClass;
	}
}
