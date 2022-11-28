package com.igsl.configmigration.defaultvalueoperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.DefaultValueOperations;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.user.ApplicationUser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.applicationuser.ApplicationUserDTO;
import com.igsl.configmigration.group.GroupDTO;
import com.igsl.configmigration.insight.ObjectBeanDTO;
import com.igsl.configmigration.label.LabelDTO;
import com.igsl.configmigration.options.OptionDTO;
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class DefaultValueOperationsDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(DefaultValueOperationsDTO.class);
	
	public enum ValueType {
		OBJECT,
		LIST_OBJECT,
		MAP_OBJECT,
		DTO,
		LIST_DTO,
		MAP_DTO
	}
	
	private static class ParseResult {
		private ValueType valueType;
		private String valueClass;
		private Object value;
		public ParseResult(ValueType valueType, String valueClass, Object value) {
			this.valueType = valueType;
			this.valueClass = valueClass;
			this.value = value;
		}
		public ValueType getValueType() {
			return valueType;
		}
		public void setValueType(ValueType valueType) {
			this.valueType = valueType;
		}
		public String getValueClass() {
			return valueClass;
		}
		public void setValueClass(String valueClass) {
			this.valueClass = valueClass;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
	}
	
	private Object defaultValue;
	private List<Object> defaultValueList;
	private Map<Object, Object> defaultValueMap;
	private JiraConfigDTO defaultDTO;
	private List<JiraConfigDTO> defaultDTOList;
	private Map<Object, JiraConfigDTO> defaultDTOMap;
	private ValueType valueType;
	private String valueClass;
	
	public Object getDefaultValueObject() {
		// Do the reverse of parseDefaultValue
		if (this.valueType != null) {
			switch (this.valueType) {
			case DTO: 
				return this.getDefaultDTO().getJiraObject();
			case LIST_DTO: 
				List<Object> dtoList = new ArrayList<>();
				for (JiraConfigDTO item : this.getDefaultDTOList()) {
					dtoList.add(item.getJiraObject());
				}
				return dtoList;
			case MAP_DTO: 
				Map<Object, Object> dtoMap = new HashMap<>();
				for (Map.Entry<Object, JiraConfigDTO> entry : this.getDefaultDTOMap().entrySet()) {
					dtoMap.put(entry.getKey(), entry.getValue().getJiraObject());
				}
				return dtoMap;
			case OBJECT: 
				return this.getDefaultValue();
			case LIST_OBJECT: 
				return this.getDefaultValueList();
			case MAP_OBJECT: 
				return this.getDefaultValueMap();
			}
		}
		return null;
	}
	
	private ParseResult parseDefaultValue(Object o) throws Exception {
		ParseResult result = null;
		if (o != null) {
			Class<?> cls = o.getClass();
			if (Option.class.isAssignableFrom(cls)) {
				OptionDTO r = new OptionDTO();
				r.setJiraObject(o);
				result = new ParseResult(ValueType.DTO, r.getClass().getCanonicalName(), r);
			} else if (ApplicationUser.class.isAssignableFrom(cls)) {
				ApplicationUserDTO r = new ApplicationUserDTO();
				r.setJiraObject(o);
				result = new ParseResult(ValueType.DTO, r.getClass().getCanonicalName(), r);
			} else if (Group.class.isAssignableFrom(cls)) {
				GroupDTO r = new GroupDTO();
				r.setJiraObject(o);
				result = new ParseResult(ValueType.DTO, r.getClass().getCanonicalName(), r);
			} else if (Label.class.isAssignableFrom(cls)) {
				LabelDTO r = new LabelDTO();
				r.setJiraObject(o);
				result = new ParseResult(ValueType.DTO, r.getClass().getCanonicalName(), r);
			} else if (ObjectBean.class.isAssignableFrom(cls)) {
				ObjectBeanDTO r = new ObjectBeanDTO();
				r.setJiraObject(o);
				result = new ParseResult(ValueType.DTO, r.getClass().getCanonicalName(), r);
			} else if (cls.isArray()) {
				Object[] src = (Object[]) o;
				List<Object> array = new ArrayList<>();
				ValueType itemType = ValueType.DTO;
				String itemClass = null;
				for (int i = 0; i < src.length; i++) {
					ParseResult item = parseDefaultValue(src[i]);
					itemClass = item.getValueClass();
					itemType = item.getValueType();
					array.add(item.getValue());
				}
				switch (itemType) {
				case DTO:
				case LIST_DTO:
				case MAP_DTO: 
					result = new ParseResult(ValueType.LIST_DTO, itemClass, array);
					break;
				case OBJECT:
				case LIST_OBJECT:
				case MAP_OBJECT:
					result = new ParseResult(ValueType.LIST_OBJECT, itemClass, array);
					break;
				}
			} else if (Map.class.isAssignableFrom(cls)) {
				Map<?, ?> src = (Map<?, ?>) o;
				Map<Object, Object> map = new TreeMap<>();
				ValueType itemType = ValueType.DTO;
				String itemClass = null;
				for (Map.Entry<?, ?> entry : src.entrySet()) {
					Object key;
					if (entry.getKey() != null) {
						key = entry.getKey();
					} else {
						key = "null";
					}
					ParseResult item = parseDefaultValue(entry.getValue());
					if (item != null) {
						itemType = item.getValueType();
						itemClass = item.getValueClass();
						map.put(key, item.getValue());
					}
				}
				switch (itemType) {
				case DTO:
				case LIST_DTO:
				case MAP_DTO: 
					result = new ParseResult(ValueType.MAP_DTO, itemClass, map);
					break;
				case OBJECT:
				case LIST_OBJECT:
				case MAP_OBJECT:
					result = new ParseResult(ValueType.MAP_OBJECT, itemClass, map);
					break;
				}
			} else if (Collection.class.isAssignableFrom(cls)) {
				Collection<?> src = (Collection<?>) o;
				List<Object> array = new ArrayList<>();
				ValueType itemType = ValueType.DTO;
				String itemClass = null;
				for (Object e : src) {
					ParseResult item = parseDefaultValue(e);
					if (item != null) {
						itemType = item.getValueType();
						itemClass = item.getValueClass();
						array.add(item.getValue());
					}
				}
				switch (itemType) {
				case DTO:
				case LIST_DTO:
				case MAP_DTO: 
					result = new ParseResult(ValueType.LIST_DTO, itemClass, array);
					break;
				case OBJECT:
				case LIST_OBJECT:
				case MAP_OBJECT:
					result = new ParseResult(ValueType.LIST_OBJECT, itemClass, array);
					break;
				}
			} else {
				// String, Long, Integer, Timestamp, etc.
				result = new ParseResult(
						ValueType.OBJECT, 
						((o != null)? o.getClass().getCanonicalName() : null), 
						o);
			}
		}
		return result;
	}
	
	/**
	 * params[0]: FieldConfig
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		DefaultValueOperations<?> obj = (DefaultValueOperations<?>) o;
		// TODO 
		// Need to find how "current date" is stored
		// For now current date/datetime will be convereted to a fixed value
		Object def = obj.getDefaultValue((FieldConfig) params[0]);
		ParseResult item = parseDefaultValue(def);
		if (item != null) {
			this.valueType = item.getValueType();
			this.valueClass = item.getValueClass();
			switch (this.valueType) {
			case DTO:
				this.defaultDTO = (JiraConfigDTO) item.getValue();
				break;
			case LIST_DTO:
				this.defaultDTOList = (List<JiraConfigDTO>) item.getValue();
				break;
			case MAP_DTO: 
				this.defaultDTOMap = (Map<Object, JiraConfigDTO>) item.getValue();
				break;
			case OBJECT:
				this.defaultValue = item.getValue();
				break;
			case LIST_OBJECT:
				this.defaultValueList = (List<Object>) item.getValue();
				break;
			case MAP_OBJECT:
				this.defaultValueMap = (Map<Object, Object>) item.getValue();
				break;
			}
		}
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
				"getValueType",
				"getDefaultValue",
				"getDefaultValueList",
				"getDefaultValueMap",
				"getDefaultDTO",
				"getDefaultDTOList",
				"getDefaultDTOMap");
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public List<Object> getDefaultValueList() {
		return defaultValueList;
	}

	public void setDefaultValueList(List<Object> defaultValueList) {
		this.defaultValueList = defaultValueList;
	}

	public Map<Object, Object> getDefaultValueMap() {
		return defaultValueMap;
	}

	public void setDefaultValueMap(Map<Object, Object> defaultValueMap) {
		this.defaultValueMap = defaultValueMap;
	}

	public JiraConfigDTO getDefaultDTO() {
		return defaultDTO;
	}

	public void setDefaultDTO(JiraConfigDTO defaultDTO) {
		this.defaultDTO = defaultDTO;
	}

	public List<JiraConfigDTO> getDefaultDTOList() {
		return defaultDTOList;
	}

	public void setDefaultDTOList(List<JiraConfigDTO> defaultDTOList) {
		this.defaultDTOList = defaultDTOList;
	}

	public Map<Object, JiraConfigDTO> getDefaultDTOMap() {
		return defaultDTOMap;
	}

	public void setDefaultDTOMap(Map<Object, JiraConfigDTO> defaultDTOMap) {
		this.defaultDTOMap = defaultDTOMap;
	}

	public ValueType getValueType() {
		return valueType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	public String getValueClass() {
		return valueClass;
	}

	public void setValueClass(String valueClass) {
		this.valueClass = valueClass;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

}
