package com.igsl.configmigration.issuesecuritylevelscheme;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueSecurityLevelUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(IssueSecurityLevelUtil.class);
	private static final IssueSecurityLevelManager LEVEL_MANAGER = 
			ComponentAccessor.getIssueSecurityLevelManager();
	private static final IssueSecuritySchemeManager SCHEME_MANAGER = 
			ComponentAccessor.getComponent(IssueSecuritySchemeManager.class);
	
	@Override
	public String getName() {
		return "Issue Security Level";
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		IssueSecurityLevel s = LEVEL_MANAGER.getSecurityLevel(idAsLong);
		if (s != null) {
			IssueSecurityLevelDTO item = new IssueSecurityLevelDTO();
			item.setJiraObject(s);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (IssueSecurityLevel s : LEVEL_MANAGER.getAllIssueSecurityLevels()) {
			if (Integer.toString(s.hashCode()).equals(uniqueKey)) {
	 			IssueSecurityLevelDTO item = new IssueSecurityLevelDTO();
				item.setJiraObject(s);
				return item;
			}
		}
		return null;
	}

	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		throw new Exception("IssueSecurityLevelDTO is read only");
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return IssueSecurityLevelDTO.class;
	}

	@Override
	public boolean isVisible() {
		// Referenced by IssueSecurityLevelSchemeDTO only
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		// Filter ignored
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		if (params != null && params.length == 1) {
			for (IssueSecurityLevel s : LEVEL_MANAGER.getAllIssueSecurityLevels()) {
				IssueSecurityLevelDTO item = new IssueSecurityLevelDTO();
				item.setJiraObject(s, params);
				result.put(item.getUniqueKey(), item);
			}
		}
		return result;
	}

}
