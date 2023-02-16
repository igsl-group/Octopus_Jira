package com.igsl.configmigration.fieldconfig;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigImpl;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

public class FieldConfigUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(FieldConfigUtil.class);
	private static final FieldConfigManager MANAGER = 
			ComponentAccessor.getComponent(FieldConfigManager.class);
	
	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public String getName() {
		return "Field Config";
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return FieldConfigDTO.class;
	}

	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		throw new Exception("FieldConfigManager cannot find all items");
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Long idAsLong = Long.parseLong(id);
		FieldConfig fc = MANAGER.getFieldConfig(idAsLong);
		if (fc != null) {
			FieldConfigDTO dto = new FieldConfigDTO();
			dto.setJiraObject(fc, params);
			return dto;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		throw new Exception("FieldConfigManager cannot find by unique key");
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		FieldConfigDTO original = null;
		if (oldItem != null) {
			original = (FieldConfigDTO) oldItem;
		}
		FieldConfigDTO src = (FieldConfigDTO) newItem;
		if (original != null) {
			LOGGER.debug("Update FieldConfigDTO: " + original.getDescription() + ", " + original.getFieldId() + ", " + original.getName());
			original.setDescription(src.getDescription());
			original.setFieldId(src.getFieldId());
			original.setName(src.getName());
			// TODO Field may not be custom field and thus has no field config...?
			MANAGER.updateFieldConfig((FieldConfig) original.getJiraObject());
			return original;
		} else {
			LOGGER.debug("Create FieldConfigDTO: " + src.getDescription() + ", " + src.getFieldId() + ", " + src.getName());
			FieldConfig newFC = new FieldConfigImpl(
					null, 
					src.getName(), 
					src.getDescription(), 
					null, 
					src.getFieldId());
			FieldConfig createdJira = MANAGER.createFieldConfig(newFC, null);
			FieldConfigDTO created = new FieldConfigDTO();
			created.setJiraObject(createdJira);
			return created;
		}
	}

}
