package com.igsl.configmigration.general;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

/**
 * Wrapper for Object.
 * Used to represent anything (except collection, map, and JiraConfigDTO) 
 * as a JiraConfigDTO for ease of deserialization.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class GeneralDTO extends JiraConfigDTO {

	private Object obj;
	private JiraConfigDTO dto;
	private String valueClass;
	private boolean useDTO;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		if (obj instanceof JiraConfigDTO) {
			this.dto = (JiraConfigDTO) obj;
			useDTO = true;
		} else {
			this.obj = obj;
			useDTO = false;
		}
		if (obj != null) {
			this.valueClass = obj.getClass().getCanonicalName();
		}
		this.uniqueKey = Integer.toString(this.hashCode());
	}

	@Override
	public String getConfigName() {
		if (useDTO) {
			return dto.getConfigName();
		}
		return String.valueOf(obj);
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		if (useDTO) {
			Class<? extends JiraConfigUtil> utilCls = dto.getUtilClass();
			r.put("Value", new JiraConfigProperty(utilCls, dto));
		} else {
			r.put("Value", new JiraConfigProperty(this.obj));
		}
		r.put("Value Class", new JiraConfigProperty(this.valueClass));
		return r;
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList("getValue");
	}

	@Override
	public Class<?> getJiraClass() {
		// Do not associate with anything
		return null;
	}
	
	@JsonIgnore
	public Object getValue() {
		if (useDTO) {
			return dto;
		}
		return obj;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return GeneralUtil.class;
	}

	@Override
	public String getInternalId() {
		return this.uniqueKey;
	}

	public String getValueClass() {
		return valueClass;
	}

	public void setValueClass(String valueClass) {
		this.valueClass = valueClass;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public JiraConfigDTO getDto() {
		return dto;
	}

	public void setDto(JiraConfigDTO dto) {
		this.dto = dto;
	}

	public boolean isUseDTO() {
		return useDTO;
	}

	public void setUseDTO(boolean useDTO) {
		this.useDTO = useDTO;
	}

}
