package com.igsl.configmigration.issuetypescheme;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;
import com.igsl.configmigration.issuetype.IssueTypeConfigItem;
import com.igsl.configmigration.optionset.OptionSetConfigItem;
import com.igsl.configmigration.project.ProjectConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeSchemeConfigUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(IssueTypeSchemeConfigUtil.class);
	private static IssueTypeSchemeManager MANAGER = ComponentAccessor.getComponent(IssueTypeSchemeManager.class);
	
	@Override
	public String getName() {
		return "Issue Type Scheme";
	}
	
	@Override
	public TypeReference<?> getTypeReference() {
		return new TypeReference<Map<String, IssueTypeSchemeConfigItem>>() {};
	}
	
	@Override
	public Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigItem> result = new HashMap<>();
		for (FieldConfigScheme scheme : MANAGER.getAllSchemes()) {
			IssueTypeSchemeConfigItem item = new IssueTypeSchemeConfigItem();
			item.setJiraObject(scheme);
			result.put(item.getKey(), item);
		}
		return result;
	}

	/**
	 * params[0]: identifier
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		assert params.length == 1 && String.class.isAssignableFrom(params[0].getClass());
		String identifier = (String) params[0];
		for (FieldConfigScheme scheme : MANAGER.getAllSchemes()) {
			if (scheme.getName().equals(identifier)) {
				return scheme;
			}
		}
		return null;
	}
	
	@Override
	public boolean merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception {
		OptionSet original = null;
		if (oldItem != null && oldItem.getJiraObject() != null) {
			original = (OptionSet) oldItem.getJiraObject();
		} else {
			original = (OptionSet) findObject(oldItem.getKey());
		}
		String name = newItem.getMap().get(IssueTypeSchemeConfigItem.KEY_NAME);
		String description = newItem.getMap().get(IssueTypeSchemeConfigItem.KEY_DESCRIPTION);
		String issueTypeString = newItem.getMap().get(IssueTypeSchemeConfigItem.KEY_ISSUE_TYPES);
		List<IssueTypeConfigItem> issueTypes = 
				OM.readValue(issueTypeString, new TypeReference<List<IssueTypeConfigItem>>() {});
		String optionSetString = newItem.getMap().get(IssueTypeSchemeConfigItem.KEY_OPTION_SET);
		OptionSetConfigItem optionSet = 
				OM.readValue(optionSetString, new TypeReference<OptionSetConfigItem>() {});
		String projectString = newItem.getMap().get(IssueTypeSchemeConfigItem.KEY_PROJECTS);
		List<ProjectConfigItem> projects = 
				OM.readValue(projectString, new TypeReference<List<ProjectConfigItem>>() {});
		if (original != null) {
			// TODO
			return true;
		} else {
			// TODO
			return false;
		}
	}
	
	@Override
	public void merge(Map<String, ImportData> items) throws Exception {
		for (ImportData data : items.values()) {
			try {
				if (merge(data.getServer(), data.getData())) {
					data.setImportResult("Updated");
				} else {
					data.setImportResult("Created");
				}
			} catch (Exception ex) {
				data.setImportResult(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
				throw ex;
			}
		}
	}

}
