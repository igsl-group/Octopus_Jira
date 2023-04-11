package com.igsl.configmigration.export.v1;

import java.util.Comparator;

public class ExportDataComparator implements Comparator<ExportData> {

	@Override
	public int compare(ExportData o1, ExportData o2) {
		if (o1 != null && o2 == null) {
			return 1;
		} else if (o1 == null && o2 != null) {
			return -1;
		} else if (o1 == null && o2 == null) {
			return 0;
		} else {
			return o1.getExportDate().compareTo(o2.getExportDate());
		}
	}

}
