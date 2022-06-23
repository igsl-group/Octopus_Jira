package com.igsl.customfieldtypes.urlfield;

import java.util.Comparator;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.google.gson.Gson;

public class URLFieldData implements Comparable<URLFieldData> {

	private static Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareTo); 

	private String url;
	private String displayText;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}

	@Override
	public int compareTo(URLFieldData o) {
		if (o == null) {
			return 1;
		}
		return nullSafeStringComparator.compare(this.displayText, o.displayText) | nullSafeStringComparator.compare(this.url, o.url);
	}

	public static URLFieldData fromString(String s) throws FieldValidationException {
		if (s == null || s.isEmpty()) {
			return null;
		}
		try {
			URLFieldData data = new Gson().fromJson(s, URLFieldData.class);
			return data;
		} catch (Exception ex) {
			throw new FieldValidationException(ex.getMessage());
		}
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
	
	public String toReadableString() {
		return (displayText == null || displayText.isEmpty())? url : displayText + " (" + url + ")";
	}
	
}
