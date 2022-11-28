package com.igsl.configmigration.issuesecuritylevelscheme;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelScheme;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;
import com.igsl.configmigration.annotation.ConfigUtil;

@ConfigUtil
@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueSecurityLevelSchemeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(IssueSecurityLevelSchemeUtil.class);
	private static final IssueSecuritySchemeManager SCHEME_MANAGER = 
			ComponentAccessor.getComponent(IssueSecuritySchemeManager.class);
	
	@Override
	public String getName() {
		return "Issue Security Scheme";
	}
	
	@Override
	public Map<String, JiraConfigDTO> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (IssueSecurityLevelScheme s : SCHEME_MANAGER.getIssueSecurityLevelSchemes()) {
			IssueSecurityLevelSchemeDTO item = new IssueSecurityLevelSchemeDTO();
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
	public Object merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
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
		IssueSecurityLevelSchemeDTO src = (IssueSecurityLevelSchemeDTO) newItem;
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
			IssueSecurityLevelUtil util = new IssueSecurityLevelUtil();
			for (IssueSecurityLevelDTO item : src.getIssueSecurityLevels()) {
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

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return IssueSecurityLevelSchemeDTO.class;
	}

}
