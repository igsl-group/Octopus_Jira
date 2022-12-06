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
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;

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
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (IssueSecurityLevelScheme s : SCHEME_MANAGER.getIssueSecurityLevelSchemes()) {
			IssueSecurityLevelSchemeDTO item = new IssueSecurityLevelSchemeDTO();
			item.setJiraObject(s);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}


	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		IssueSecurityLevelScheme s = SCHEME_MANAGER.getIssueSecurityLevelScheme(idAsLong);
		if (s != null) {
			IssueSecurityLevelSchemeDTO item = new IssueSecurityLevelSchemeDTO();
			item.setJiraObject(s);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (IssueSecurityLevelScheme s : SCHEME_MANAGER.getIssueSecurityLevelSchemes()) {
			if (s.getName().equals(uniqueKey)) {
				IssueSecurityLevelSchemeDTO item = new IssueSecurityLevelSchemeDTO();
				item.setJiraObject(s);
				return item;
			}
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		final IssueSecurityLevelUtil LEVEL_UTIL = 
				(IssueSecurityLevelUtil) JiraConfigTypeRegistry.getConfigUtil(IssueSecurityLevelUtil.class);
		IssueSecurityLevelSchemeDTO original = null;
		if (oldItem != null) {
			original = (IssueSecurityLevelSchemeDTO) oldItem;
		} else {
			original = (IssueSecurityLevelSchemeDTO) findByDTO(newItem);
		}
		IssueSecurityLevelSchemeDTO src = (IssueSecurityLevelSchemeDTO) newItem;
		IssueSecurityLevelSchemeDTO result = null;
		Scheme scheme = null;
		if (original != null) {
			// Update
			Scheme s = new Scheme(original.getId(), src.getName(), src.getDescription(), null);	
			// TODO last parameter, what is it
			SCHEME_MANAGER.updateScheme(s);
			result = (IssueSecurityLevelSchemeDTO) findByInternalId(Long.toString(original.getId()));
		} else {
			// Create
			scheme = SCHEME_MANAGER.createSchemeObject(src.getName(), src.getDescription());
			result = (IssueSecurityLevelSchemeDTO) findByInternalId(Long.toString(scheme.getId()));
		}
		Long defaultLevel = null;
		if (result != null) {
			// Merge levels
			for (IssueSecurityLevelDTO item : src.getIssueSecurityLevels()) {
				item.setSchemeId(result.getId());
				IssueSecurityLevelDTO level = (IssueSecurityLevelDTO) LEVEL_UTIL.merge(null, item);
				if (item.getId().equals(src.getDefaultSecurityLevelId())) {
					defaultLevel = level.getId();
				}
			}
		}
		// Set default level, no choice but to use deprecated APIs
		if (defaultLevel != null) {
			GenericValue gv = SCHEME_MANAGER.getScheme(result.getId());
			gv.set("defaultlevel", defaultLevel);
			SCHEME_MANAGER.updateScheme(scheme);
		}
		return result;
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return IssueSecurityLevelSchemeDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}


}
