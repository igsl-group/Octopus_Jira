package com.igsl.configmigration.fieldscreenscheme;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldscreen.FieldScreenDTO;
import com.igsl.configmigration.fieldscreen.FieldScreenUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ScreenableIssueOperationUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ScreenableIssueOperationUtil.class);
	
	@Override
	public String getName() {
		return "Issue Operation";
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (ScreenableIssueOperation item : IssueOperations.getIssueOperations()) {
			ScreenableIssueOperationDTO dto = new ScreenableIssueOperationDTO();
			dto.setJiraObject(item);
			result.put(dto.getUniqueKey(), dto);
		}
		return result;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		for (ScreenableIssueOperation item : IssueOperations.getIssueOperations()) {
			if (item.getId().equals(idAsLong)) {
				ScreenableIssueOperationDTO dto = new ScreenableIssueOperationDTO();
				dto.setJiraObject(item);
				return dto;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (ScreenableIssueOperation item : IssueOperations.getIssueOperations()) {
			if (item.getNameKey().equals(uniqueKey)) {
				ScreenableIssueOperationDTO dto = new ScreenableIssueOperationDTO();
				dto.setJiraObject(item);
				return dto;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		throw new Exception("Issue Operation is read only");
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ScreenableIssueOperationDTO.class;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

}
