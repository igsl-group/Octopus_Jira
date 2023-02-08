package com.igsl.configmigration.priority;

import java.util.Comparator;

public class PriorityComparator implements Comparator<PriorityDTO> {

	@Override
	public int compare(PriorityDTO o1, PriorityDTO o2) {
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
