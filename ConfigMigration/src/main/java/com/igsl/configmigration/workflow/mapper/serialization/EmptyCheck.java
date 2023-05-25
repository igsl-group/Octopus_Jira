package com.igsl.configmigration.workflow.mapper.serialization;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * POJO can implement this interface to indicate if it is empty (and this excluded from serialization).
 * 
 * You need to use this with EmptyFilter.
 * @see EmptyFilter 
 */
public interface EmptyCheck {
	@JsonIgnore
	public boolean isEmpty();
}
