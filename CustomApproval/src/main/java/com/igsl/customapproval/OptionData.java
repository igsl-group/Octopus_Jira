package com.igsl.customapproval;

import java.util.ArrayList;
import java.util.List;

public class OptionData {
	
	private Long parentId;
	private Long sequence;
	private String value;
	private boolean defaultSelected;
	private List<OptionData> childOptions = new ArrayList<>();
	
	public OptionData(Long parentId, Long sequence, String value, boolean defaultSelected) {
		this.parentId = parentId;
		this.sequence = sequence;
		this.value = value;
		this.defaultSelected = defaultSelected;
	}
	
	public Long getParentId() {
		return parentId;
	}
	public Long getSequence() {
		return sequence;
	}
	public String getValue() {
		return value;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public List<OptionData> getChildOptions() {
		return childOptions;
	}
	public void setChildOptions(List<OptionData> childOptions) {
		this.childOptions = childOptions;
	}
	public boolean isDefaultSelected() {
		return defaultSelected;
	}
	public void setDefaultSelected(boolean defaultSelected) {
		this.defaultSelected = defaultSelected;
	}
}