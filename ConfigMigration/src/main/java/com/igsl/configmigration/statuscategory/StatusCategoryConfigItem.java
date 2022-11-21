package com.igsl.configmigration.statuscategory;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.status.category.StatusCategory;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class StatusCategoryConfigItem extends JiraConfigItem {

	protected List<String> aliases;
	protected String colorName;
	protected Long id;
	protected String key;
	protected String name;
	protected String primaryAlias;
	protected String translatedName;
	
	@Override
	public void fromJiraObject(Object obj, Object... params) throws Exception {
		StatusCategory o = (StatusCategory) obj;
		this.aliases = o.getAliases();
		this.colorName = o.getColorName();
		this.id = o.getId();
		this.key = o.getKey();
		this.name = o.getName();
		this.primaryAlias = o.getPrimaryAlias();
		this.translatedName = o.getTranslatedName();
	}

	@Override
	public String getUniqueKey() {
		return this.getName();
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	public List<String> getAliases() {
		return aliases;
	}

	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}

	public String getColorName() {
		return colorName;
	}

	public void setColorName(String colorName) {
		this.colorName = colorName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getPrimaryAlias",
				"getTranslatedName",
				"getKey",
				"getColorName",
				"getAliases");
	}

}
