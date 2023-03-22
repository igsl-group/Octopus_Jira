package com.igsl.configmigration.report.v1;

import java.util.ArrayList;
import java.util.List;

import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;

public class MergeReportData {

	public String id;
	public String objectType;
	public String uniqueKey;
	public String displayName;
	public String newId;
	public String newUniqueKey;
	public Boolean result;
	public List<String> warnings = new ArrayList<>();
	public List<String> errors = new ArrayList<>();
	
	public MergeReportData(JiraConfigDTO dto) {
		this.id = dto.getInternalId();
		JiraConfigUtil util = JiraConfigTypeRegistry.getConfigUtil(dto.getUtilClass());
		this.objectType = util.getName();
		this.uniqueKey = dto.getUniqueKey();
		this.displayName = dto.getConfigName();
	}

	public void setNewDTO(JiraConfigDTO dto) {
		if (dto != null) {
			this.newId = dto.getInternalId();
			this.newUniqueKey = dto.getUniqueKey();
		}
	}
	
	public void setResult(Boolean result) {
		this.result = result;
	}
	
	public void addWarning(String warning) {
		this.warnings.add(warning);
	}
	
	public void addError(String error) {
		this.errors.add(error);
	}
	
	public String getId() {
		return id;
	}

	public String getObjectType() {
		return objectType;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public String getNewId() {
		return newId;
	}

	public String getNewUniqueKey() {
		return newUniqueKey;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Boolean getResult() {
		return result;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public List<String> getErrors() {
		return errors;
	}
}
