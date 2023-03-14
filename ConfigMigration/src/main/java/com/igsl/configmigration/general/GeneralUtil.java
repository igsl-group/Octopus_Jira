package com.igsl.configmigration.general;

import java.util.Map;

import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

public class GeneralUtil extends JiraConfigUtil {

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public String getName() {
		return "General";
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return GeneralDTO.class;
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
