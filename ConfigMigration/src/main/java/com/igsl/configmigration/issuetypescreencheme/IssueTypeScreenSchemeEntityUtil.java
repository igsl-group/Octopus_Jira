package com.igsl.configmigration.issuetypescreencheme;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldconfigscheme.FieldConfigSchemeDTO;
import com.igsl.configmigration.fieldscreenscheme.FieldScreenSchemeDTO;
import com.igsl.configmigration.fieldscreenscheme.FieldScreenSchemeUtil;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.issuetype.IssueTypeUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeScreenSchemeEntityUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(IssueTypeScreenSchemeEntityUtil.class);
	private static final IssueTypeScreenSchemeManager MANAGER = 
			ComponentAccessor.getComponent(IssueTypeScreenSchemeManager.class);
	
	@Override
	public String getName() {
		return "Issue Type Screen Scheme Entity";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		IssueTypeScreenScheme itss = (IssueTypeScreenScheme) params[0];
		for (Object o : MANAGER.getIssueTypeScreenSchemeEntities(itss)) {
			IssueTypeScreenSchemeEntity e = (IssueTypeScreenSchemeEntity) o;
			if (e.getId().equals(idAsLong)) {
				IssueTypeScreenSchemeEntityDTO item = new IssueTypeScreenSchemeEntityDTO();
				item.setJiraObject(e, itss);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		IssueTypeScreenScheme itss = (IssueTypeScreenScheme) params[0];
		for (Object o : MANAGER.getIssueTypeScreenSchemeEntities(itss)) {
			IssueTypeScreenSchemeEntity e = (IssueTypeScreenSchemeEntity) o;
			IssueTypeScreenSchemeEntityDTO item = new IssueTypeScreenSchemeEntityDTO();
			item.setJiraObject(e, itss);
			if (item.getUniqueKey().equals(uniqueKey)) {
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		IssueTypeScreenSchemeEntityDTO original = null;
		if (oldItem != null) {
			original = (IssueTypeScreenSchemeEntityDTO) oldItem;
		} else {
			original = (IssueTypeScreenSchemeEntityDTO) findByDTO(newItem);
		}
		FieldScreenSchemeUtil fieldScreenSchemeUtil = (FieldScreenSchemeUtil) JiraConfigTypeRegistry.getConfigUtil(FieldScreenSchemeUtil.class); 
		IssueTypeUtil issueTypeUtil = (IssueTypeUtil) JiraConfigTypeRegistry.getConfigUtil(IssueTypeUtil.class); 
		IssueTypeScreenSchemeEntityDTO src = (IssueTypeScreenSchemeEntityDTO) newItem;
		FieldScreenSchemeDTO fieldScreenScheme = (FieldScreenSchemeDTO) fieldScreenSchemeUtil.findByDTO(src.getFieldScreenScheme());
		IssueTypeDTO issueType = null;
		if (src.getIssueType() != null) {
			issueType = (IssueTypeDTO) issueTypeUtil.findByDTO(src.getIssueType());
		}
		IssueTypeScreenScheme issueTypeScreenScheme = (IssueTypeScreenScheme) newItem.getObjectParameters()[0];
		if (original != null) {
			IssueTypeScreenSchemeEntityDTO existingDTO = (IssueTypeScreenSchemeEntityDTO) findByDTO(original);
			IssueTypeScreenSchemeEntity existing = (IssueTypeScreenSchemeEntity) existingDTO.getJiraObject();
			existing.setFieldScreenScheme((FieldScreenScheme) fieldScreenScheme.getJiraObject());
			if (issueType != null) {
				existing.setIssueTypeId(issueType.getId());
			}
			existing.setIssueTypeScreenScheme(issueTypeScreenScheme);
			MANAGER.updateIssueTypeScreenSchemeEntity(existing);
			return existingDTO;
		} else {
			IssueTypeScreenSchemeEntity createdJira = new IssueTypeScreenSchemeEntityImpl(
					MANAGER, ComponentAccessor.getFieldScreenSchemeManager(), ComponentAccessor.getConstantsManager());
			createdJira.setFieldScreenScheme((FieldScreenScheme) fieldScreenScheme.getJiraObject());
			if (issueType != null) {
				createdJira.setIssueTypeId(issueType.getId());
			}
			createdJira.setIssueTypeScreenScheme(issueTypeScreenScheme);
			MANAGER.createIssueTypeScreenSchemeEntity(createdJira);			
			IssueTypeScreenSchemeEntityDTO created = new IssueTypeScreenSchemeEntityDTO();
			created.setJiraObject(createdJira, issueTypeScreenScheme);
			return created;
		}
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return IssueTypeScreenSchemeEntityDTO.class;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		// Filter ignored
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		IssueTypeScreenScheme itss = (IssueTypeScreenScheme) params[0];
		for (Object o : MANAGER.getIssueTypeScreenSchemeEntities(itss)) {
			IssueTypeScreenSchemeEntity e = (IssueTypeScreenSchemeEntity) o;
			IssueTypeScreenSchemeEntityDTO item = new IssueTypeScreenSchemeEntityDTO();
			item.setJiraObject(e, itss);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
