package com.igsl.configmigration.customfieldsearcher;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

/**
 * CustomFieldSearcher wrapper.
 * This object type should be read-only; there is no way to add new searcher except via plugin.
 * 
 * So many details and nested objects are not needed. 
 * Just keep enough fields to find it in server. 
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class CustomFieldSearcherDTO extends JiraConfigDTO {

	private String completeKey;

	/**
	 * #0: CustomFieldType
	 */
	@Override
	protected int getObjectParameterCount() {
		return 1;
	}
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		CustomFieldSearcher obj = (CustomFieldSearcher) o;
		// obj.getCustomFieldSearcherClauseHandler();
		this.completeKey = obj.getDescriptor().getCompleteKey();
//		obj.getSearchInformation();
//		obj.getSearchInputTransformer();
//		obj.getSearchRenderer();
		this.uniqueKey = this.completeKey;
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Complete Key", new JiraConfigProperty(this.completeKey));
		return r;
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

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return CustomFieldSearcherUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return CustomFieldSearcher.class;
	}

}
