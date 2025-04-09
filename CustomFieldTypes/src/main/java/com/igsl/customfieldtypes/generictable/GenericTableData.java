package com.igsl.customfieldtypes.generictable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class GenericTableData {
	private List<Map<String, String>> data;
	
	public static GenericTableData parse(String s) throws FieldValidationException {
		Gson gson = new Gson();
		try {
			GenericTableData data = gson.fromJson(s, GenericTableData.class);
			if (data != null && data.data != null && data.data.size() == 0) {
				// Convert empty data into null
				return null;
			}
			return data;
		} catch (JsonSyntaxException ex) {
			throw new FieldValidationException(ex.getMessage());
		}
	}
	
	public GenericTableData() {
		this.data = new ArrayList<>();
	}
	
	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public List<Map<String, String>> getData() {
		return data;
	}
	public void setData(List<Map<String, String>> data) {
		this.data = data;
	}	
}
