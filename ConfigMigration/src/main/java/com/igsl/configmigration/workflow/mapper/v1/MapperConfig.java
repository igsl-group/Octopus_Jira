package com.igsl.configmigration.workflow.mapper.v1;

import net.java.ao.Entity;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

/**
 * Workflow mapping configuration.
 */
@Table(value = "MapperConfig")
public interface MapperConfig extends Entity {

	void setDisabled(boolean disabled);
	boolean isDisabled();

	@StringLength(value = StringLength.UNLIMITED)
	void setRegex(String regex);
	String getRegex();

	@StringLength(value = StringLength.UNLIMITED)
	void setCaptureGroups(String captureGroups);
	String getCaptureGroups();
	
	@StringLength(value = StringLength.UNLIMITED)
	void setReplacement(String replacement);
	String getReplacement();
	
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
