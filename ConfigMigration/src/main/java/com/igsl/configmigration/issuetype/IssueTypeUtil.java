package com.igsl.configmigration.issuetype;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
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

	public MergeResult merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
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
		AvatarDTO av = (AvatarDTO) avatarUtil.merge(null, src.getAvatarConfigItem()).getNewDTO();
		if (original != null) {
			// Update
			ISSUE_MANAGER.updateIssueType(originalJira, src.getName(), src.getDescription(), av.getId());
			result.setNewDTO(original);
		} else {
			// Create
			IssueType createdJira = ISSUE_MANAGER.createIssueType(src.getName(), src.getDescription(), av.getId());
			IssueTypeDTO created = new IssueTypeDTO();
			created.setJiraObject(createdJira);
			result.setNewDTO(created);
		}
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return IssueTypeDTO.class;
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
		for (IssueType it : ISSUE_MANAGER.getIssueTypes()) {
			IssueTypeDTO item = new IssueTypeDTO();
			item.setJiraObject(it);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
