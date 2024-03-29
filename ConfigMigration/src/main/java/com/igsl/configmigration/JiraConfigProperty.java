package com.igsl.configmigration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class JiraConfigProperty {
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String NEWLINE = "\r\n";
	
	public enum JiraConfigPropertyType {
		TEXT,
		TEXT_LIST,
		LIST,
		MAP,
		IMAGE,
		COLOR
	}
	
	private String imageType;
	private String value;
	private List<JiraConfigRef> list = new ArrayList<>();
	private Map<Object, JiraConfigRef> map = new TreeMap<>();
	private List<String> textList = new ArrayList<>();
	private JiraConfigPropertyType type;
	
	@JsonIgnore
	public String getImageData() {
		if (this.type == JiraConfigPropertyType.IMAGE) {
			return "data:" + this.imageType + ";base64," + this.value;
		}
		return null;
	}
	
	public JiraConfigProperty() {
		this.type = null;
		this.value = null;
	}
	public static JiraConfigProperty createColor(String colorValue) {
		JiraConfigProperty r = new JiraConfigProperty();
		r.type = JiraConfigPropertyType.COLOR;
		r.value = colorValue;
		return r;
	}
	public JiraConfigProperty(String imageType, String imageContentBase64) {
		this.type = JiraConfigPropertyType.IMAGE;
		this.imageType = imageType;
		this.value = imageContentBase64;
	}
	public JiraConfigProperty(Object value) {
		this.type = JiraConfigPropertyType.TEXT;
		if (value != null) {
			this.value = value.toString();
		}
	}
	public JiraConfigProperty(Map<?, ?> value) {
		this.type = JiraConfigPropertyType.TEXT_LIST;
		if (value != null) {
			for (Object o : value.entrySet()) {
				if (o != null) {
					Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
					this.textList.add(e.getKey() + " = " + e.getValue());
				} else {
					this.textList.add("null");
				}
			}
		}
	}
	public JiraConfigProperty(List<?> value) {
		this.type = JiraConfigPropertyType.TEXT_LIST;
		if (value != null) {
			for (Object o : value) {
				if (o != null) {
					this.textList.add(o.toString());
				} else {
					this.textList.add("null");
				}
			}
		}
	}
	public JiraConfigProperty(int value) {
		this.type = JiraConfigPropertyType.TEXT;
		this.value = Integer.toString(value);
	}
	public JiraConfigProperty(String value) {
		this.type = JiraConfigPropertyType.TEXT;
		this.value = value;
	}
	public JiraConfigProperty(Long value) {
		this.type = JiraConfigPropertyType.TEXT;
		if (value != null) {
			this.value = Long.toString(value);
		}
	}
	public JiraConfigProperty(Date value) {
		this.type = JiraConfigPropertyType.TEXT;
		if (value != null) {
			this.value = SDF.format(value);
		}
	}
	public JiraConfigProperty(Class<? extends JiraConfigUtil> utilClass, JiraConfigDTO dto) {
		this.type = JiraConfigPropertyType.LIST;
		if (dto != null) {
			this.list.add(new JiraConfigRef(dto));
		}
	}
	public JiraConfigProperty(
			Class<? extends JiraConfigUtil> utilClass, 
			Collection<? extends JiraConfigDTO> dtos) {
		this.type = JiraConfigPropertyType.LIST;
		if (dtos != null) {
			for (JiraConfigDTO dto : dtos) {
				if (dto != null) {
					this.list.add(new JiraConfigRef(dto));
				} else {
					this.list.add(null);
				}
			}
		}
	}
	public JiraConfigProperty(Class<? extends JiraConfigUtil> utilClass, JiraConfigDTO[] dtos) {
		this.type = JiraConfigPropertyType.LIST;
		if (dtos != null) {
			for (JiraConfigDTO dto : dtos) {
				if (dto != null) {
					this.list.add(new JiraConfigRef(dto));
				} else {
					this.list.add(null);
				}
			}
		}
	}
	public JiraConfigProperty(Class<? extends JiraConfigUtil> utilClass, List<? extends JiraConfigDTO> dtos) {
		this.type = JiraConfigPropertyType.LIST;
		if (dtos != null) {
			for (JiraConfigDTO dto : dtos) {
				if (dto != null) {
					this.list.add(new JiraConfigRef(dto));
				} else {
					this.list.add(null);
				}
			}
		}
	}
	public JiraConfigProperty(Class<? extends JiraConfigUtil> utilClass, Map<?, ? extends JiraConfigDTO> dtos) {
		this.type = JiraConfigPropertyType.MAP;
		if (dtos != null) {
			for (Map.Entry<?, ? extends JiraConfigDTO> dto : dtos.entrySet()) {
				if (dto.getValue() != null) {
					JiraConfigDTO d = (JiraConfigDTO) dto.getValue();
					this.map.put(dto.getKey(), new JiraConfigRef(d));
				} else {
					this.map.put(dto.getKey(), null);
				}
			}
		}
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public List<JiraConfigRef> getList() {
		return list;
	}
	public void setList(List<JiraConfigRef> list) {
		this.list = list;
	}
	public Map<Object, JiraConfigRef> getMap() {
		return map;
	}
	public void setMap(Map<Object, JiraConfigRef> map) {
		this.map = map;
	}
	public JiraConfigPropertyType getType() {
		return type;
	}
	public void setType(JiraConfigPropertyType type) {
		this.type = type;
	}
	public String getImageType() {
		return imageType;
	}
	public void setImageType(String imageType) {
		this.imageType = imageType;
	}
	public List<String> getTextList() {
		return textList;
	}
	public void setTextList(List<String> textList) {
		this.textList = textList;
	}	
}
