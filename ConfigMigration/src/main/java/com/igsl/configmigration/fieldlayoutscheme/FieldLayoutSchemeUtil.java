package com.igsl.configmigration.fieldlayoutscheme;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntityImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeImpl;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.fieldlayout.FieldLayoutDTO;
import com.igsl.configmigration.fieldlayout.FieldLayoutUtil;
import com.igsl.configmigration.issuetype.IssueTypeDTO;
import com.igsl.configmigration.issuetype.IssueTypeUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldLayoutSchemeUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(FieldLayoutSchemeUtil.class);
	private static final FieldLayoutManager MANAGER = ComponentAccessor.getFieldLayoutManager();
	private static final ConstantsManager CONSTANTS_MANAGER = ComponentAccessor.getConstantsManager();
	
	@Override
	public String getName() {
		return "Field Configuration Scheme";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		FieldLayoutScheme scheme = MANAGER.getMutableFieldLayoutScheme(idAsLong);
		if (scheme != null) {
			FieldLayoutSchemeDTO dto = new FieldLayoutSchemeDTO();
			dto.setJiraObject(scheme);
			return dto;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (FieldLayoutScheme scheme : MANAGER.getFieldLayoutSchemes()) {
			if (scheme.getName().equals(uniqueKey)) {
				FieldLayoutSchemeDTO item = new FieldLayoutSchemeDTO();
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
		FieldLayoutSchemeDTO original = null;
		if (oldItem != null) {
			original = (FieldLayoutSchemeDTO) oldItem;
		} else {
			original = (FieldLayoutSchemeDTO) findByDTO(newItem);
		}
		FieldLayoutSchemeDTO src = (FieldLayoutSchemeDTO) newItem;
		if (original != null) {
			// Delete and recreate
			MANAGER.deleteFieldLayoutScheme((FieldLayoutScheme) original.getJiraObject());
		}
		// Create
		FieldLayoutScheme createdJira = new FieldLayoutSchemeImpl(MANAGER, null);
		createdJira.setDescription(src.getDescription());
		createdJira.setName(src.getName());
		createdJira = MANAGER.createFieldLayoutScheme(createdJira);
		// Create entities
		FieldLayoutUtil flUtil = (FieldLayoutUtil) JiraConfigTypeRegistry.getConfigUtil(FieldLayoutUtil.class);
		IssueTypeUtil itUtil = (IssueTypeUtil) JiraConfigTypeRegistry.getConfigUtil(IssueTypeUtil.class);
		for (FieldLayoutSchemeEntityDTO e : src.getEntities()) {
			FieldLayoutSchemeEntity entity = new FieldLayoutSchemeEntityImpl(MANAGER, null, CONSTANTS_MANAGER);
			if (e.getFieldLayout() != null) {
				FieldLayoutDTO layout = (FieldLayoutDTO) flUtil.findByDTO(e.getFieldLayout());
				entity.setFieldLayoutId(layout.getId());
			}
			entity.setFieldLayoutScheme(createdJira);
			if (e.getIssueType() != null) {
				IssueTypeDTO issueType = (IssueTypeDTO) itUtil.findByDTO(e.getIssueType());
				entity.setIssueTypeId(issueType.getId());
			}
			MANAGER.createFieldLayoutSchemeEntity(entity);
		}
		FieldLayoutSchemeDTO created = new FieldLayoutSchemeDTO();
		created.setJiraObject(createdJira);
		result.setNewDTO(created);
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return FieldLayoutSchemeDTO.class;
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
		for (FieldLayoutScheme scheme : MANAGER.getFieldLayoutSchemes()) {
			LOGGER.debug("Checking scheme: " + scheme.getName() + " : " + scheme.getDescription());
			FieldLayoutSchemeDTO item = new FieldLayoutSchemeDTO();
			item.setJiraObject(scheme);
			if (!matchFilter(item, filter)) {
				LOGGER.debug("Not match filter");
				continue;
			}
			LOGGER.debug("Matches filter");
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
