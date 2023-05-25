package com.igsl.configmigration.workflow.mapper.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Property inclusion filter for Jackson XML. POJO should implement EmptyCheck.
 * 
 * To set it up with code as default: 
 * ObjectMapper.setDefaultPropertyInclusion(new JsonInclude.Value(EmptyFilter.class.getAnnotation(JsonInclude.class)))
 * 
 * Or use this annotation on a field:
 * @JsonInclude(value = Include.CUSTOM, valueFilter = EmptyFilter.class)
 * 
 * The JsonInclude annotation on EmptyFilter is only used for the ObjectMapper setup above.
 */
@JsonInclude(value = Include.CUSTOM, valueFilter = EmptyFilter.class)
public class EmptyFilter {

	@Override
	public boolean equals(Object o) {
		// Return true to exclude
		if (o == null) {
			return true;
		}
		if (o instanceof EmptyCheck) {
			EmptyCheck item = (EmptyCheck) o;
			boolean r = item.isEmpty();
			return r;
		}
		return false;
	}
	
}