package com.igsl.configmigration.project;

import java.util.Map;

import com.atlassian.jira.project.Project;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ProjectConfigItem extends JiraConfigItem {

	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		Project obj = (Project) o;
		Map<String, String> map = this.getMap();
		map.put(KEY_NAME, obj.getName());
		map.put(KEY_ID, obj.getKey());
		// TODO More properties
	}
	
	@Override
	public int compareTo(JiraConfigItem o) {
		if (o != null) {
			return 	compare(KEY_NAME, this, o);
		}
		return 1;
	}

}
