package com.igsl.configmigration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value={"implementation"}, allowGetters=true)
public class JiraConfigRef {
	
	private String display;
	private String util;
	private String type;
	private String uniqueKey;
	private String uniqueKeyJS;
	
	public JiraConfigRef() {
		// Default constructor for JSON deserialization
	}
	
	public JiraConfigRef(JiraConfigDTO dto) {
		if (dto != null) {
			this.display = dto.getConfigName();
			this.util = dto.getUtilClass().getCanonicalName();
			this.type = JiraConfigTypeRegistry.getConfigUtil(dto.getUtilClass()).getName();
			this.uniqueKey = dto.getUniqueKey();
			this.uniqueKeyJS = dto.getUniqueKeyJS();
		}
	}

	public final String getImplementation() {
		return JiraConfigRef.class.getCanonicalName();
	}
	
	@JsonIgnore
	public String getRefKey() {
		return this.util + "-" + this.uniqueKey;
	}

	public String getDisplay() {
		return display;
	}
	public String getUtil() {
		return util;
	}
	public String getUniqueKey() {
		return uniqueKey;
	}
	public String getType() {
		return type;
	}
	public String getUniqueKeyJS() {
		return uniqueKeyJS;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public void setUtil(String util) {
		this.util = util;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}
	public void setUniqueKeyJS(String uniqueKeyJS) {
		this.uniqueKeyJS = uniqueKeyJS;
	}
}