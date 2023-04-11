package com.igsl.configmigration.licensedapplication;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.application.api.ApplicationKey;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.jira.license.LicenseDetails;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class LicensedApplicationUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(LicensedApplicationUtil.class);
	private static final JiraLicenseManager MANAGER = ComponentAccessor.getComponent(JiraLicenseManager.class);
	
	@Override
	public String getName() {
		return "Licensed Application";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		for (LicenseDetails details : MANAGER.getLicenses()) {
			for (ApplicationKey key : details.getLicensedApplications().getKeys()) {
				if (id.equals(key.value())) {
					LicensedApplicationDTO dto = new LicensedApplicationDTO();
					dto.setJiraObject(key);
					return dto;
				}
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		return findByInternalId(uniqueKey, params);
	}
	
	@Override
	public MergeResult merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		throw new Exception("Application migration is not supported");
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return LicensedApplicationDTO.class;
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
		// Filter ignored
		Map<String, JiraConfigDTO> result = new HashMap<>();
		for (LicenseDetails details : MANAGER.getLicenses()) {
			for (ApplicationKey key : details.getLicensedApplications().getKeys()) {
				LicensedApplicationDTO dto = new LicensedApplicationDTO();
				dto.setJiraObject(key);
				result.put(dto.getUniqueKey(), dto);
			}
		}
		return result;
	}

}
