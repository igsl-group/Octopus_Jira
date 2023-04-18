package com.igsl.customfieldtypes.efforttable;

import java.text.DecimalFormat;
import java.util.Comparator;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.google.gson.Gson;

public class EffortTableDataRow implements Comparable<EffortTableDataRow> {

	private static Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareTo); 
	private static Comparator<Double> nullSafeDoubleComparator = Comparator.nullsFirst(Double::compareTo);

	private String task;
	private String headCountDay;
	
	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String getHeadCountDay() {
		return headCountDay;
	}

	public void setHeadCountDay(String headCountDay) {
		this.headCountDay = headCountDay;
	}

	@Override
	public int compareTo(EffortTableDataRow o) {
		if (o == null) {
			return 1;
		}
		return nullSafeStringComparator.compare(this.task, o.task) | nullSafeStringComparator.compare(this.headCountDay, o.headCountDay);
	}

	public static EffortTableDataRow fromString(String s) throws FieldValidationException {
		if (s == null || s.isEmpty()) {
			return null;
		}
		try {
			EffortTableDataRow data = new Gson().fromJson(s, EffortTableDataRow.class);
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
		return task + ": " + headCountDay;
	}
	
}
