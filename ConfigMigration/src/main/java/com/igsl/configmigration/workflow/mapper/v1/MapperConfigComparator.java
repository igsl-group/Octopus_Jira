package com.igsl.configmigration.workflow.mapper.v1;

import java.util.Comparator;

public class MapperConfigComparator implements Comparator<MapperConfig> {

	@Override
	public int compare(MapperConfig o1, MapperConfig o2) {
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 != null && o2 == null) {
			return 1;
		} else if (o1 == null && o2 != null) {
			return -1;
		} else {
			return o1.getDescription().compareTo(o2.getDescription());
		}
	}

}
