package com.igsl.configmigration.field;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.fields.Field;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldDTO extends JiraConfigDTO {

	private String id;
	private String name;
	private String nameKey;
	
	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return FieldUtil.class;
	}
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		Field o = (Field) obj;
		this.id = o.getId();
		this.name = o.getName();
		this.nameKey = o.getNameKey();
	}
	
	@Override
	public String getUniqueKey() {
		return this.getName();
	}

	@Override
	public String getInternalId() {
		return this.getId();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName", 
				"getNameKey");
	}

	@Override
	public Class<?> getJiraClass() {
		return Field.class;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameKey() {
		return nameKey;
	}

	public void setNameKey(String nameKey) {
		this.nameKey = nameKey;
	}

}
