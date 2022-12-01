package com.igsl.configmigration.customfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.user.ApplicationUser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;
import com.igsl.configmigration.customfieldsearcher.CustomFieldSearcherDTO;
import com.igsl.configmigration.customfieldsearcher.CustomFieldSearcherUtil;
import com.igsl.configmigration.customfieldtype.CustomFieldTypeDTO;
import com.igsl.configmigration.customfieldtype.CustomFieldTypeUtil;
import com.igsl.configmigration.defaultvalueoperations.DefaultValueOperationsDTO;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.issuetype.IssueTypeUtil;
import com.igsl.configmigration.options.OptionDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class CustomFieldUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(CustomFieldUtil.class);
	
	private static final CustomFieldManager CUSTOM_FIELD_MANAGER = 
			ComponentAccessor.getCustomFieldManager();
	private static final ManagedConfigurationItemService CONFIG_ITEM_SERVICE = 
			ComponentAccessor.getComponent(ManagedConfigurationItemService.class);
	private static final OptionsManager OPTIONS_MANAGER = ComponentAccessor.getOptionsManager();

	@Override
	public String getName() {
		return "Custom Field";
	}
	
	private static boolean isLocked(CustomField cf) {
		if (cf != null) {
			ManagedConfigurationItem item = CONFIG_ITEM_SERVICE.getManagedCustomField(cf);
			ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
			return !CONFIG_ITEM_SERVICE.doesUserHavePermission(currentUser, item);
		}
		return false;
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (CustomField cf : CUSTOM_FIELD_MANAGER.getCustomFieldObjects()) {
			if (!isLocked(cf)) {
				CustomFieldDTO item = new CustomFieldDTO();
				item.setJiraObject(cf);
				result.put(item.getUniqueKey(), item);
			}
		}
		return result;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		for (CustomField cf : CUSTOM_FIELD_MANAGER.getCustomFieldObjects()) {
			if (cf.getId().equals(id) && !isLocked(cf)) {
				CustomFieldDTO item = new CustomFieldDTO();
				item.setJiraObject(cf);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (CustomField cf : CUSTOM_FIELD_MANAGER.getCustomFieldObjects()) {
			if (cf.getName().equals(uniqueKey) && !isLocked(cf)) {
				CustomFieldDTO item = new CustomFieldDTO();
				item.setJiraObject(cf);
				return item;
			}
		}
		return null;
	}

	private void createOptionTree(FieldConfig config, OptionDTO opt, Long parentId) {
		Option created = null;
		if (parentId != null) {
			created = OPTIONS_MANAGER.createOption(config, parentId, opt.getSequence(), opt.getValue());
		} else {
			created = OPTIONS_MANAGER.createOption(config, null, opt.getSequence(), opt.getValue());
		}
		if (created != null && opt.getChildOptions() != null) {
			for (OptionDTO child : opt.getChildOptions()) {
				createOptionTree(config, child, created.getOptionId());
			}
		}
	}
	
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		final CustomFieldTypeUtil CUSTOM_FIELD_TYPE_UTIL = 
				(CustomFieldTypeUtil) JiraConfigTypeRegistry.getConfigUtil(CustomFieldTypeUtil.class);
		final CustomFieldSearcherUtil CUSTOM_FIELD_SEARCHER_UTIL = 
				(CustomFieldSearcherUtil) JiraConfigTypeRegistry.getConfigUtil(CustomFieldSearcherUtil.class);
		final IssueTypeUtil ISSUE_TYPE_UTIL = 
				(IssueTypeUtil) JiraConfigTypeRegistry.getConfigUtil(IssueTypeUtil.class);
		CustomFieldDTO original = null;
		if (oldItem != null) {
			original = (CustomFieldDTO) oldItem;
		} else {
			original = (CustomFieldDTO) findByDTO(newItem);
		}
		CustomFieldDTO src = (CustomFieldDTO) newItem;
		CustomFieldTypeDTO fieldType = (CustomFieldTypeDTO) CUSTOM_FIELD_TYPE_UTIL.findByDTO(
				src.getCustomFieldType());
		LOGGER.debug("CustomFieldType: " + fieldType);
		CustomFieldSearcherDTO fieldSearcher = (CustomFieldSearcherDTO) CUSTOM_FIELD_SEARCHER_UTIL.findByDTO(
				src.getCustomFieldSearcher());
		LOGGER.debug("CustomFieldSearcher: " + fieldSearcher);
		List<JiraContextNode> context = Arrays.asList(GlobalIssueContext.getInstance());
		LOGGER.debug("JiraContextNode: " + context);
		List<IssueType> issueTypes = new ArrayList<>();
		for (IssueTypeDTO item : src.getAssociatedIssueTypes()) {
			IssueTypeDTO it = (IssueTypeDTO) ISSUE_TYPE_UTIL.findByDTO(item);
			if (it != null) {
				issueTypes.add((IssueType) it.getJiraObject());
			}
		}
		if (issueTypes.isEmpty()) {
			issueTypes.add(null);	// Jira wants a null value to indicate global
		}
		LOGGER.debug("IssueType: " + issueTypes);
		if (original != null) {
			// Update
			CUSTOM_FIELD_MANAGER.updateCustomField(
					Long.parseLong(original.getId()), 
					src.getName(), 
					src.getDescription(), 
					(CustomFieldSearcher) fieldSearcher.getJiraObject());
			return findByDTO(original);
		} else {
			// Create
			CustomField createdJira = CUSTOM_FIELD_MANAGER.createCustomField(
					src.getName(), 
					src.getDescription(), 
					(CustomFieldType<?, ?>) fieldType.getJiraObject(),
					(CustomFieldSearcher) fieldSearcher.getJiraObject(), 
					context, 
					issueTypes);
			FieldConfigScheme scheme = createdJira.getConfigurationSchemes().get(0);
			FieldConfig config = scheme.getOneAndOnlyConfig();
			// Options
			if (src.getOptions() != null && src.getOptions().getRootOptions() != null) {
				for (OptionDTO opt : src.getOptions().getRootOptions()) {
					createOptionTree(config, opt, null);
				}
			}
			// Default value
			DefaultValueOperationsDTO def = src.getDefaultValueOperations();
			if (def != null) {
				Object defaultValue = def.getRawValue();
				createdJira.getDefaultValueOperations().setDefaultValue(config, defaultValue);
			}
			CustomFieldDTO created = new CustomFieldDTO();
			created.setJiraObject(createdJira);
			return created;
		}
	}
	
		@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return CustomFieldDTO.class;
	}

	@Override
	public boolean isPublic() {
		return true;
	}

}
