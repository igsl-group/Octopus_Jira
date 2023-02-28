package com.igsl.configmigration.status;

import java.util.Comparator;

public class StatusComparator implements Comparator<StatusDTO> {

	@Override
	public int compare(StatusDTO o1, StatusDTO o2) {
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 != null && o2 == null) {
			return 1;
		} else if (o1 == null && o2 != null) {
			return -1;
		} else {
			return o1.getSequence().compareTo(o2.getSequence());
		}
	}

}
