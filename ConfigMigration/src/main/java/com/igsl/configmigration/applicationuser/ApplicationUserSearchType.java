package com.igsl.configmigration.applicationuser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.igsl.configmigration.JiraConfigSearchType;

public class ApplicationUserSearchType extends JiraConfigSearchType {

	public static final ApplicationUserSearchType ID = new ApplicationUserSearchType("ID", "id");
	public static final ApplicationUserSearchType KEY = new ApplicationUserSearchType("Key", "key");	
	public static final ApplicationUserSearchType EMAIL = new ApplicationUserSearchType("Email", "email");
	public static final ApplicationUserSearchType USERNAME = new ApplicationUserSearchType("Login ID", "userName");
	
	protected ApplicationUserSearchType(String displayName, String fieldName) {
		super(ApplicationUserUtil.class.getCanonicalName(), displayName, fieldName);
	}

	public static List<JiraConfigSearchType> values() {
		return Arrays.asList(ID, KEY, EMAIL, USERNAME);
	}

}
