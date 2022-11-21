package com.igsl.configmigration.status;

// Because we cannot create StatusCategory instances, this class is created to deserialize StatusCategory
public class StatusCategoryStandIn {

	private String id;
	private String key;
	private String name;
	private String colorName;
	private Long sequence;
	private String[] aliases;
	private String primaryAlias;
	private String translatedName;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getColorName() {
		return colorName;
	}
	public void setColorName(String colorName) {
		this.colorName = colorName;
	}
	public Long getSequence() {
		return sequence;
	}
	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}
	public String[] getAliases() {
		return aliases;
	}
	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}
	public String getPrimaryAlias() {
		return primaryAlias;
	}
	public void setPrimaryAlias(String primaryAlias) {
		this.primaryAlias = primaryAlias;
	}
	public String getTranslatedName() {
		return translatedName;
	}
	public void setTranslatedName(String translatedName) {
		this.translatedName = translatedName;
	}
}
