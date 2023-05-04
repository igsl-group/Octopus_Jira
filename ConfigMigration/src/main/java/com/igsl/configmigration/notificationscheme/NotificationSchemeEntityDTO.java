package com.igsl.configmigration.notificationscheme;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.applicationuser.ApplicationUserDTO;
import com.igsl.configmigration.general.GeneralDTO;
import com.igsl.configmigration.general.GeneralUtil;
import com.igsl.configmigration.projectrole.ProjectRoleDTO;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class NotificationSchemeEntityDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(NotificationSchemeEntityDTO.class);
	
	private static final NotificationTypeManager NOTIFICATION_TYPE_MANAGER = 
			ComponentAccessor.getComponent(NotificationTypeManager.class);
			
	private Long id;
	private GeneralDTO parameter;
	private NotificationTypeDTO type;
	private EventTypeDTO eventType;
	private EventTypeDTO templateId;
	
	/*
	 * Notes: 
	 * - User/Group custom field cannot be used, no fields can be selected?
	 * - templateId is null in all other cases
	 * - parameter stores ID which requires remapping.
	 * - eventType is things like Issue Created.
	 * - type indicates what the recipient type is, also indicating what parameter is.
	 * - Where can I find a list of types? 
	 */
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		SchemeEntity e = (SchemeEntity) o;
		EventTypeUtil evUtil = (EventTypeUtil) JiraConfigTypeRegistry.getConfigUtil(EventTypeUtil.class);
		if (e.getEntityTypeId() != null) {
			this.eventType = (EventTypeDTO) evUtil.findByInternalId(String.valueOf(e.getEntityTypeId()));
		}
		this.id = e.getId();
		if (e.getTemplateId() != null) {
			this.templateId = (EventTypeDTO) evUtil.findByInternalId(String.valueOf(e.getTemplateId()));
		}
		this.type = new NotificationTypeDTO();
		this.type.setJiraObject(NOTIFICATION_TYPE_MANAGER.getSchemeType(e.getType()));
		NotificationTypeUtil ntUtil = (NotificationTypeUtil) JiraConfigTypeRegistry.getConfigUtil(NotificationTypeUtil.class);
		this.parameter = ntUtil.parseParameter(e.getParameter(), this.type);
		this.uniqueKey = Long.toString(this.id);
	}
	
	@Override
	public int getObjectParameterCount() {
		// 0: NotificationSchemeDTO
		return 1;
	}

	@Override
	public void setupRelatedObjects() {
		NotificationSchemeDTO scheme = (NotificationSchemeDTO) this.objectParameters[0];
		if (scheme != null) {
			// Note: Collection value not supported as all NotificationType uses a single object
			if (this.parameter != null && this.parameter.getValue() != null) {
				Object value = this.parameter.getValue();
				if (value instanceof JiraConfigDTO) {
					JiraConfigDTO dto = (JiraConfigDTO) this.parameter.getValue();
					scheme.addRelatedObject(dto);
					dto.addReferencedObject(scheme);
				}
			}
		}
	}
	
	@Override
	public String getConfigName() {
		StringBuilder sb = new StringBuilder();
		sb	.append(this.eventType.getName())
			.append(" (")
			.append(this.type.getDisplayName());
		if (this.parameter != null) {
			sb.append(": ").append(this.parameter.getConfigName());
		}
		sb.append(")");
		return sb.toString(); 
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Parameter", new JiraConfigProperty(GeneralUtil.class, this.parameter));
		r.put("Notification Type", new JiraConfigProperty(NotificationTypeUtil.class, this.type));
		r.put("Event Type", new JiraConfigProperty(EventTypeUtil.class, this.eventType));
		r.put("Template", new JiraConfigProperty(EventTypeUtil.class, this.templateId));
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
		return NotificationSchemeEntityUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return SchemeEntity.class;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public GeneralDTO getParameter() {
		return parameter;
	}

	public void setParameter(GeneralDTO parameter) {
		this.parameter = parameter;
	}

	public EventTypeDTO getEventType() {
		return eventType;
	}

	public void setEventType(EventTypeDTO eventType) {
		this.eventType = eventType;
	}

	public NotificationTypeDTO getType() {
		return type;
	}

	public void setType(NotificationTypeDTO type) {
		this.type = type;
	}

	public EventTypeDTO getTemplateId() {
		return templateId;
	}

	public void setTemplateId(EventTypeDTO templateId) {
		this.templateId = templateId;
	}

}
