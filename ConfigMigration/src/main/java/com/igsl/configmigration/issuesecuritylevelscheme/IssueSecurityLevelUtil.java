package com.igsl.configmigration.issuesecuritylevelscheme;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;

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
	public Map<String, JiraConfigDTO> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (IssueSecurityLevel s : LEVEL_MANAGER.getAllIssueSecurityLevels()) {
			IssueSecurityLevelDTO item = new IssueSecurityLevelDTO();
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
		for (IssueSecurityLevel s : LEVEL_MANAGER.getAllIssueSecurityLevels()) {
			if (s.getName().equals(identifier)) {
				return s;
			}
		}
		return null;
	}
	
	public Object merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		IssueSecurityLevel original = null;
		if (oldItem != null) {
			if (oldItem.getJiraObject() != null) {
				original = (IssueSecurityLevel) oldItem.getJiraObject();
			} else {
				original = (IssueSecurityLevel) findObject(oldItem.getUniqueKey());
			}
		} else {
			original = (IssueSecurityLevel) findObject(newItem.getUniqueKey());
		}
		IssueSecurityLevelDTO src = (IssueSecurityLevelDTO) newItem;
		if (original != null) {
			// Update
			IssueSecurityLevelImpl item = new IssueSecurityLevelImpl(
					original.getId(), src.getName(), src.getDescription(), src.getSchemeId());
			return LEVEL_MANAGER.updateIssueSecurityLevel(item);
		} else {
			// Create
			return LEVEL_MANAGER.createIssueSecurityLevel(src.getSchemeId(), src.getName(), src.getDescription());
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

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return IssueSecurityLevelDTO.class;
	}

	@Override
	public boolean isPublic() {
		// Referenced by IssueSecurityLevelSchemeDTO only
		return false;
	}

}
