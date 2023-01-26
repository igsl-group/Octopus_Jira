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
public class FieldScreenSchemeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(FieldScreenSchemeUtil.class);
	private static FieldScreenSchemeManager MANAGER = ComponentAccessor.getFieldScreenSchemeManager();
	private static FieldScreenManager SCREEN_MANAGER = ComponentAccessor.getFieldScreenManager();
	
	@Override
	public String getName() {
		return "Screen Scheme";
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (FieldScreenScheme it : MANAGER.getFieldScreenSchemes()) {
			FieldScreenSchemeDTO item = new FieldScreenSchemeDTO();
			item.setJiraObject(it);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		FieldScreenScheme s = MANAGER.getFieldScreenScheme(idAsLong);
		if (s != null) {
			FieldScreenSchemeDTO item = new FieldScreenSchemeDTO();
			item.setJiraObject(s);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (FieldScreenScheme it : MANAGER.getFieldScreenSchemes()) {
			if (uniqueKey.equals(it.getName())) {
				FieldScreenSchemeDTO item = new FieldScreenSchemeDTO();
				item.setJiraObject(it);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		FieldScreenSchemeDTO original = null;
		if (oldItem != null) {
			original = (FieldScreenSchemeDTO) oldItem;
		} else {
			original = (FieldScreenSchemeDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		FieldScreenUtil fieldScreenUtil = (FieldScreenUtil) 
				JiraConfigTypeRegistry.getConfigUtil(FieldScreenUtil.class);
		ScreenableIssueOperationUtil issueOperationUtil = (ScreenableIssueOperationUtil)
				JiraConfigTypeRegistry.getConfigUtil(ScreenableIssueOperationUtil.class);
		FieldScreenScheme originalJira = (original != null)? (FieldScreenScheme) original.getJiraObject(): null;
		FieldScreenSchemeDTO src = (FieldScreenSchemeDTO) newItem;
		if (original != null) {
			// Update
			originalJira.setDescription(src.getDescription());
			originalJira.setName(src.getName());
			originalJira.getFieldScreenSchemeItems().clear();
			MANAGER.updateFieldScreenScheme(originalJira);
			// Update items
			for (FieldScreenSchemeItemDTO dto : src.getFieldScreenSchemeItems()) {
				FieldScreenSchemeItem item = new FieldScreenSchemeItemImpl(MANAGER, SCREEN_MANAGER);
				FieldScreenDTO fs = (FieldScreenDTO) fieldScreenUtil.findByDTO(dto.getFieldScreen());
				item.setFieldScreen((FieldScreen) fs.getJiraObject());
				item.setFieldScreenScheme(originalJira);
				ScreenableIssueOperationDTO op = (ScreenableIssueOperationDTO) 
						issueOperationUtil.findByUniqueKey(dto.getIssueOperation().getNameKey());
				if (op != null) {
					item.setIssueOperation((ScreenableIssueOperation) op.getJiraObject());
				} else {
					item.setIssueOperation(null);
				}
				MANAGER.createFieldScreenSchemeItem(item);
			}
			FieldScreenSchemeDTO updated = new FieldScreenSchemeDTO();
			updated.setJiraObject(originalJira);
			return updated;
		} else {
			// Create
			FieldScreenScheme scheme = new FieldScreenSchemeImpl(MANAGER);
			scheme.setDescription(src.getDescription());
			scheme.setName(src.getName());
			MANAGER.createFieldScreenScheme(scheme);
			// Create items
			for (FieldScreenSchemeItemDTO item : src.getFieldScreenSchemeItems()) {
				FieldScreenSchemeItem it = new FieldScreenSchemeItemImpl(MANAGER, SCREEN_MANAGER);
				FieldScreenDTO fs = (FieldScreenDTO) fieldScreenUtil.findByDTO(item.getFieldScreen());
				it.setFieldScreen((FieldScreen) fs.getJiraObject());
				it.setFieldScreenScheme(scheme);
				ScreenableIssueOperationDTO op = (ScreenableIssueOperationDTO) 
						issueOperationUtil.findByUniqueKey(item.getIssueOperation().getUniqueKey());
				if (op != null) {
					it.setIssueOperation((ScreenableIssueOperation) op.getJiraObject());
				} else {
					it.setIssueOperation(null);
				}
				MANAGER.createFieldScreenSchemeItem(it);
			}
			FieldScreenSchemeDTO created = new FieldScreenSchemeDTO();
			created.setJiraObject(scheme);
			return created;
		}
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return FieldScreenSchemeDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

}
