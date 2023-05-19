package com.igsl.configmigration.fieldlayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayoutImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItemImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.customfield.CustomFieldDTO;
import com.igsl.configmigration.customfield.CustomFieldUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldLayoutUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(FieldLayoutUtil.class);
	private static FieldLayoutManager MANAGER = ComponentAccessor.getFieldLayoutManager();
	private static FieldManager FIELD_MANAGER = ComponentAccessor.getFieldManager();
	
	@Override
	public String getName() {
		return "Field Configuration";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		FieldLayout f = MANAGER.getFieldLayout(idAsLong);
		if (f != null) {
			FieldLayoutDTO dto = new FieldLayoutDTO();
			dto.setJiraObject(f, params);
			return dto;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (FieldLayout f : MANAGER.getEditableFieldLayouts()) {
			if (f.getName().equals(uniqueKey)) {
				FieldLayoutDTO dto = new FieldLayoutDTO();
				dto.setJiraObject(f, params);
				return dto;
			}
		}
		return null;
	}
	
	@Override
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		FieldLayoutDTO original = null;
		if (oldItem != null) {
			original = (FieldLayoutDTO) oldItem;
		} else {
			original = (FieldLayoutDTO) findByUniqueKey(newItem.getUniqueKey());
		}
		FieldLayoutDTO src = (FieldLayoutDTO) newItem;
		CustomFieldUtil util = (CustomFieldUtil) JiraConfigTypeRegistry.getConfigUtil(CustomFieldUtil.class);
		List<FieldLayoutItem> items = new ArrayList<>();
		for (FieldLayoutItemDTO item : src.getFieldLayoutItems()) {
			// Map custom field
			String fieldId = util.resolveFieldId(
					exportStore, importStore, item.getSystemField(), item.getCustomField());
			OrderableField<?> of = FIELD_MANAGER.getOrderableField(fieldId);
			LOGGER.debug("Layout adding item: " + fieldId + 
					" Hidden: " + item.isHidden() + 
					" Required: " + item.isRequired() + 
					" Renderer: " + item.getRendererType() + 
					" Desc: " + item.getFieldDescription()
					);
			FieldLayoutItemImpl it = new FieldLayoutItemImpl.Builder()
				.setOrderableField(of)
				.setFieldDescription(item.getFieldDescription())
				.setHidden(item.isHidden())
				.setRequired(item.isRequired())
				.setRendererType(item.getRendererType())
				.build();
			items.add(it);
		}
		EditableFieldLayout createdJira = null;
		if (original != null) {
			// Update
			// Fetch the original as EditableLayoutItem to access its .getGenericValue()
			createdJira = MANAGER.getEditableFieldLayout(original.getId());	
			// Create a clone with same ID but new items
			createdJira = new EditableFieldLayoutImpl(createdJira.getGenericValue(), items);	
			createdJira.setDescription(src.getDescription());
			createdJira.setName(src.getName());
			createdJira = MANAGER.storeAndReturnEditableFieldLayout(createdJira);
		} else {
			// Create
			createdJira = new EditableFieldLayoutImpl(null, items);
			createdJira.setDescription(src.getDescription());
			createdJira.setName(src.getName());			
			createdJira = MANAGER.storeAndReturnEditableFieldLayout(createdJira);
		}
		FieldLayoutDTO created = new FieldLayoutDTO();
		created.setJiraObject(createdJira);
		result.setNewDTO(created);
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return FieldLayoutDTO.class;
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
		// Filter is ignored
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (FieldLayout f : MANAGER.getEditableFieldLayouts()) {
			FieldLayoutDTO dto = new FieldLayoutDTO();
			dto.setJiraObject(f, params);
			result.put(dto.getUniqueKey(), dto);
		}
		return result;
	}

}
