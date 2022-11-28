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
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;
import com.igsl.configmigration.customfieldsearcher.CustomFieldSearcherUtil;
import com.igsl.configmigration.customfieldtype.CustomFieldTypeUtil;
import com.igsl.configmigration.defaultvalueoperations.DefaultValueOperationsDTO;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.issuetype.IssueTypeUtil;
import com.igsl.configmigration.options.OptionDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class CustomFieldUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(CustomFieldUtil.class);
	private static final CustomFieldManager CUSTOM_FIELD_MANAGER = ComponentAccessor.getCustomFieldManager();
	private static final CustomFieldTypeUtil CUSTOM_FIELD_TYPE_UTIL = new CustomFieldTypeUtil();
	private static final CustomFieldSearcherUtil CUSTOM_FIELD_SEARCHER_UTIL = new CustomFieldSearcherUtil();
	private static final IssueTypeUtil ISSUE_TYPE_UTIL = new IssueTypeUtil();
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
	public Map<String, JiraConfigDTO> readAllItems(Object... params) throws Exception {
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

	/**
	 * params[0]: name
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		String identifier = (String) params[0];
		for (CustomField cf : CUSTOM_FIELD_MANAGER.getCustomFieldObjects()) {
			if (cf.getName().equals(identifier) && !isLocked(cf)) {
				return cf;
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
	
	public Object merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		CustomField original = null;
		if (oldItem != null) {
			if (oldItem.getJiraObject() != null) {
				original = (CustomField) oldItem.getJiraObject();
			} else {
				original = (CustomField) findObject(oldItem.getUniqueKey());
			}
		} else {
			original = (CustomField) findObject(newItem.getUniqueKey());
		}
		CustomFieldDTO src = (CustomFieldDTO) newItem;
		CustomFieldType<?, ?> fieldType = (CustomFieldType<?, ?>) CUSTOM_FIELD_TYPE_UTIL.findObject(
				src.getCustomFieldType().getUniqueKey());
		LOGGER.debug("CustomFieldType: " + fieldType);
		CustomFieldSearcher fieldSearcher = (CustomFieldSearcher) CUSTOM_FIELD_SEARCHER_UTIL.findObject(
				fieldType, src.getCustomFieldSearcher().getUniqueKey());
		LOGGER.debug("CustomFieldSearcher: " + fieldSearcher);
		List<JiraContextNode> context = Arrays.asList(GlobalIssueContext.getInstance());
		LOGGER.debug("JiraContextNode: " + context);
		List<IssueType> issueTypes = new ArrayList<>();
		for (IssueTypeDTO item : src.getAssociatedIssueTypes()) {
			IssueType it = (IssueType) ISSUE_TYPE_UTIL.findObject(item.getUniqueKey());
			if (it != null) {
				issueTypes.add(it);
			}
		}
		if (issueTypes.isEmpty()) {
			issueTypes.add(null);	// Jira wants a null value to indicate global
		}
		LOGGER.debug("IssueType: " + issueTypes);
		if (original != null) {
			// Update
			CUSTOM_FIELD_MANAGER.updateCustomField(
					original.getIdAsLong(), src.getName(), src.getDescription(), fieldSearcher);
			// TODO
			return original;
		} else {
			// Create
			CustomField created = CUSTOM_FIELD_MANAGER.createCustomField(
					src.getName(), 
					src.getDescription(), 
					fieldType,
					fieldSearcher, 
					context, 
					issueTypes);
			// Options
			FieldConfigScheme scheme = created.getConfigurationSchemes().get(0);
			FieldConfig config = scheme.getOneAndOnlyConfig();
			for (OptionDTO opt : src.getOptions().getRootOptions()) {
				createOptionTree(config, opt, null);
			}
			// Default value
			DefaultValueOperationsDTO def = src.getDefaultValueOperations();
			def.getDefaultValueObject();
			
			created.getDefaultValueOperations().setDefaultValue(config, null); // TODO
			return created;
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
		return CustomFieldDTO.class;
	}

	@Override
	public boolean isPublic() {
		return true;
	}

}
