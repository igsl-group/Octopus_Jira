package com.igsl.customfieldtypes.efforttable;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.google.gson.Gson;

public class EffortTableData implements Comparable<EffortTableData> {

	private List<EffortTableDataRow> rows = new ArrayList<EffortTableDataRow>();
	private String expenses;
	private String totalHeadCountDay;
	
	public List<EffortTableDataRow> getRows() {
		return rows;
	}

	public void setRows(List<EffortTableDataRow> rows) {
		this.rows = rows;
	}

	public String getExpenses() {
		return expenses;
	}

	public void setExpenses(String expenses) {
		this.expenses = expenses;
	}

	public String getTotalHeadCountDay() {
		return totalHeadCountDay;
	}

	public void setTotalHeadCountDay(String totalHeadCountDay) {
		this.totalHeadCountDay = totalHeadCountDay;
	}

	@Override
	public int compareTo(EffortTableData o) {
		if (o == null) {
			return 1;
		}
		if (this.expenses != o.expenses || this.totalHeadCountDay != o.totalHeadCountDay) {
			return 1;
		}
		if (rows.size() != o.rows.size()) {
			return 1;
		}
		for (int i = 0; i < rows.size(); i++) {
			int r = rows.get(i).compareTo(o.rows.get(i));
			if (r != 0) {
				return 1;
			}
		}
		return 0;
	}

	public static EffortTableData fromString(String s) throws FieldValidationException {
		if (s == null || s.isEmpty()) {
			return null;
		}
		try {
			EffortTableData data = new Gson().fromJson(s, EffortTableData.class);
			return data;
		} catch (Exception ex) {
			throw new FieldValidationException(ex.getMessage());
		}
	}
	
	@Override
	public String toString() {
		if (rows.size() != 0 || (expenses != null && expenses != "")) {
			return new Gson().toJson(this);
		}
		return null;
	}
	
	public String toReadableString() {
		return toString();
	}
	
}
