package com.igsl.customapproval.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.igsl.customapproval.CustomApprovalSetup;
import com.igsl.customapproval.CustomApprovalUtil;

@Component
public class CustomApprovalIssueEventListener implements InitializingBean, DisposableBean {

	private static final Logger LOGGER = Logger.getLogger(CustomApprovalIssueEventListener.class);
	private static final UserManager USER_MANAGER = ComponentAccessor.getUserManager();
	private static final IssueManager ISSUE_MANAGER = ComponentAccessor.getIssueManager();
	private static final ObjectMapper OM = new ObjectMapper();
	
	private EventPublisher eventPublisher;
	
	@Autowired
	public CustomApprovalIssueEventListener(@JiraImport EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
	
	// This is invoked after plugin is enabled
	@Override
    public void afterPropertiesSet() throws Exception {
		LOGGER.debug("Registering listener");
        this.eventPublisher.register(this);
    }

	// This is invoked on plugin unload
	@Override
	public void destroy() throws Exception {
		LOGGER.debug("Unregistering listener");
		this.eventPublisher.unregister(this);
	}
	
	@EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
		LOGGER.debug("Issue event received: " + issueEvent.getIssue().getKey());
		CustomField manualRequestParticipant = CustomApprovalSetup.getManualRequestParticipantCustomField();
        CustomField requestParticipant = CustomApprovalSetup.getRequestParticipantField();
		if (manualRequestParticipant != null && requestParticipant != null) {
	        Long eventTypeId = issueEvent.getEventTypeId();
	        Issue issue = issueEvent.getIssue();
        	MutableIssue is = ISSUE_MANAGER.getIssueObject(issue.getKey());
        	if (is != null) {
		        // Check eventTypeId
		        if (eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
		        	// Initialize manualRequestParticipant with requestParticipant
		        	LOGGER.debug("Issue created: " + issue.getKey());
	        		Object value = is.getCustomFieldValue(requestParticipant);
	        		if (value != null) {
	        			try {
	        				LOGGER.debug("Request participant(" + value.getClass() + "): " + OM.writeValueAsString(value));
	        			} catch (Exception ex) {
	        				LOGGER.error("Failed to print Request Participant", ex);
	        			}
	        		}
	        		if (value != null) {
	        			is.setCustomFieldValue(manualRequestParticipant, value);
		        		ISSUE_MANAGER.updateIssue(
		        				CustomApprovalUtil.getAdminUser(), is, EventDispatchOption.DO_NOT_DISPATCH, false);
			        	LOGGER.debug("Updated Manual Request Participant");
	        		}
		        } else if (eventTypeId.equals(EventType.ISSUE_UPDATED_ID)) {
		        	// Update manualParticipant
		        	LOGGER.debug("Issue updated: " + issue.getKey());
		        	List<ApplicationUser> originalList = (List<ApplicationUser>) 
		        			issue.getCustomFieldValue(manualRequestParticipant);
		        	List<String> originalValues = new ArrayList<>();
		        	if (originalList != null) {
		        		for (ApplicationUser user : originalList) {
		        			originalValues.add(user.getKey());
		        		}
		        	}
		        	try {
		        		List<GenericValue> changeList = issueEvent.getChangeLog().getRelated("ChildChangeItem");
		        		if (changeList != null) {
		        			Iterator<GenericValue> it = changeList.iterator();
		        			while (it.hasNext()) {
		        				GenericValue gv = it.next();
		        				LOGGER.debug("Fields: " + OM.writeValueAsString(gv.getAllFields()));
		        				String fieldName = gv.get("field").toString();
		        				if (CustomApprovalUtil.REUQEST_PARTICIPANT_FIELD_NAME.equals(fieldName)) {
		        			        // Value is comma-delimited list of user keys with space
		        					String oldValue = ((String) gv.get("oldvalue")).replaceAll("\\s", "");
		        			        String newValue = ((String) gv.get("newvalue")).replaceAll("\\s", "");
		        			        LOGGER.debug("Old Value: " + oldValue);
		        			        LOGGER.debug("New Value: " + newValue);
		        			        Set<String> oldValues = new HashSet<>();
		        			        for (String s : oldValue.split(",")) {
		        			        	if (!s.isEmpty()) {
		        			        		oldValues.add(s);
		        			        	}
		        			        }
		        			        Set<String> newValues = new HashSet<>();
		        			        for (String s : newValue.split(",")) {
		        			        	if (!s.isEmpty()) {
		        			        		newValues.add(s);
		        			        	}
		        			        }
		        			        LOGGER.debug("Old/new values parsed");
		        			        LOGGER.debug("Old Values: " + OM.writeValueAsString(oldValues));
		        			        LOGGER.debug("New Values: " + OM.writeValueAsString(newValues));
		        			        if (oldValues.equals(newValues)) {
		        			        	// No change, no action
		        			        	LOGGER.debug("Same set, no action");
		        			        } else if (oldValues.containsAll(newValues)) {
		        			        	// Values removed
		        			        	LOGGER.debug("Values removed");
		        			        	oldValues.removeAll(newValues);
		        			        	LOGGER.debug("Items removed: " + OM.writeValueAsString(oldValues));
		        			        	// Remove 
		        			        	originalValues.removeAll(oldValues);
		        			        } else if (newValues.containsAll(oldValues)) {
		        			        	// Values added
		        			        	LOGGER.debug("Values added");
		        			        	newValues.removeAll(oldValues);
		        			        	LOGGER.debug("Items added: " + OM.writeValueAsString(newValues));
		        			        	// Add
		        			        	originalValues.addAll(newValues);
		        			        } else {
		        			        	LOGGER.debug("Old/New values mismatch");
		        			        }
		        			        List<ApplicationUser> newManualRequestParticipant = new ArrayList<>();
		        			        for (String s : originalValues) {
		        			        	ApplicationUser u = USER_MANAGER.getUserByKey(s);
		        			        	if (u != null) {
		        			        		newManualRequestParticipant.add(u);
		        			        	} else {
		        			        		LOGGER.error("User cannot be found with key: " + s);
		        			        	}
		        			        }
		        			        is.setCustomFieldValue(manualRequestParticipant, newManualRequestParticipant);
	        			        	LOGGER.debug("Update Manual Request Participant: " + newManualRequestParticipant);
	        		        		ISSUE_MANAGER.updateIssue(
	        		        				CustomApprovalUtil.getAdminUser(), is, 
	        		        				EventDispatchOption.DO_NOT_DISPATCH, false);
	        			        	LOGGER.debug("Updated Manual Request Participant: " + newManualRequestParticipant);
		        				}
		        			}
		        		}
		        	} catch (Exception ex) {
		        		LOGGER.error("Failed to process issue updated event", ex);
		        	}
		        }
        	} else {
        		LOGGER.error("Unable to retrieve mutable issue for issue " + issue.getKey());
        	}
		} else {
			LOGGER.error("One of the custom fields not found, manualRequestParticipant: " + 
					manualRequestParticipant + 
					" requestParticipant: " + requestParticipant);
		}
	}
}
