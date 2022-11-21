package com.igsl.configmigration.issuesecuritylevelscheme;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelScheme;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueSecurityLevelSchemeConfigUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(IssueSecurityLevelSchemeConfigUtil.class);
	private static final IssueSecuritySchemeManager SCHEME_MANAGER = 
			ComponentAccessor.getComponent(IssueSecuritySchemeManager.class);
	
	@Override
	public String getName() {
		return "Issue Security Scheme";
	}
	
	@Override
	public TypeReference<?> getTypeReference() {
		return new TypeReference<Map<String, IssueSecurityLevelSchemeConfigItem>>() {};
	}
	
	@Override
	public Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigItem> result = new HashMap<>();
		for (IssueSecurityLevelScheme s : SCHEME_MANAGER.getIssueSecurityLevelSchemes()) {
			IssueSecurityLevelSchemeConfigItem item = new IssueSecurityLevelSchemeConfigItem();
			item.setJiraObject(s);
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
		for (IssueSecurityLevelScheme s : SCHEME_MANAGER.getIssueSecurityLevelSchemes()) {
			if (s.getName().equals(identifier)) {
				return s;
			}
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public Object merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception {
		IssueSecurityLevelScheme original = null;
		if (oldItem != null) {
			if (oldItem.getJiraObject() != null) {
				original = (IssueSecurityLevelScheme) oldItem.getJiraObject();
			} else {
				original = (IssueSecurityLevelScheme) findObject(oldItem.getUniqueKey());
			}
		} else {
			original = (IssueSecurityLevelScheme) findObject(newItem.getUniqueKey());
		}
		IssueSecurityLevelSchemeConfigItem src = (IssueSecurityLevelSchemeConfigItem) newItem;
		Scheme result = null;
		if (original != null) {
			// Update
			Scheme s = new Scheme(original.getId(), src.getName(), src.getDescription(), null);
			SCHEME_MANAGER.updateScheme(s);
			result = s;
		} else {
			// Create
			result = SCHEME_MANAGER.createSchemeObject(src.getName(), src.getDescription());
		}
		Long defaultLevel = null;
		if (result != null) {
			// Merge levels
			IssueSecurityLevelConfigUtil util = new IssueSecurityLevelConfigUtil();
			for (IssueSecurityLevelConfigItem item : src.getIssueSecurityLevels()) {
				item.setSchemeId(result.getId());
				IssueSecurityLevel level = (IssueSecurityLevel) util.merge(null, item);
				if (item.getId().equals(src.getDefaultSecurityLevelId())) {
					defaultLevel = level.getId();
				}
			}
		}
		// Set default level, no choice but to use deprecated APIs
		if (defaultLevel != null) {
			GenericValue scheme = SCHEME_MANAGER.getScheme(result.getId());
			scheme.set("defaultlevel", defaultLevel);
			SCHEME_MANAGER.updateScheme(scheme);
		}
		return result;
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
