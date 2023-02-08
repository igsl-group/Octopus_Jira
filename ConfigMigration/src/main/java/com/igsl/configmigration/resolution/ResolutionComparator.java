package com.igsl.configmigration.resolution;

import java.util.Comparator;

public class ResolutionComparator implements Comparator<ResolutionDTO> {

	@Override
	public int compare(ResolutionDTO o1, ResolutionDTO o2) {
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
