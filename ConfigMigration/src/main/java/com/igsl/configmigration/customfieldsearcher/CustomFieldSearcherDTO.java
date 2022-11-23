package com.igsl.configmigration.customfieldsearcher;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.priority.Priority;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class CustomFieldSearcherDTO extends JiraConfigItem {

	private String completeKey;
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		CustomFieldSearcher obj = (CustomFieldSearcher) o;
		// obj.getCustomFieldSearcherClauseHandler();
		this.completeKey = obj.getDescriptor().getCompleteKey();
//		obj.getSearchInformation();
//		obj.getSearchInputTransformer();
//		obj.getSearchRenderer();
	}

	@Override
	public String getUniqueKey() {
		return this.getCompleteKey();
	}

	@Override
	public String getInternalId() {
		return this.getCompleteKey();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getCompleteKey");
	}

	public String getCompleteKey() {
		return completeKey;
	}

	public void setCompleteKey(String completeKey) {
		this.completeKey = completeKey;
	}

}
