package com.igsl.configmigration;

import java.util.Collection;
import java.util.Collections;

/**
 * Common interface for search type.
 */
public abstract class JiraConfigSearchType {
	protected String objectType;
	protected String displayName;
	protected String fieldName;
	
	protected JiraConfigSearchType(String objectType, String displayName, String fieldName) {
		this.objectType = objectType;
		this.displayName = displayName;
		this.fieldName = fieldName;
	}
	
	public static Collection<JiraConfigSearchType> values() {
		return Collections.emptyList();
	}
	
	@Override
	public final String toString() {
		return this.objectType + "-" + this.fieldName;
	}
	public final String getObjectType() {
		return this.objectType;
	}
	public final String getDisplayName() {
		return this.displayName;
	}
	public final String getFieldName() {
		return this.fieldName;
	}
}
