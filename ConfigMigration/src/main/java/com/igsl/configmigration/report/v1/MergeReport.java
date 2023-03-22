package com.igsl.configmigration.report.v1;

import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

@Table(value = "MergeReport")
public interface MergeReport extends Entity {

	void setMergeDate(Date mergeDate);
	Date getMergeDate();
	
	void setMergeUser(String username);
	String getMergeUser();
	
	@StringLength(value = StringLength.UNLIMITED)
	void setContent(String content);
	String getContent();

	void setTotalProjectCount(long count);
	long getTotalProjectCount();
	
	void setSuccessProjectCount(long count);
	long getSuccessProjectCount();
	
	void setFailedProjectCount(long count);
	long getFailedProjectCount();
	
	void setTotalObjectCount(long count);
	long getTotalObjectCount();
	
	void setSuccessObjectCount(long count);
	long getSuccessObjectCount();
	
	void setFailedObjectCount(long count);
	long getFailedObjectCount();
	
}
