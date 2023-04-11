package com.igsl.configmigration.report.v1;

import java.util.Comparator;

public class MergeReportComparator implements Comparator<MergeReport> {

	@Override
	public int compare(MergeReport o1, MergeReport o2) {
		if (o1 != null && o2 == null) {
			return 1;
		} else if (o1 == null && o2 != null) {
			return -1;
		} else if (o1 == null && o2 == null) {
			return 0;
		} else {
			return o1.getMergeDate().compareTo(o2.getMergeDate());
		}
	}

}
