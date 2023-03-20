package com.igsl.configmigration.insight;

import java.util.Collections;
import java.util.Map;

import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

public class ObjectSchemaPropertyBeanUtil extends JiraConfigUtil {

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public String getName() {
		return "Object Schema Property Bean";
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ObjectSchemaPropertyBeanDTO.class;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		return null;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		return Collections.emptyMap();
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
