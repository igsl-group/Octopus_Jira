package com.igsl.configmigration;

import java.util.ArrayList;
import java.util.List;

public class MergeResult {
	private JiraConfigDTO newDTO;
	private List<String> warnings = new ArrayList<>();
	
	public MergeResult() {
		this.newDTO = null;
	}
	
	public MergeResult(JiraConfigDTO dto) {
		this.newDTO = dto;
	}

	public JiraConfigDTO getNewDTO() {
		return newDTO;
	}

	public void setNewDTO(JiraConfigDTO newDTO) {
		this.newDTO = newDTO;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}
	
	public void addWarning(String msg) {
		this.warnings.add(msg);
	}
}
