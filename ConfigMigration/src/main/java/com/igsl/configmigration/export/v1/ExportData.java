package com.igsl.configmigration.export.v1;

import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

/*
 * Notes: 
 * Use @Table to separate interface name with table name
 * Use @Mutator on setter and @Accessor on getter to separate field names with column names
 * Column names are case sensitive
 * Use stream() instead of find() and get() if huge amount of data is expected
 * Cascade is not supported; so modify data in correct order.
 * ActiveObjects can only be used after plugin is up and running.
 * Create UpgradeTask to migrate data if Entity changes. 
 * 
 * Last Upgrade Task: 
 * SELECT * from propertystring where id in ( SELECT id FROM propertyentry where property_key like ‘%plugin_key%’);
 * SELECT * from propertystring where id in ( SELECT id FROM propertyentry where property_key like ‘%plugin_table_prefix%’);
 * 
 * Last plugin version: 
 * UPDATE pluginversion SET pluginversion = ‘1.0.0’ WHERE pluginkey LIKE ‘%plugin_key%’;
 * 
 * Upgrade task: 
 * https://developer.atlassian.com/server/framework/atlassian-sdk/handling-ao-upgrade-tasks/
 * 
 * Active object best practice: 
 * https://developer.atlassian.com/server/framework/atlassian-sdk/best-practices-for-developing-with-active-objects/
 */ 

@Table(value = "ExportData")
public interface ExportData extends Entity {

	@StringLength(value = StringLength.UNLIMITED)
	void setContent(String content);
	String getContent();
	
	@StringLength(value = StringLength.UNLIMITED) 
	void setDescription(String description);
	String getDescription();
	
	void setExportDate(Date exportDate);
	Date getExportDate();
	
	void setExportUser(String username);
	String getExportUser();
}
