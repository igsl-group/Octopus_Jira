package com.igsl.configmigration.permissionscheme;

import java.util.Comparator;

public class PermissionSchemeEntityComparator implements Comparator<PermissionSchemeEntityDTO> {

	@Override
	public int compare(PermissionSchemeEntityDTO o1, PermissionSchemeEntityDTO o2) {
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 != null && o2 == null) {
			return 1;
		} else if (o1 == null && o2 != null) {
			return -1;
		} else {
			return o1.getConfigName().compareTo(o2.getConfigName());
		}
	}

}
