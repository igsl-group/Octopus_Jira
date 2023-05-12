package com.igsl.configmigration.applicationrole;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.application.ApplicationRole;
import com.atlassian.jira.application.ApplicationRoleManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ApplicationRoleUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ApplicationRoleUtil.class);
	private static final ApplicationRoleManager MANAGER = 
			ComponentAccessor.getComponent(ApplicationRoleManager.class);
	
	@Override
	public String getName() {
		return "Application Role";
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Map<String, JiraConfigDTO> all = search(null, params);
		for (JiraConfigDTO item : all.values()) {
			if (item.getInternalId().equals(id)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * #0: owner as String, optional
	 */
	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		Map<String, JiraConfigDTO> all = search(null, params);
		if (all.containsKey(uniqueKey)) {
			return all.get(uniqueKey);
		}
		return null;
	}
	
	@Override
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		throw new Exception("Application Role is read only");
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ApplicationRoleDTO.class;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	public boolean isReadOnly() {
		return true;
	}
	
	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		// Filter is ignored
		Map<String, JiraConfigDTO> result = new LinkedHashMap<>();
		for (ApplicationRole ar : MANAGER.getRoles()) {
			ApplicationRoleDTO dto = new ApplicationRoleDTO();
			dto.setJiraObject(ar);
			result.put(dto.getUniqueKey(), dto);
		}
		return result;
	}

}
