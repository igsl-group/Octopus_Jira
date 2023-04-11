package com.igsl.configmigration.version;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.project.ProjectDTO;
import com.igsl.configmigration.project.ProjectUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class VersionUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(VersionUtil.class);
	private static VersionManager MANAGER = ComponentAccessor.getVersionManager();
	
	@Override
	public String getName() {
		return "Version";
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
			String key;
			if (it.getProject() == null) {
				key = it.getName();
			} else {
				key = it.getProject().getName() + " - " + it.getName();
			}
			if (uniqueKey.equals(key)) {
				VersionDTO item = new VersionDTO();
				item.setJiraObject(it);
				return item;
			}
		}
		return null;
	}

	@Override
	public MergeResult merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
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
			result.setNewDTO(findByInternalId(Long.toString(originalJira.getId())));
		} else {
			// Project
			ProjectUtil projUtil = (ProjectUtil) JiraConfigTypeRegistry.getConfigUtil(ProjectUtil.class);
			ProjectDTO p = (ProjectDTO) projUtil.findByUniqueKey(src.getUniqueKey());
			// Create
			Version createdJira = MANAGER.createVersion(
					src.getName(),
					src.getStartDate(),
					src.getReleaseDate(),
					src.getDescription(),
					((p != null)? p.getId() : null), 
					null,	// TODO Find previous version
					src.isReleased());
			VersionDTO created = new VersionDTO();
			created.setJiraObject(createdJira);
			result.setNewDTO(created);
		}
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return VersionDTO.class;
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
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (Version it : MANAGER.getAllVersions()) {
			VersionDTO item = new VersionDTO();
			item.setJiraObject(it);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
