package com.igsl.configmigration.issuetypescheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.atlassian.jira.project.Project;
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
import com.igsl.configmigration.optionset.OptionSetDTO;
import com.igsl.configmigration.optionset.OptionSetUtil;
import com.igsl.configmigration.project.ProjectDTO;
import com.igsl.configmigration.project.ProjectUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class IssueTypeSchemeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(IssueTypeSchemeUtil.class);
	private static final IssueTypeSchemeManager MANAGER = 
			ComponentAccessor.getComponent(IssueTypeSchemeManager.class);
	
	@Override
	public String getName() {
		return "Issue Type Scheme";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		for (FieldConfigScheme scheme : MANAGER.getAllSchemes()) {
			if (scheme.getId().equals(idAsLong)) {
				IssueTypeSchemeDTO item = new IssueTypeSchemeDTO();
				item.setJiraObject(scheme);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (FieldConfigScheme scheme : MANAGER.getAllSchemes()) {
			if (scheme.getName().equals(uniqueKey)) {
				IssueTypeSchemeDTO item = new IssueTypeSchemeDTO();
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
		final OptionSetUtil OPTION_SET_UTIL = 
				(OptionSetUtil) JiraConfigTypeRegistry.getConfigUtil(OptionSetUtil.class);
		final FieldConfigUtil FIELD_CONFIG_UTIL = 
				(FieldConfigUtil) JiraConfigTypeRegistry.getConfigUtil(FieldConfigUtil.class);
		final IssueTypeUtil ISSUE_TYPE_UTIL = 
				(IssueTypeUtil) JiraConfigTypeRegistry.getConfigUtil(IssueTypeUtil.class);
		IssueTypeSchemeDTO original = null;
		if (oldItem != null) {
			original = (IssueTypeSchemeDTO) oldItem;
		} else {
			original = (IssueTypeSchemeDTO) findByDTO(newItem);
		}
		ProjectUtil projectUtil = (ProjectUtil) JiraConfigTypeRegistry.getConfigUtil(ProjectUtil.class); 
		IssueTypeSchemeDTO src = (IssueTypeSchemeDTO) newItem;
		List<Project> projects = new ArrayList<>();
		for (ProjectDTO p : src.getAssociatedProjects()) {
			ProjectDTO dto = (ProjectDTO) projectUtil.findByUniqueKey(p.getUniqueKey());
			if (dto != null) {
				projects.add((Project) dto.getJiraObject());
			}
		}
		if (original != null) {
			FIELD_CONFIG_UTIL.merge(exportStore, original.getFieldConfig(), importStore, src.getFieldConfig());
			List<String> optionIds = new ArrayList<>();
			for (IssueTypeDTO issueType : src.getAssociatedIssueTypes()) {
				if (!ISSUE_TYPE_UTIL.isDefaultObject(issueType)) {
					IssueTypeDTO dto = (IssueTypeDTO) ISSUE_TYPE_UTIL.findByDTO(issueType);
					if (dto != null) {
						optionIds.add(dto.getId());
					} else {
						result.addWarning(
								"Issue type \"" + issueType.getConfigName() + 
								"\" cannot be found, association to it will be skipped");
					}
				}
			}
			FieldConfigScheme.Builder b = new FieldConfigScheme.Builder((FieldConfigScheme) src.getJiraObject());
			FieldConfigScheme updatedJira = MANAGER.update(b.toFieldConfigScheme(), optionIds);
			MANAGER.addProjectAssociations(updatedJira, projects);
		} else {
			FieldConfigDTO fc = src.getFieldConfig();
			FIELD_CONFIG_UTIL.merge(exportStore, null, importStore, src.getFieldConfig());
			List<String> optionIds = new ArrayList<>();
			for (IssueTypeDTO issueType : src.getAssociatedIssueTypes()) {
				IssueTypeDTO dto = (IssueTypeDTO) ISSUE_TYPE_UTIL.findByDTO(issueType);
				if (dto != null) {
					optionIds.add(dto.getId());
				}
			}
			FieldConfigScheme createdJira = MANAGER.create(src.getName(), src.getDescription(), optionIds);
			MANAGER.getIssueTypesForScheme(createdJira);
			MANAGER.addProjectAssociations(createdJira, projects);
			FieldConfigSchemeDTO created = new FieldConfigSchemeDTO();
			created.setJiraObject(createdJira);
			result.setNewDTO(created);
		}
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return IssueTypeSchemeDTO.class;
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
		for (FieldConfigScheme scheme : MANAGER.getAllSchemes()) {
			IssueTypeSchemeDTO item = new IssueTypeSchemeDTO();
			item.setJiraObject(scheme);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
