package com.igsl.configmigration.group;

import java.util.Arrays;
import java.util.List;

import com.atlassian.crowd.embedded.api.Group;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class GroupDTO extends JiraConfigDTO {

	private String name;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		Group o = (Group) obj;
		this.name = o.getName();
	}

	@Override
	public String getUniqueKey() {
		return this.getName();
	}

	@Override
	public String getInternalId() {
		return this.getName();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return GroupUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return Group.class;
	}
}
