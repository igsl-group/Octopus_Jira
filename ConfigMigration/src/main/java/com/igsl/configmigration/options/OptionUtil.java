package com.igsl.configmigration.options;

import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.fieldconfig.FieldConfigDTO;
import com.igsl.configmigration.fieldconfig.FieldConfigUtil;
import com.igsl.configmigration.general.GeneralDTO;

public class OptionUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(OptionUtil.class);
	private static final OptionsManager MANAGER = ComponentAccessor.getOptionsManager();
	
	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public String getName() {
		return "Option";
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return OptionDTO.class;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		throw new Exception("Not implemented, Option is not searchable");
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		throw new Exception("Not implemented, Option is not searchable");
	}

	@Override
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		Option created = null;
		OptionDTO src = (OptionDTO) oldItem;
		if (src != null) {
			MANAGER.deleteOptionAndChildren((Option) oldItem.getJiraObject());
		}
		OptionDTO tar = (OptionDTO) newItem;
		LOGGER.debug("OptionDTO newItem: " + OM.writeValueAsString(tar));
		FieldConfigDTO fieldConfig = (FieldConfigDTO) tar.getObjectParameters()[0];
		Long parentId = (Long) tar.getObjectParameters()[1];
		tar.setParentId(parentId);
		LOGGER.debug("OptionUtil fieldConfig: " + fieldConfig);
		LOGGER.debug("OptionUtil getParentId(): " + tar.getParentId());
		LOGGER.debug("OptionUtil getSequence(): " + tar.getSequence());
		LOGGER.debug("OptionUtil getValue(): " + tar.getValue());
		if (parentId != null) {
			created = MANAGER.createOption((FieldConfig) fieldConfig.getJiraObject(), parentId, tar.getSequence(), tar.getValue());
		} else {
			created = MANAGER.createOption((FieldConfig) fieldConfig.getJiraObject(), null, tar.getSequence(), tar.getValue());
		}
		LOGGER.debug("OptionUtil created OptionID: " + created.getOptionId());
		if (created != null && tar.getChildOptions() != null) {
			for (OptionDTO child : tar.getChildOptions()) {
				child.setJiraObject(null, fieldConfig, created.getOptionId());
				child.setParentId(created.getOptionId());
				OptionDTO createdChild = (OptionDTO) merge(exportStore, null, importStore, child).getNewDTO();
			}
		}
		tar.setJiraObject(created, fieldConfig, parentId);
		result.setNewDTO(tar);
		return result;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		return Collections.emptyMap();
	}

}
