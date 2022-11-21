package com.igsl.configmigration.issuetypescheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.issuetype.IssueTypeConfigItem;
import com.igsl.configmigration.issuetype.IssueTypeConfigUtil;
import com.igsl.configmigration.optionset.OptionSetConfigItem;
import com.igsl.configmigration.project.ProjectConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeSchemeConfigItem extends JiraConfigItem {

	private static final Logger LOGGER = Logger.getLogger(IssueTypeSchemeConfigItem.class);
	private static IssueTypeSchemeManager MANAGER = ComponentAccessor.getComponent(IssueTypeSchemeManager.class);
	private static IssueTypeConfigUtil ISSUE_TYPE_UTIL = new IssueTypeConfigUtil();
	private static OptionSetManager OPTION_SET_MANAGER = ComponentAccessor.getComponent(OptionSetManager.class);
	
	public static final String KEY_ISSUE_TYPES = "Issue Types";
	public static final String KEY_PROJECTS = "Projects";
	public static final String KEY_DESCRIPTION = "Description";
	public static final String KEY_OPTION_SET = "Option Set";
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		FieldConfigScheme obj = (FieldConfigScheme) o;
		Map<String, String> map = this.getMap();
		List<IssueTypeConfigItem> issueTypes = new ArrayList<>();
		// getIssueTypes() always return nothing. Need to call IssueTypeSchemeManager in newer Jira version.
		for (IssueType type : MANAGER.getIssueTypesForScheme(obj)) {
			if (type != null) {
				IssueTypeConfigItem item = new IssueTypeConfigItem();
				item.setJiraObject(type);
				issueTypes.add(item);
			}
		}
		map.put(KEY_ISSUE_TYPES, OM.writeValueAsString(issueTypes));
		map.put(KEY_ID, Long.toString(obj.getId()));
		map.put(KEY_NAME, obj.getName());
		List<ProjectConfigItem> projects = new ArrayList<>();
		for (Project p : obj.getAssociatedProjectObjects()) {
			if (p != null) {
				ProjectConfigItem item = new ProjectConfigItem();
				item.setJiraObject(p);
				projects.add(item);
			}
		}
		map.put(KEY_PROJECTS, OM.writeValueAsString(projects));
		FieldConfig fieldConfig = obj.getOneAndOnlyConfig();
		OptionSet optionSet = OPTION_SET_MANAGER.getOptionsForConfig(fieldConfig);
		OptionSetConfigItem optionSetItem = new OptionSetConfigItem();
		optionSetItem.setJiraObject(optionSet);
		map.put(KEY_OPTION_SET, OM.writeValueAsString(optionSetItem));		
		map.put(KEY_DESCRIPTION, obj.getDescription());
	}

	@Override
	public int compareTo(JiraConfigItem o) {
		if (o != null) {
			try {
				return compare(
						this, o,
						new String[] {
								KEY_OPTION_SET,
								KEY_ISSUE_TYPES,
								KEY_NAME,
								KEY_DESCRIPTION,
								KEY_PROJECTS
						},
						new TypeReference[] {
								new TypeReference<OptionSetConfigItem>() {},
								new TypeReference<List<IssueTypeConfigItem>>() {},
								null,
								null,
								new TypeReference<List<ProjectConfigItem>>() {}
						});
			} catch (Exception e) {
				return 1;
			}
		}
		return 1;
	}

}
