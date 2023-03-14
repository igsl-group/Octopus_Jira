package com.igsl.configmigration.status;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.issue.status.category.StatusCategory;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

/**
 * StatusCategory wrapper.
 * 
 * There should be no way to add new StatusCategory, so this can be read only.
 * Only referenced from Status.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class StatusCategoryDTO extends JiraConfigDTO {

	protected List<String> aliases;
	protected String colorName;
	protected Long id;
	protected String key;
	protected String name;
	protected String primaryAlias;
	protected String translatedName;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		StatusCategory o = (StatusCategory) obj;
		this.aliases = o.getAliases();
		this.colorName = o.getColorName();
		this.id = o.getId();
		this.key = o.getKey();
		this.name = o.getName();
		this.primaryAlias = o.getPrimaryAlias();
		this.translatedName = o.getTranslatedName();
		this.uniqueKey = this.name;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Aliases", new JiraConfigProperty(this.aliases));
		r.put("Color Name", new JiraConfigProperty(this.colorName));
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Key", new JiraConfigProperty(this.key));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Primary Alias", new JiraConfigProperty(this.primaryAlias));
		r.put("Translated Name", new JiraConfigProperty(this.translatedName));
		return r;
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

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return StatusCategoryUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return StatusCategory.class;
	}

}
