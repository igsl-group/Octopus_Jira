package com.igsl.configmigration.issuetype;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;
import com.igsl.configmigration.avatar.AvatarDTO;
import com.igsl.configmigration.avatar.AvatarUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(IssueTypeUtil.class);
	private static IssueTypeManager ISSUE_MANAGER = ComponentAccessor.getComponent(IssueTypeManager.class);
	
	@Override
	public String getName() {
		return "Issue Type";
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (IssueType it : ISSUE_MANAGER.getIssueTypes()) {
			IssueTypeDTO item = new IssueTypeDTO();
			item.setJiraObject(it);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		IssueType it = ISSUE_MANAGER.getIssueType(id);
		if (it != null) {
			IssueTypeDTO item = new IssueTypeDTO();
			item.setJiraObject(it);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (IssueType it : ISSUE_MANAGER.getIssueTypes()) {
			if (it.getName().equals(uniqueKey)) {
				IssueTypeDTO item = new IssueTypeDTO();
				item.setJiraObject(it);
				return item;
			}
		}
		return null;
	}

	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		IssueTypeDTO original = null;
		if (oldItem != null) {
			original = (IssueTypeDTO) oldItem;
		} else {
			original = (IssueTypeDTO) findByDTO(newItem);
		}
		IssueType originalJira = null;
		if (original != null) {
			originalJira = (IssueType) original.getJiraObject();
		}
		IssueTypeDTO src = (IssueTypeDTO) newItem;
		// Avatar
		AvatarUtil avatarUtil = new AvatarUtil();
		AvatarDTO av = (AvatarDTO) avatarUtil.merge(null, src.getAvatarConfigItem());
		if (original != null) {
			// Update
			ISSUE_MANAGER.updateIssueType(originalJira, src.getName(), src.getDescription(), av.getId());
			return original;
		} else {
			// Create
			IssueType createdJira = ISSUE_MANAGER.createIssueType(src.getName(), src.getDescription(), av.getId());
			IssueTypeDTO created = new IssueTypeDTO();
			created.setJiraObject(createdJira);
			return created;
		}
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return IssueTypeDTO.class;
	}

	@Override
	public boolean isPublic() {
		return true;
	}

}
