package com.igsl.configmigration.version;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class VersionDTO extends JiraConfigDTO {

	private String description;
	private Long id;
	private String name;
	private Long projectId;
	private Date releaseDate;
	private Long sequence;
	private Date startDate;
	private boolean archived;
	private boolean released;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		Version o = (Version) obj;
		this.description = o.getDescription();
		this.id = o.getId();
		this.name = o.getName();
		this.projectId = o.getProjectId();
		this.releaseDate = o.getReleaseDate();
		this.sequence = o.getSequence();
		this.startDate = o.getStartDate();
		this.released = o.isReleased();
		this.archived = o.isArchived();
		if (o.getProject() != null) {
			this.uniqueKey = o.getProject().getName() + " - " + this.name;
		} else {
			this.uniqueKey = this.name;
		}
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Project ID", new JiraConfigProperty(this.projectId));
		r.put("Release Date", new JiraConfigProperty(this.releaseDate));
		r.put("Sequence", new JiraConfigProperty(this.sequence));
		r.put("Start Date", new JiraConfigProperty(this.startDate));
		r.put("Released", new JiraConfigProperty(this.released));
		r.put("Archived", new JiraConfigProperty(this.archived));
		return r;
	}
	
	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getDescription",
				"getName",
				"getReleaseDate",
				"getSequence",
				"getStartDate",
				"isReleased",
				"isArchived");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return VersionUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return Version.class;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public Long getSequence() {
		return sequence;
	}

	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public boolean isReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}

}
