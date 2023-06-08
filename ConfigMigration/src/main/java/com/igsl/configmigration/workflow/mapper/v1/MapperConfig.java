package com.igsl.configmigration.workflow.mapper.v1;

import net.java.ao.Entity;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

/**
 * Workflow mapping configuration.
 */
@Table(value = "MapperConfig")
public interface MapperConfig extends Entity {

	// TODO Enhancement: Swap array to a list of formats? JSON? CSV? Regex for parsing items?
	
	void setDisabled(boolean disabled);
	boolean isDisabled();

	void setArray(boolean array);
	boolean isArray();
	
	@StringLength(value = StringLength.UNLIMITED)
	void setXPath(String xPath);
	String getXPath();
	
	@StringLength(value = StringLength.UNLIMITED)
	void setDescription(String description);
	String getDescription();

	@StringLength(value = StringLength.UNLIMITED)
	void setObjectType(String objectType);
	String getObjectType();
}
