package com.igsl.configmigration.issuesecuritylevelscheme;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelScheme;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueSecurityLevelSchemeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(IssueSecurityLevelSchemeUtil.class);
	private static final IssueSecuritySchemeManager SCHEME_MANAGER = 
			ComponentAccessor.getComponent(IssueSecuritySchemeManager.class);
	private static final IssueSecurityLevelManager LEVEL_MANAGER = 
			ComponentAccessor.getIssueSecurityLevelManager();
	
	@Override
	public String getName() {
		return "Issue Security Scheme";
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
	public MergeResult merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		final IssueSecurityLevelUtil LEVEL_UTIL = 
				(IssueSecurityLevelUtil) JiraConfigTypeRegistry.getConfigUtil(IssueSecurityLevelUtil.class);
		IssueSecurityLevelSchemeDTO original = null;
		if (oldItem != null) {
			original = (IssueSecurityLevelSchemeDTO) oldItem;
		} else {
			original = (IssueSecurityLevelSchemeDTO) findByDTO(newItem);
		}
		IssueSecurityLevelSchemeDTO src = (IssueSecurityLevelSchemeDTO) newItem;
		IssueSecurityLevelSchemeDTO created = null;
		Scheme scheme = null;
		Long id;
		if (original != null) {
			// Update
			Scheme s = new Scheme(
					original.getId(), "type", src.getName(), src.getDescription(), Collections.emptyList());
			// Note: In Jira source code, only the Long id is used to locate the existing object
			SCHEME_MANAGER.updateScheme(s);
			id = original.getId();
		} else {
			// Create
			scheme = SCHEME_MANAGER.createSchemeObject(src.getName(), src.getDescription());
			id = scheme.getId();
		}
		if (id != null) {
			Long defaultLevel = null;
			created = (IssueSecurityLevelSchemeDTO) findByInternalId(Long.toString(id));
			result.setNewDTO(created);
			// Recreate levels
			for (IssueSecurityLevel lvl : LEVEL_MANAGER.getIssueSecurityLevels(id)) {
				LEVEL_MANAGER.deleteSecurityLevel(lvl.getId());
			}
			// Merge levels
			for (IssueSecurityLevelDTO item : src.getIssueSecurityLevels()) {
				item.setSchemeId(id);
				item.setJiraObject(null, created);
				MergeResult mr = LEVEL_UTIL.merge(null, item);
				IssueSecurityLevelDTO level = (IssueSecurityLevelDTO) mr.getNewDTO();
				if (item.getId().equals(src.getDefaultSecurityLevelId())) {
					defaultLevel = level.getId();
				}
			}
			// Set default level, no choice but to use deprecated APIs
			if (defaultLevel != null) {
				GenericValue gv = SCHEME_MANAGER.getScheme(id);
				gv.set("defaultlevel", defaultLevel);
				SCHEME_MANAGER.updateScheme(gv);
			}
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

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (IssueSecurityLevelScheme s : SCHEME_MANAGER.getIssueSecurityLevelSchemes()) {
			IssueSecurityLevelSchemeDTO item = new IssueSecurityLevelSchemeDTO();
			item.setJiraObject(s);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
