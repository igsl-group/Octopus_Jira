package com.igsl.configmigration.options;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.fieldconfig.FieldConfigDTO;

public class OptionUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(OptionUtil.class);
	private static final OptionsManager MANAGER = ComponentAccessor.getOptionsManager();
	
	@Override
	public boolean isPublic() {
		return false;
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
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		throw new Exception("Not implemented, Option is not searchable");
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
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		Option created = null;
		OptionDTO src = (OptionDTO) oldItem;
		if (src != null) {
			MANAGER.deleteOptionAndChildren((Option) oldItem.getJiraObject());
		}
		OptionDTO tar = (OptionDTO) newItem;
		FieldConfigDTO fieldConfigDTO = (FieldConfigDTO) newItem.getSearchParameters()[0];
		FieldConfig fieldConfig = (FieldConfig) fieldConfigDTO.getJiraObject();
		if (tar.getParentId() != null) {
			created = MANAGER.createOption(fieldConfig, tar.getParentId(), tar.getSequence(), tar.getValue());
		} else {
			created = MANAGER.createOption(fieldConfig, null, tar.getSequence(), tar.getValue());
		}
		tar.setOptionId(created.getOptionId());
		if (created != null && tar.getChildOptions() != null) {
			for (OptionDTO child : tar.getChildOptions()) {
				child.setParentId(tar.getOptionId());
				OptionDTO createdChil = (OptionDTO) merge(null, child);
				child.setOptionId(createdChil.getOptionId());
			}
		}
		return tar;
	}

}
