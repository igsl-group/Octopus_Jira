package com.igsl.configmigration.project;

import java.util.Map;

import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

public class ProjectTypeKeyUtil extends JiraConfigUtil {

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public String getName() {
		return "Project Type Key";
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ProjectTypeKeyDTO.class;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		return null;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		return null;
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		return null;
	}

}
