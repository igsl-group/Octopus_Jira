package com.igsl.configmigration.issuetypescreencheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.SessionData.ImportData;
import com.igsl.configmigration.fieldconfig.FieldConfigDTO;
import com.igsl.configmigration.fieldconfig.FieldConfigUtil;
import com.igsl.configmigration.fieldconfigscheme.FieldConfigSchemeDTO;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.issuetype.IssueTypeUtil;
import com.igsl.configmigration.issuetypescheme.IssueTypeSchemeDTO;
import com.igsl.configmigration.issuetypescheme.IssueTypeSchemeUtil;
import com.igsl.configmigration.optionset.OptionSetDTO;
import com.igsl.configmigration.optionset.OptionSetUtil;
import com.igsl.configmigration.project.ProjectDTO;
import com.igsl.configmigration.project.ProjectUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeScreenSchemeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(IssueTypeScreenSchemeUtil.class);
	private static final IssueTypeScreenSchemeManager MANAGER = 
			ComponentAccessor.getComponent(IssueTypeScreenSchemeManager.class);
	private static final ProjectManager PROJECT_MANAGER = ComponentAccessor.getProjectManager();
	
	@Override
	public String getName() {
		return "Issue Type Screen Scheme";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		for (IssueTypeScreenScheme scheme : MANAGER.getIssueTypeScreenSchemes()) {
			if (scheme.getId().equals(idAsLong)) {
				IssueTypeScreenSchemeDTO item = new IssueTypeScreenSchemeDTO();
				item.setJiraObject(scheme);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (IssueTypeScreenScheme scheme : MANAGER.getIssueTypeScreenSchemes()) {
			if (scheme.getName().equals(uniqueKey)) {
				IssueTypeScreenSchemeDTO item = new IssueTypeScreenSchemeDTO();
				item.setJiraObject(scheme);
				return item;
			}
		}
		return null;
	}

	@Override
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		IssueTypeScreenSchemeDTO original = null;
		if (oldItem != null) {
			original = (IssueTypeScreenSchemeDTO) oldItem;
		} else {
			original = (IssueTypeScreenSchemeDTO) findByDTO(newItem);
		}
		ProjectUtil projectUtil = (ProjectUtil) JiraConfigTypeRegistry.getConfigUtil(ProjectUtil.class); 
		IssueTypeScreenSchemeUtil issueTypeScreenSchemeUtil = (IssueTypeScreenSchemeUtil) JiraConfigTypeRegistry.getConfigUtil(IssueTypeScreenSchemeUtil.class); 
		IssueTypeScreenSchemeEntityUtil issueTypeScreenSchemeEntityUtil = 
				(IssueTypeScreenSchemeEntityUtil) JiraConfigTypeRegistry.getConfigUtil(IssueTypeScreenSchemeEntityUtil.class); 
		OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();
		IssueTypeScreenSchemeDTO src = (IssueTypeScreenSchemeDTO) newItem;
		List<ProjectDTO> projects = new ArrayList<>();
		for (ProjectDTO p : src.getProjects()) {
			ProjectDTO dto = (ProjectDTO) projectUtil.findByUniqueKey(p.getUniqueKey());
			if (dto != null) {
				projects.add(dto);
			}
		}
		IssueTypeScreenScheme createdJira = null;
		if (original != null) {
			IssueTypeScreenSchemeDTO existingDTO = (IssueTypeScreenSchemeDTO) issueTypeScreenSchemeUtil.findByDTO(src);
			IssueTypeScreenScheme existing = (IssueTypeScreenScheme) existingDTO.getJiraObject();
			existing.setDescription(src.getDescription());
			existing.setName(src.getName());
			MANAGER.updateIssueTypeScreenScheme(existing);
			createdJira = existing;			
		} else {
			createdJira = new IssueTypeScreenSchemeImpl(MANAGER);
			createdJira.setDescription(src.getDescription());
			createdJira.setName(src.getName());
			MANAGER.createIssueTypeScreenScheme(createdJira);
		}
		IssueTypeScreenSchemeDTO created = null;
		if (createdJira != null) {
			created = new IssueTypeScreenSchemeDTO();
			created.setJiraObject(createdJira);
			result.setNewDTO(created);
			// Re-associate with projects
			for (GenericValue gv : createdJira.getProjects()) {
				String projectId = String.valueOf(
						gv.getAllFields().get(IssueTypeScreenSchemeDTO.GENERIC_VALUE_PROJECT_ID));
				Project p = PROJECT_MANAGER.getProjectObj(Long.parseLong(projectId));
				MANAGER.removeSchemeAssociation(p, createdJira);
			}
			for (ProjectDTO pDto : src.getProjects()) {
				ProjectDTO p = (ProjectDTO) projectUtil.findByDTO(pDto);
				if (p != null) {
					MANAGER.addSchemeAssociation((Project) p.getJiraObject(), createdJira);
				} else {
					result.addWarning(
							"Project \"" + pDto.getConfigName() + 
							"\" cannot be found, association to it will be skipped.");
				}
			}
			// Update association to items
			MANAGER.removeIssueTypeSchemeEntities(createdJira);
			for (IssueTypeScreenSchemeEntityDTO entity : src.getEntities()) {
				entity.setJiraObject(null, created);
				IssueTypeScreenSchemeEntityDTO existingEntityDTO = 
						(IssueTypeScreenSchemeEntityDTO) issueTypeScreenSchemeEntityUtil.findByDTO(entity);
				issueTypeScreenSchemeEntityUtil.merge(exportStore, existingEntityDTO, importStore, entity);
			}
		}
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return IssueTypeScreenSchemeDTO.class;
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
		for (IssueTypeScreenScheme scheme : MANAGER.getIssueTypeScreenSchemes()) {
			IssueTypeScreenSchemeDTO item = new IssueTypeScreenSchemeDTO();
			item.setJiraObject(scheme);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
