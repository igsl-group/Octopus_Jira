package com.igsl.configmigration.defaultvalueoperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.DefaultValueOperations;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldconfig.FieldConfigDTO;
import com.igsl.configmigration.general.GeneralDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class DefaultValueOperationsDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(DefaultValueOperationsDTO.class);
	public static final String NULL_KEY_REPLACEMENT = "[NULL_KEY]";

	public enum ValueType {
		OBJECT,
		LIST,
		MAP,
		ARRAY
	}
	
	private ValueType valueType;
	// Class of defaultValue, or individual item in case of array, map and collection
	private Class<?> valueClass;
	private JiraConfigDTO defaultValue;
	private List<JiraConfigDTO> defaultListValue;
	private Map<Object, JiraConfigDTO> defaultMapValue;
	
	private JiraConfigDTO parseValueHelper(Object o, FieldConfig fieldConfig) throws Exception {
		if (o != null) {
			Class<?> cls = o.getClass();
			Class<? extends JiraConfigDTO> dtoClass = JiraConfigTypeRegistry.getDTOClass(cls);
			JiraConfigDTO dto;
			if (dtoClass != null) {
				dto = dtoClass.newInstance();
			} else {
				dto = new GeneralDTO();
			}
			// Special handling for Option
			Long parentId = null;
			if (Option.class.isAssignableFrom(cls)) {
				Option opt = (Option) o;
				if (opt.getParentOption() != null) {
					parentId = opt.getParentOption().getOptionId();
				}
			}
			dto.setJiraObject(o, fieldConfig, parentId);
			return dto;
		} 
		return new GeneralDTO();
	}
	
	// Recursively turn objects into JiraConfigDTO so they serialize and deserialize properly
	private void parseValue(Object value, FieldConfig fieldConfig) throws Exception {
		if (value != null) {
			Class<?> valueClass = value.getClass();
			if (valueClass.isArray()) {
				this.defaultListValue = new ArrayList<>();
				Object[] src = (Object[]) value;
				for (Object item : src) {
					JiraConfigDTO dto = parseValueHelper(item, fieldConfig);
					this.defaultListValue.add(dto);
					if (dto != null) {
						this.valueClass = dto.getClass();
					}
				}
				this.valueType = ValueType.ARRAY;
			} else if (Map.class.isAssignableFrom(valueClass)) {
				this.defaultMapValue = new HashMap<>();
				Map<?, ?> src = (Map<?, ?>) value;
				for (Map.Entry<?, ?> entry : src.entrySet()) {
					Object key;
					if (entry.getKey() != null) {
						key = entry.getKey();
					} else {
						key = NULL_KEY_REPLACEMENT;
					}
					JiraConfigDTO val = parseValueHelper(entry.getValue(), fieldConfig);
					this.defaultMapValue.put(key, val);
					if (val != null) {
						this.valueClass = val.getClass();
					}
				}
				this.valueType = ValueType.MAP;
			} else if (Collection.class.isAssignableFrom(valueClass)) {
				this.defaultListValue = new ArrayList<>();
				Collection<?> src = (Collection<?>) value;
				for (Object item : src) {
					JiraConfigDTO val = parseValueHelper(item, fieldConfig);
					this.defaultListValue.add(val);
					if (val != null) {
						this.valueClass = val.getClass();
					}
				}
				this.valueType = ValueType.LIST;
			} else if (JiraConfigDTO.class.isAssignableFrom(valueClass)) {
				this.defaultValue = (JiraConfigDTO) value;		
				if (this.defaultValue != null) {
					this.valueClass = this.defaultValue.getClass();
				}
				this.valueType = ValueType.OBJECT;
			} else {
				this.defaultValue = parseValueHelper(value, fieldConfig);
				if (this.defaultValue != null) {
					this.valueClass = this.defaultValue.getClass();
				}
				this.valueType = ValueType.OBJECT;
			}
		} else {
			this.defaultValue = new GeneralDTO();
			this.valueClass = null;
			this.valueType = ValueType.OBJECT;
		}
	}
	
	/**
	 * #0: FieldConfig
	 */
	@Override
	protected int getObjectParameterCount() {
		return 1;
	}
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		DefaultValueOperations<?> obj = (DefaultValueOperations<?>) o;
		FieldConfig fieldConfig = (FieldConfig) objectParameters[0];
		// TODO
		// Need to find how "current date" is stored
		// For now current date/datetime will be convereted to a fixed value
		Object defVal = obj.getDefaultValue(fieldConfig);
		parseValue(defVal, fieldConfig);
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

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public Class<?> getJiraClass() {
		return DefaultValueOperations.class;
	}

	public JiraConfigDTO getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(JiraConfigDTO defaultValue) {
		this.defaultValue = defaultValue;
	}

	public ValueType getValueType() {
		return valueType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	public List<JiraConfigDTO> getDefaultListValue() {
		return defaultListValue;
	}

	public void setDefaultListValue(List<JiraConfigDTO> defaultListValue) {
		this.defaultListValue = defaultListValue;
	}

	public Map<Object, JiraConfigDTO> getDefaultMapValue() {
		return defaultMapValue;
	}

	public void setDefaultMapValue(Map<Object, JiraConfigDTO> defaultMapValue) {
		this.defaultMapValue = defaultMapValue;
	}

	public Class<?> getValueClass() {
		return valueClass;
	}

	public void setValueClass(Class<?> valueClass) {
		this.valueClass = valueClass;
	}

}
