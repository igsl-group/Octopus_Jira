package com.igsl.configmigration.fieldlayoutscheme;

import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldLayoutSchemeEntityUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(FieldLayoutSchemeEntityUtil.class);
	private static final FieldLayoutManager MANAGER = ComponentAccessor.getFieldLayoutManager();
	
	@Override
	public String getName() {
		return "Field Confguration Scheme Entity";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		FieldLayoutSchemeDTO schemeDTO = (FieldLayoutSchemeDTO) params[0];
		FieldLayoutScheme scheme = (FieldLayoutScheme) schemeDTO.getJiraObject();
		Long idAsLong = Long.parseLong(id);
		for (FieldLayoutSchemeEntity entity : MANAGER.getFieldLayoutSchemeEntities(scheme)) {
			if (entity.getId().equals(idAsLong)) {
				FieldLayoutSchemeDTO item = new FieldLayoutSchemeDTO();
				item.setJiraObject(scheme);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		return findByInternalId(uniqueKey, params);
	}

	@Override
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		throw new Exception("FieldLayoutSchemeEntity is read only");
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return FieldLayoutSchemeEntityDTO.class;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
//		FieldLayoutSchemeDTO schemeDTO = (FieldLayoutSchemeDTO) params[0];
//		FieldLayoutScheme scheme = (FieldLayoutScheme) schemeDTO.getJiraObject();
//		Map<String, JiraConfigDTO> result = new TreeMap<>();
//		for (FieldLayoutSchemeEntity entity : MANAGER.getFieldLayoutSchemeEntities(scheme)) {
//			FieldLayoutSchemeEntityDTO item = new FieldLayoutSchemeEntityDTO();
//			item.setJiraObject(entity, schemeDTO);
//			if (!matchFilter(item, filter)) {
//				continue;
//			}
//			result.put(item.getUniqueKey(), item);
//		}
//		return result;
		return Collections.emptyMap();
	}

}
