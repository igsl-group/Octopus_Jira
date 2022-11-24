package com.igsl.configmigration.customfield;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.ConfigUtil;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;
import com.igsl.configmigration.customfieldsearcher.CustomFieldSearcherUtil;
import com.igsl.configmigration.customfieldtype.CustomFieldTypeUtil;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.issuetype.IssueTypeUtil;

@ConfigUtil
@JsonDeserialize(using = JsonDeserializer.None.class)
public class CustomFieldUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(CustomFieldUtil.class);
	private static CustomFieldManager CUSTOM_FIELD_MANAGER = ComponentAccessor.getCustomFieldManager();
	private static CustomFieldTypeUtil CUSTOM_FIELD_TYPE_UTIL = new CustomFieldTypeUtil();
	private static CustomFieldSearcherUtil CUSTOM_FIELD_SEARCHER_UTIL = new CustomFieldSearcherUtil();
	private static IssueTypeUtil ISSUE_TYPE_UTIL = new IssueTypeUtil();
	
	@Override
	public String getName() {
		return "Custom Field";
	}
	
	@Override
	public TypeReference<?> getTypeReference() {
		return new TypeReference<Map<String, CustomFieldDTO>>() {};
	}
	
	@Override
	public Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigItem> result = new TreeMap<>();
		for (CustomField cf : CUSTOM_FIELD_MANAGER.getCustomFieldObjects()) {
			CustomFieldDTO item = new CustomFieldDTO();
			item.setJiraObject(cf);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	/**
	 * params[0]: name
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		String identifier = (String) params[0];
		for (CustomField cf : CUSTOM_FIELD_MANAGER.getCustomFieldObjects()) {
			if (cf.getName().equals(identifier)) {
				return cf;
			}
		}
		return null;
	}
	
	public Object merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception {
		CustomField original = null;
		if (oldItem != null) {
			if (oldItem.getJiraObject() != null) {
				original = (CustomField) oldItem.getJiraObject();
			} else {
				original = (CustomField) findObject(oldItem.getUniqueKey());
			}
		} else {
			original = (CustomField) findObject(newItem.getUniqueKey());
		}
		CustomFieldDTO src = (CustomFieldDTO) newItem;
		CustomFieldType<?, ?> fieldType = (CustomFieldType<?, ?>) CUSTOM_FIELD_TYPE_UTIL.findObject(
				src.getCustomFieldType().getUniqueKey());
		CustomFieldSearcher fieldSearcher = (CustomFieldSearcher) CUSTOM_FIELD_SEARCHER_UTIL.findObject(
				fieldType, src.getCustomFieldSearcher().getCompleteKey());
		if (original != null) {
			// Update
			CUSTOM_FIELD_MANAGER.updateCustomField(
					original.getIdAsLong(), src.getName(), src.getDescription(), fieldSearcher);
			return original;
		} else {
			// Create
			List<IssueType> issueTypes = new ArrayList<>();
			for (IssueTypeDTO item : src.getAssociatedIssueTypes()) {
				IssueType it = (IssueType) ISSUE_TYPE_UTIL.findObject(item.getUniqueKey());
				if (it != null) {
					issueTypes.add(it);
				}
			}
			return CUSTOM_FIELD_MANAGER.createCustomField(
					src.getName(), 
					src.getDescription(), 
					fieldType,
					fieldSearcher, 
					null, 	// TODO How to find context? via config?
					issueTypes);
		}
	}
	
	@Override
	public void merge(Map<String, ImportData> items) throws Exception {
		for (ImportData data : items.values()) {
			try {
				merge(data.getServer(), data.getData());
				data.setImportResult("Updated");
			} catch (Exception ex) {
				data.setImportResult(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
				throw ex;
			}
		}
	}

}
