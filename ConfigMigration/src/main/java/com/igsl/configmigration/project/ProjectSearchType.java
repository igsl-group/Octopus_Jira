package com.igsl.configmigration.project;

import java.util.Arrays;
import java.util.List;

import com.igsl.configmigration.JiraConfigSearchType;

public class ProjectSearchType extends JiraConfigSearchType {

	public static final ProjectSearchType ID = new ProjectSearchType("ID", "id");
	public static final ProjectSearchType KEY = new ProjectSearchType("Key", "key");	
	public static final ProjectSearchType NAME = new ProjectSearchType("Name", "name");
	
	protected ProjectSearchType(String displayName, String fieldName) {
		super(ProjectUtil.class.getCanonicalName(), displayName, fieldName);
	}

	public static List<JiraConfigSearchType> values() {
		return Arrays.asList(ID, KEY, NAME);
	}

}
