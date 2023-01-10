package com.igsl.configmigration.version;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class VersionUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(VersionUtil.class);
	private static VersionManager MANAGER = ComponentAccessor.getVersionManager();
	
	@Override
	public String getName() {
		return "Version";
	}
	
	@Override
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (Version it : MANAGER.getAllVersions()) {
			VersionDTO item = new VersionDTO();
			item.setJiraObject(it);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Version s = MANAGER.getVersion(Long.parseLong(id));
		if (s != null) {
			VersionDTO item = new VersionDTO();
			item.setJiraObject(s);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (Version it : MANAGER.getAllVersions()) {
			if (uniqueKey.equals(it.getName())) {
				VersionDTO item = new VersionDTO();
				item.setJiraObject(it);
				return item;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		VersionDTO original = null;
		if (oldItem != null) {
			original = (VersionDTO) oldItem;
		} else {
			original = (VersionDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		Version originalJira = (original != null)? (Version) original.getJiraObject(): null;
		VersionDTO src = (VersionDTO) newItem;
		if (original != null) {
			// Update
			originalJira = MANAGER.editVersionDetails(originalJira, src.getName(), src.getDescription());
			originalJira = MANAGER.editVersionReleaseDate(originalJira, src.getReleaseDate());
			originalJira = MANAGER.editVersionStartDate(originalJira, src.getStartDate());
			MANAGER.update(originalJira);
			return findByInternalId(Long.toString(originalJira.getId()));
		} else {
			// Create
			Version createdJira = MANAGER.createVersion(
					src.getName(),
					src.getStartDate(),
					src.getReleaseDate(),
					src.getDescription(),
					src.getProjectId(),	// TODO Map project ID
					null,	// TODO Find previous version
					src.isReleased());
			VersionDTO created = new VersionDTO();
			created.setJiraObject(createdJira);
			return created;
		}
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return VersionDTO.class;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

}
