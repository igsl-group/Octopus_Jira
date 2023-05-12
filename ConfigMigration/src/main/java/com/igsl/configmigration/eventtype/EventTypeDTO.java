package com.igsl.configmigration.eventtype;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class EventTypeDTO extends JiraConfigDTO {

	private static final EventTypeManager MANAGER = ComponentAccessor.getEventTypeManager();
	
	private Long id;
	private String name;
	private String description;
	private Long templateId;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		EventType ev = (EventType) o;
		this.id = ev.getId();
		this.description = ev.getDescription();
		this.name = ev.getName();
		this.templateId = ev.getTemplateId();
		this.uniqueKey = this.name;
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Template ID", new JiraConfigProperty(this.templateId));
		return r;
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getDescription",
				"getCompleteIconUrl",
				"getStatusColor",
				"getSvgIconUrl",
				"getSequence",
				"getIconUrl",
				"getRasterIconUrl");
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.id);
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return EventTypeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return EventType.class;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

}
