package com.igsl.configmigration.issuesecuritylevelscheme;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelScheme;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
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
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
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
			// Merge levels - match by name, update description
			Map<String, String> targetMap = new HashMap<>();
			for (IssueSecurityLevelDTO item : src.getIssueSecurityLevels()) {
				targetMap.put(item.getName(), item.getDescription());
			}			
			for (IssueSecurityLevel lvl : LEVEL_MANAGER.getIssueSecurityLevels(id)) {
				String levelName = lvl.getName();
				if (targetMap.containsKey(levelName)) {
					// Update level description
					IssueSecurityLevel updateLvl = new IssueSecurityLevelImpl(lvl.getId(), levelName, targetMap.get(levelName), id);
					LEVEL_MANAGER.updateIssueSecurityLevel(updateLvl);
					// Remove from targetMap
					targetMap.remove(lvl.getName());
				} else {
					// Delete removed level
					LEVEL_MANAGER.deleteSecurityLevel(lvl.getId());
				}
			}
			// Add remaining items in targetMap
			for (Map.Entry<String, String> entry : targetMap.entrySet()) {
				IssueSecurityLevel newLvl = new IssueSecurityLevelImpl(null, entry.getKey(), entry.getValue(), id);
				LEVEL_MANAGER.createIssueSecurityLevel(newLvl);
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
