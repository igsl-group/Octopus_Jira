package com.igsl.configmigration.issuetypescheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;
import com.igsl.configmigration.fieldconfigscheme.FieldConfigSchemeDTO;
import com.igsl.configmigration.optionset.OptionSetDTO;
import com.igsl.configmigration.optionset.OptionSetUtil;

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
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (FieldConfigScheme scheme : MANAGER.getAllSchemes()) {
			IssueTypeSchemeDTO item = new IssueTypeSchemeDTO();
			item.setJiraObject(scheme);
			result.put(item.getUniqueKey(), item);
		}
		return result;
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
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		final OptionSetUtil OPTION_SET_UTIL = 
				(OptionSetUtil) JiraConfigTypeRegistry.getConfigUtil(OptionSetUtil.class);
		IssueTypeSchemeDTO original = null;
		if (oldItem != null) {
			original = (IssueTypeSchemeDTO) oldItem;
		} else {
			original = (IssueTypeSchemeDTO) findByDTO(newItem);
		}
		IssueTypeSchemeDTO src = (IssueTypeSchemeDTO) newItem;
		if (original != null) {
			OptionSetDTO optionSetDTO = 
					(OptionSetDTO) OPTION_SET_UTIL.merge(original.getFieldConfig(), src.getFieldConfig());
			OptionSet optionSet = (OptionSet) optionSetDTO.getJiraObject();
			FieldConfigScheme.Builder b = new FieldConfigScheme.Builder((FieldConfigScheme) src.getJiraObject());
			MANAGER.update(b.toFieldConfigScheme(), optionSet.getOptionIds());
			return null;
		} else {
			OptionSetDTO optionSetDTO = 
					(OptionSetDTO) OPTION_SET_UTIL.merge(original.getFieldConfig(), src.getFieldConfig());
			OptionSet optionSet = (OptionSet) optionSetDTO.getJiraObject();
			List<String> optionIds = new ArrayList<>();
			optionIds.addAll(optionSet.getOptionIds());
			FieldConfigScheme createdJira = MANAGER.create(src.getName(), src.getDescription(), optionIds);
			FieldConfigSchemeDTO created = new FieldConfigSchemeDTO();
			created.setJiraObject(createdJira);
			return created;
		}
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return IssueTypeSchemeDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

}
