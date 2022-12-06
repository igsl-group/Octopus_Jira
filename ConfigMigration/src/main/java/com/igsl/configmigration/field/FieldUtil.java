package com.igsl.configmigration.field;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(FieldUtil.class);
	private static FieldManager MANAGER = ComponentAccessor.getFieldManager();
	
	@Override
	public String getName() {
		return "Field";
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (Field f : MANAGER.getAllAvailableNavigableFields()) {
			FieldDTO dto = new FieldDTO();
			dto.setJiraObject(f);
			result.put(f.getId(), dto);
		}
		for (Field f : MANAGER.getAllSearchableFields()) {
			FieldDTO dto = new FieldDTO();
			dto.setJiraObject(f);
			result.put(f.getId(), dto);
		}
		return result;
	}

	/**
	 * #0: owner as String, optional
	 */
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Field f = MANAGER.getField(id);
		if (f != null) {
			FieldDTO dto = new FieldDTO();
			dto.setJiraObject(f);
			return dto;
		}
		return null;
	}

	/**
	 * #0: owner as String, optional
	 */
	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (Field f : MANAGER.getAllAvailableNavigableFields()) {
			if (f.getName().equals(uniqueKey)) {
				FieldDTO dto = new FieldDTO();
				dto.setJiraObject(f);
				return dto;
			}
		}
		for (Field f : MANAGER.getAllSearchableFields()) {
			if (f.getName().equals(uniqueKey)) {
				FieldDTO dto = new FieldDTO();
				dto.setJiraObject(f);
				return dto;
			}
		}
		return null;
	}
	
	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		throw new Exception("Field is not modifiable");
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return FieldDTO.class;
	}

	@Override
	public boolean isPublic() {
		return true;
	}

}
