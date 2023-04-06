package com.igsl.configmigration.avatar;

import java.util.Comparator;

public class AvatarComparator implements Comparator<AvatarDTO> {

	@Override
	public int compare(AvatarDTO o1, AvatarDTO o2) {
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 != null && o2 == null) {
			return 1;
		} else if (o1 == null && o2 != null) {
			return -1;
		} else {
			return o1.getFileName().compareTo(o2.getFileName());
		}
	}

}
