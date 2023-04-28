package com.igsl.configmigration.customfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

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
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.user.ApplicationUser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.KeyGuide;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.customfieldsearcher.CustomFieldSearcherDTO;
import com.igsl.configmigration.customfieldsearcher.CustomFieldSearcherUtil;
import com.igsl.configmigration.customfieldtype.CustomFieldTypeDTO;
import com.igsl.configmigration.customfieldtype.CustomFieldTypeUtil;
import com.igsl.configmigration.defaultvalueoperations.DefaultValueOperationsDTO;
import com.igsl.configmigration.field.FieldDTO;
import com.igsl.configmigration.field.FieldUtil;
import com.igsl.configmigration.fieldconfig.FieldConfigDTO;
import com.igsl.configmigration.general.GeneralDTO;
import com.igsl.configmigration.insight.ObjectBeanDTO;
import com.igsl.configmigration.insight.ObjectBeanUtil;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.issuetype.IssueTypeUtil;
import com.igsl.configmigration.options.OptionDTO;
import com.igsl.configmigration.options.OptionUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class CustomFieldUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(CustomFieldUtil.class);
	
	private static final CustomFieldManager CUSTOM_FIELD_MANAGER = 
			ComponentAccessor.getCustomFieldManager();
	private static final OptionsManager OPTIONS_MANAGER = 
			ComponentAccessor.getOptionsManager();
	private static final ManagedConfigurationItemService CONFIG_ITEM_SERVICE = 
			ComponentAccessor.getComponent(ManagedConfigurationItemService.class);
	
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
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		for (CustomField cf : CUSTOM_FIELD_MANAGER.getCustomFieldObjects()) {
			if (cf.getId().equals(id) && !isLocked(cf)) {
				CustomFieldDTO item = new CustomFieldDTO();
				item.setJiraObject(cf, params);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (CustomField cf : CUSTOM_FIELD_MANAGER.getCustomFieldObjects()) {
			CustomFieldTypeDTO type = new CustomFieldTypeDTO();
			type.setJiraObject(cf.getCustomFieldType());
			String uKey = makeUniqueKey(cf.getId(), cf.getName(), cf.getDescription(), type);
			if (uKey.equals(uniqueKey) && !isLocked(cf)) {
				CustomFieldDTO item = new CustomFieldDTO();
				item.setJiraObject(cf, params);
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
		final CustomFieldTypeUtil CUSTOM_FIELD_TYPE_UTIL = 
				(CustomFieldTypeUtil) JiraConfigTypeRegistry.getConfigUtil(CustomFieldTypeUtil.class);
		final CustomFieldSearcherUtil CUSTOM_FIELD_SEARCHER_UTIL = 
				(CustomFieldSearcherUtil) JiraConfigTypeRegistry.getConfigUtil(CustomFieldSearcherUtil.class);
		final IssueTypeUtil ISSUE_TYPE_UTIL = 
				(IssueTypeUtil) JiraConfigTypeRegistry.getConfigUtil(IssueTypeUtil.class);
		final OptionUtil OPTION_UTIL = 
				(OptionUtil) JiraConfigTypeRegistry.getConfigUtil(OptionUtil.class);
		CustomFieldDTO original = null;
		if (oldItem != null) {
			original = (CustomFieldDTO) oldItem;
		} else {
			// Use mappedObject if provided, otherwise create as new object
			if (newItem.getMappedObject() != null) {
				original = (CustomFieldDTO) findByUniqueKey(newItem.getMappedObject().getUniqueKey());
			} else {
				original = null;
			}
		}
		CustomFieldDTO src = (CustomFieldDTO) newItem;
		CustomFieldTypeDTO fieldType = (CustomFieldTypeDTO) CUSTOM_FIELD_TYPE_UTIL.findByDTO(
				src.getCustomFieldType());
		if (fieldType == null) {
			throw new Exception("Custom Field type " + src.getCustomFieldType().getConfigName() + " cannot be found");
		}
		LOGGER.debug("CustomFieldType: " + fieldType);
		CustomFieldSearcherDTO fieldSearcher = null;
		if (src.getCustomFieldSearcher() != null) {
			src.getCustomFieldSearcher().setJiraObject(null, (CustomFieldType<?, ?>) fieldType.getJiraObject());		
			fieldSearcher = (CustomFieldSearcherDTO) CUSTOM_FIELD_SEARCHER_UTIL.findByDTO(
					src.getCustomFieldSearcher());
			LOGGER.debug("CustomFieldSearcher: " + fieldSearcher);
			if (fieldSearcher == null) {
				result.addWarning(
						"Custom field searcher \"" + 
						src.getCustomFieldSearcher().getConfigName() + 
						"\" cannot be found, custom field will not be indexed.");
			}
		}
		List<JiraContextNode> context = Arrays.asList(GlobalIssueContext.getInstance());
		LOGGER.debug("JiraContextNode: " + context);
		List<IssueType> issueTypes = new ArrayList<>();
		for (IssueTypeDTO item : src.getAssociatedIssueTypes()) {
			if (!ISSUE_TYPE_UTIL.isDefaultObject(item)) {
				IssueTypeDTO it = (IssueTypeDTO) ISSUE_TYPE_UTIL.findByDTO(item);
				if (it != null) {
					issueTypes.add((IssueType) it.getJiraObject());
				} else {
					result.addWarning("Issue Type \"" + item.getConfigName() + "\" cannot be found, it will be skipped.");
				}
			}
		}
		if (issueTypes.isEmpty()) {
			issueTypes.add(null);	// Jira wants a null value to indicate global
		}
		LOGGER.debug("IssueType: " + issueTypes);
		CustomField createdJira = null;
		if (original != null) {
			createdJira = (CustomField) original.getJiraObject();
			// Update
			CUSTOM_FIELD_MANAGER.updateCustomField(
					original.getIdAsLong(),
					src.getName(), 
					src.getDescription(), 
					(fieldSearcher == null)? null : (CustomFieldSearcher) fieldSearcher.getJiraObject());
		} else {
			// Create
			createdJira = CUSTOM_FIELD_MANAGER.createCustomField(
					src.getName(), 
					src.getDescription(), 
					(CustomFieldType<?, ?>) fieldType.getJiraObject(),
					(fieldSearcher == null)? null : (CustomFieldSearcher) fieldSearcher.getJiraObject(), 
					context, 
					issueTypes);
		}
		if (createdJira != null) {
			FieldConfigScheme scheme = createdJira.getConfigurationSchemes().get(0);
			FieldConfig config = scheme.getOneAndOnlyConfig();
			FieldConfigDTO configDTO = new FieldConfigDTO();
			configDTO.setJiraObject(config);
			// Delete existing options
			Options options = createdJira.getOptions(null, config, null);
			if (options != null) {
				for (Option opt : options.getRootOptions()) {
					OPTIONS_MANAGER.deleteOptionAndChildren(opt);
				}
			} 
			// Recreate options
			if (src.getOptions() != null && src.getOptions().getRootOptions() != null) {
				for (OptionDTO opt : src.getOptions().getRootOptions()) {
					// Update option's FieldConfig
					opt.setJiraObject(null, configDTO, null);
					OptionDTO createdOption = (OptionDTO) 
							OPTION_UTIL.merge(exportStore, null, importStore, opt).getNewDTO();
					LOGGER.debug("merged option raw: " + createdOption.getJiraObject());
					opt.setJiraObject(createdOption.getJiraObject(), configDTO, null);
				}
			} 
			// Clear existing default value
			createdJira.getDefaultValueOperations().setDefaultValue(config, null);
			// Set default value
			DefaultValueOperationsDTO def = src.getDefaultValueOperations();
			if (def != null) {
				LOGGER.debug("Source default: " + OM.writeValueAsString(def));
				Object defaultValue = getRawValue(src.getOptions().getAllOptions(), def);
				LOGGER.debug("defaultValue: " + defaultValue);
				if (defaultValue != null) {
					createdJira.getDefaultValueOperations().setDefaultValue(config, defaultValue);
				}
			}
			CustomFieldDTO created = new CustomFieldDTO();
			created.setJiraObject(createdJira);
			result.setNewDTO(created);
			// Update mappedObject in DTOStore
			JiraConfigDTO storeSrc = importStore.getTypeStore(this.getImplementation()).get(src.getUniqueKey());
			storeSrc.setMappedObject(created);
		}
		return result;
	}
	
	private static Object getRawValueHelper(List<OptionDTO> options, Object o) throws Exception {
		// We have OptionDTO
		if (o instanceof OptionDTO) {
			// Map to existing options
			OptionDTO search = (OptionDTO) o;
			LOGGER.debug("Find: " + OM.writeValueAsString(search));
			LOGGER.debug("sequence: " + search.getSequence());
			LOGGER.debug("value: " + search.getValue());
			LOGGER.debug("childOptions: " + OM.writeValueAsString(search.getChildOptions()));
			for (OptionDTO item : options) {
				// Compare the two OptionDTO
				LOGGER.debug("Vs: " + OM.writeValueAsString(item));
				LOGGER.debug("sequence: " + item.getSequence());
				LOGGER.debug("value: " + item.getValue());
				LOGGER.debug("childOptions: " + OM.writeValueAsString(item.getChildOptions()));
				List<String> differences = JiraConfigDTO.getDifferences("", search, item);
				LOGGER.debug("Differences: " + OM.writeValueAsString(differences));
				if (differences.size() == 0) {
					// Match found
					LOGGER.debug("Match found");
					return item.getJiraObject();
				}
			}
			// No match
			return null;
		} else if (o instanceof ObjectBeanDTO) {
			// Find existing object bean
			ObjectBeanDTO data = (ObjectBeanDTO) o;
			LOGGER.debug("Insight from data: " + data.getUniqueKey() + " name: " + data.getLabel());
			ObjectBeanUtil insightUtil = 
					(ObjectBeanUtil) JiraConfigTypeRegistry.getConfigUtil(ObjectBeanUtil.class);
			ObjectBeanDTO dto = (ObjectBeanDTO) insightUtil.findByDTO(data);
			if (dto != null) {
				LOGGER.debug("Insight found: " + data.getUniqueKey() + " name: " + dto.getLabel());
				return dto.getJiraObject();
			} 
			// No match
			return null;
		} else if (o instanceof GeneralDTO) {
			return ((GeneralDTO) o).getValue(); 
		} else {
			// We have basic types like Timestamp, String, etc.
			GeneralDTO dto = new GeneralDTO();
			dto.setJiraObject(o);
			return dto;
		}
		// TODO What else?
	}
	
	public static Object getRawValue(List<OptionDTO> options, DefaultValueOperationsDTO dto) throws Exception {
		switch (dto.getValueType()) {
		case ARRAY:
			List<Object> array = new ArrayList<>();
			for (Object o : dto.getDefaultListValue()) {
				array.add(getRawValueHelper(options, o));
			}
			return array.toArray(new Object[0]);
		case LIST:
			List<Object> list = new ArrayList<>();
			for (Object o : dto.getDefaultListValue()) {
				list.add(getRawValueHelper(options, o));
			}
			return list;
		case MAP:
			Map<Object, Object> map = new HashMap<>();
			for (Map.Entry<Object, JiraConfigDTO> entry : dto.getDefaultMapValue().entrySet()) {
				Object key = entry.getKey();
				if (DefaultValueOperationsDTO.NULL_KEY_REPLACEMENT.equals(key)) {
					// Restore null key replacement
					key = null;
				}
				Object value = entry.getValue();
				Object item = getRawValueHelper(options, value);
				map.put(key, item);
			}
			return map;
		case OBJECT:
			return getRawValueHelper(options, dto.getDefaultValue());
		}
		return null;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return CustomFieldDTO.class;
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
		for (CustomField cf : CUSTOM_FIELD_MANAGER.getCustomFieldObjects()) {
			if (!isLocked(cf)) {
				CustomFieldDTO dto = new CustomFieldDTO();
				dto.setJiraObject(cf, params);
				if (!matchFilter(dto, filter)) {
					continue;
				}
				result.put(dto.getUniqueKey(), dto);
			}
		}
		return result;
	}
	
	private static final String DELIMITER = ".";
	
	// Specific API for CustomFieldDTO
	public String makeUniqueKey(String id, String name, String description, CustomFieldTypeDTO customFieldType) {
		StringBuilder result = new StringBuilder();
		result
			.append(id).append(DELIMITER)
			.append(name).append(DELIMITER)
			.append(customFieldType.getName());
		return result.toString();
	}
	
	@Override
	public boolean isManualMatch() {
		return true;
	}
	
	public static final String MATCH_ID = "id";
	public static final String MATCH_UNIQUEKEY = "uniqueKey";
	public static final String MATCH_NAME = "name";
	public static final String MATCH_CUSTOMFIELDTYPE = "customFieldType";
	
	@Override
	public List<JiraConfigDTO> findMatches(DTOStore store, Map<String, String> params) throws Exception {
		List<JiraConfigDTO> result = new ArrayList<>();
		// Parse unique key into parts
		if (params != null) {
			String id = params.get(MATCH_ID);
			String uniqueKey = params.get(MATCH_UNIQUEKEY);
			String name = params.get(MATCH_NAME);
			String customFieldType = params.get(MATCH_CUSTOMFIELDTYPE);
			LOGGER.debug("id: " + id);
			LOGGER.debug("uniqueKey: " + uniqueKey);
			LOGGER.debug("name: " + name);
			LOGGER.debug("customFieldType: " + customFieldType);
			// Find items with matching name and custom field type
			Map<String, JiraConfigDTO> dtoMap = store.getTypeStore(this);
			for (JiraConfigDTO dto : dtoMap.values()) {
				CustomFieldDTO cf = (CustomFieldDTO) dto;
				if (id != null && !cf.getId().equals(id)) {
					continue;
				}
				if (uniqueKey != null && !cf.getUniqueKey().equals(uniqueKey)) {
					continue;
				}
				if (name != null && !cf.getName().equals(name)) {
					continue;
				}
				if (customFieldType != null && !cf.getCustomFieldType().getName().equals(customFieldType)) {
					continue;
				}
				result.add(cf);
			}
		}
		return result;
	}

	@Override
	public List<KeyGuide> getCompareGuide(DTOStore exportStore, DTOStore importStore) throws Exception {
		List<KeyGuide> result = new ArrayList<>();
		List<JiraConfigDTO> exportList = new ArrayList<>();
		exportList.addAll(exportStore.getTypeStore(this).values());
		List<JiraConfigDTO> importList = new ArrayList<>();
		importList.addAll(importStore.getTypeStore(this).values());
		// Go through exportList, removing importList items if a single match is found
		for (JiraConfigDTO exportObj : exportList) {
			KeyGuide kg = new KeyGuide();
			kg.exportUniqueKey = exportObj.getUniqueKey();
			Map<String, String> params = new HashMap<>();
			params.put(MATCH_UNIQUEKEY, exportObj.getUniqueKey());
			List<JiraConfigDTO> list = findMatches(importStore, params);
			if (list.size() == 1) {
				kg.importUniqueKey = list.get(0).getUniqueKey();
				importList.remove(list.get(0));
			}
			result.add(kg);
		}
		// Then add remaining importList items to list
		for (JiraConfigDTO importObj : importList) {
			KeyGuide kg = new KeyGuide();
			kg.importUniqueKey = importObj.getUniqueKey();
			result.add(kg);
		}
		return result;
	}

	/**
	 * Resolve field ID.
	 * systemField or customField must be non-null.
	 * 
	 * CustomField is used if non-null.
	 * It will be looked up in importStore. 
	 * If there is mappedObject, it will be used. 
	 * If not, it will be looked up in exportStore for a single match.
	 * 
	 * @param exportStore Export store
	 * @param importStore Import store
	 * @param systemField FieldDTO
	 * @param customField CustomFieldDTO
	 * @return String field ID. Null if not found.
	 * @throws Exception
	 */
	public String resolveFieldId(
			DTOStore exportStore, DTOStore importStore, 
			FieldDTO systemField, CustomFieldDTO customField) throws Exception {
		String fieldId = null;
		FieldUtil fieldUtil = (FieldUtil) JiraConfigTypeRegistry.getConfigUtil(FieldUtil.class);
		if (customField != null) {
			LOGGER.debug("Custom field");
			// Look up custom field in importStore to find if there is a mappedObject
			Map<String, String> params = new HashMap<>();
			params.put(CustomFieldUtil.MATCH_ID, customField.getId());
			List<JiraConfigDTO> list = findMatches(importStore, params);
			if (list != null && list.size() == 1) {
				LOGGER.debug("Custom field found by ID");
				CustomFieldDTO dto = (CustomFieldDTO) list.get(0);
				if (dto.getMappedObject() != null) {
					LOGGER.debug("Using mapped object");
					// Use mapped object
					dto = (CustomFieldDTO) findByUniqueKey(dto.getMappedObject().getUniqueKey());
					if (dto != null) {
						LOGGER.debug("Mapped object found");
						fieldId = dto.getId();
					}
				} else {
					// Lookup for single match
					LOGGER.debug("Using single match");
					params.clear();
					params.put(CustomFieldUtil.MATCH_NAME, customField.getName());
					list = findMatches(exportStore, params);
					if (list != null && list.size() == 1) {
						LOGGER.debug("Single match found");
						fieldId = ((CustomFieldDTO) list.get(0)).getId();
					}
				}
			}
		} else if (systemField != null) {
			LOGGER.debug("System field");
			// Look up system field
			FieldDTO field = (FieldDTO) fieldUtil.findByDTO(systemField);
			if (field != null) {
				fieldId = field.getId();
			}
		}
		LOGGER.debug("Final Field ID: " + fieldId);
		return fieldId;
	}
	
}
