package com.igsl.customapproval.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.crowd.event.group.GroupMembershipDeletedEvent;
import com.atlassian.crowd.event.group.GroupMembershipsCreatedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.rest.IssueFinderV2.SearchResult;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.query.Query;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igsl.customapproval.CustomApprovalConfig;
import com.igsl.customapproval.CustomApprovalSetup;
import com.igsl.customapproval.CustomApprovalUtil;
import com.igsl.customapproval.data.ApprovalData;
import com.igsl.customapproval.data.ApprovalSettings;
import com.igsl.customapproval.data.DelegationSetting;
import com.igsl.customapproval.delegation.DelegationUtil;

/**
 * To monitor changes to:
 * 1. Issue Updated: 
 *    a. Check if updated field is Request Participants
 *       Writes changes to Manual Request participant
 *    b. Check if updated field is Approver fields (extracted from ApprovalData)
 *       Recalculate approver list, update Request Participants
 *    
 * 2. Group Membership Updated: 
 *    Recalculate approver list, update Request Participants
 * 
 * When adding to Request Participants, simply add the change into it.
 * 
 * When setting Request Participants from approver list, merge approver list into it.
 * 
 * When removing from Request Participants, compare with Manual Request Participant.
 * Don't remove if item is in Manual Request Participant.
 */
@Component
public class CustomApprovalIssueEventListener implements InitializingBean, DisposableBean {

	private static final Logger LOGGER = Logger.getLogger(CustomApprovalIssueEventListener.class);
	private static final UserManager USER_MANAGER = ComponentAccessor.getUserManager();
	private static final IssueManager ISSUE_MANAGER = ComponentAccessor.getIssueManager();
	private static final JqlQueryParser JQL_PARSER = ComponentAccessor.getComponent(JqlQueryParser.class);
	private static final SearchService SEARCH_SERVICE = ComponentAccessor.getComponent(SearchService.class);
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
	
	private List<Issue> getIssuesWithCustomApproval() {
		List<Issue> result = new ArrayList<>();
		try {
			Query q = JQL_PARSER.parseQuery(CustomApprovalUtil.getDelegationFilter());
			SearchResults<Issue> list = 
					SEARCH_SERVICE.search(CustomApprovalUtil.getAdminUser(), q, PagerFilter.getUnlimitedFilter());
			for (Issue issue : list.getResults()) {
				result.add(issue);
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to search for issues with custom approval", ex);
		}
		return result;
	}
	
	/**
	 * Refreshes issue's Request Participants field by Manual Request Participant and approver list.
	 * @param issue Issue to be updated, if null, all issues with custom approval
	 */
	private void updateRequestParticipants(Issue issue) {
		// Inspect ALL ongoing issues with CustomApproval enabled
		// Refresh Request Participants
		// No shortcuts possible
		CustomField cfRequestParticipants = CustomApprovalSetup.getRequestParticipantField();
		// If Request Participants field is not found, do nothing
		if (cfRequestParticipants == null) {
			LOGGER.debug("Request Participants field not found");
			return;
		}
		CustomField cfManualParticipants = CustomApprovalSetup.getManualRequestParticipantCustomField();
		if (cfManualParticipants == null) {
			LOGGER.debug("Manual Request Participants field not found");
			return;
		}
		CustomField indicatorField = CustomApprovalSetup.getIndicatorCustomField();
		if (indicatorField == null) {
			LOGGER.debug("Issue Updated Event Indicator field not found");
			return;
		}
		List<Issue> issueList = new ArrayList<>();
		if (issue != null) {
			issueList.add(issue);
		} else {
			issueList = getIssuesWithCustomApproval();
		}
		for (Issue is : issueList) {
			LOGGER.debug("Processing issue: " + is.getKey());
			Map<String, ApplicationUser> approvers = CustomApprovalUtil.getApproverList(is);
			if (approvers == null) {
				LOGGER.debug("No approvers");
				continue;
			}
			List<ApplicationUser> delegateTargets = new ArrayList<>();
			for (ApplicationUser delegator : approvers.values()) {
				for (DelegationSetting setting : DelegationUtil.loadData(delegator.getKey(), new Date())) {
					ApplicationUser target = USER_MANAGER.getUserByKey(setting.getDelegateToUser());
					if (target != null) {
						delegateTargets.add(target);
					}
				}
			}
			List<ApplicationUser> participants = 
					(List<ApplicationUser>) is.getCustomFieldValue(cfRequestParticipants);
			List<ApplicationUser> manualParticipants = 
					(List<ApplicationUser>) is.getCustomFieldValue(cfManualParticipants);
			// The returned list is immutable, extract it into a set of user key for comparison
			Set<String> existingParticipants = new HashSet<>();
//			if (participants != null) {
//				for (ApplicationUser user : participants) {
//					LOGGER.debug("Original participant: " + user.getName());
//					existingParticipants.add(user.getKey());
//				}
//			}
			// Add manual participants
			if (manualParticipants != null) {
				for (ApplicationUser user : manualParticipants) {
					LOGGER.debug("Manual participant: " + user.getName());
					existingParticipants.add(user.getKey());
				}
			}
			// Add approvers
			for (ApplicationUser user : approvers.values()) {
				LOGGER.debug("Approver: " + user.getName());
				existingParticipants.add(user.getKey());
			}
			// Add delegations
			for (ApplicationUser user : delegateTargets) {
				LOGGER.debug("Delegation: " + user.getName());
				existingParticipants.add(user.getKey());
			}
			// Form new list
			List<ApplicationUser> newParticipants = new ArrayList<>();
			for (String userKey : existingParticipants) {
				ApplicationUser user = USER_MANAGER.getUserByKey(userKey);
				if (user != null) {
					newParticipants.add(user);
					LOGGER.debug("Participant: " + user.getName());
				} else {
					LOGGER.warn("Request Participants contains invalid user: " + userKey);
				}
			}
			// Update issue
			MutableIssue mi = ISSUE_MANAGER.getIssueObject(is.getKey());
			if (mi == null) {
				LOGGER.error("Unable to retrieve mutable issue: " + is.getKey());
				continue;
			}
			mi.setCustomFieldValue(cfRequestParticipants, newParticipants);
			mi.setCustomFieldValue(indicatorField, Boolean.TRUE.toString());
			LOGGER.error("Issue updated with indicator on: " + is.getKey());
			ISSUE_MANAGER.updateIssue(
					CustomApprovalUtil.getAdminUser(), mi, EventDispatchOption.ISSUE_UPDATED, false);
		}
	}
	
	@EventListener
    public void onGroupMembershipsCreatedEvent(GroupMembershipsCreatedEvent groupEvent) {
		LOGGER.debug("Group membership event created received: " + groupEvent.getGroupName());
		for (String e : groupEvent.getEntityNames()) {
			LOGGER.debug("Entities: " + e);
		}
		LOGGER.debug("End of event: " + groupEvent.getGroupName());
		updateRequestParticipants(null);
	}
	
	@EventListener
    public void onGroupMembershipsDeletedEvent(GroupMembershipDeletedEvent groupEvent) {
		LOGGER.debug("Group membership deleted event received: " + 
				groupEvent.getGroupName() + ": " + groupEvent.getEntityName());
		updateRequestParticipants(null);
	}

	@EventListener
    public void onIssueUpdateEvent(IssueEvent issueEvent) {
		LOGGER.debug("Issue event received: " + issueEvent.getIssue().getKey());
		// Narrow down event type
		Long eventTypeId = issueEvent.getEventTypeId();
		if (!EventType.ISSUE_CREATED_ID.equals(eventTypeId) &&
			!EventType.ISSUE_UPDATED_ID.equals(eventTypeId)) {
			LOGGER.debug("Not created/updated issue event, event ignored");
			return;
		}
		CustomField manualRequestParticipant = CustomApprovalSetup.getManualRequestParticipantCustomField();
        CustomField requestParticipant = CustomApprovalSetup.getRequestParticipantField();
        CustomField indicatorField = CustomApprovalSetup.getIndicatorCustomField();
		if (manualRequestParticipant == null) {
			LOGGER.debug("Manual Request Participant custom field cannot be found, event ignored");
			return;
		}
		if (requestParticipant == null) {
			LOGGER.debug("Request Participants custom field cannot be found, event ignored");
			return;
		}
		if (indicatorField == null) {
			LOGGER.debug("Issue Updated Event Indicator custom field cannot be found, event ignored");
			return;
		}
		Issue issue = issueEvent.getIssue();
		MutableIssue is = ISSUE_MANAGER.getIssueObject(issue.getKey());
		if (is == null) {
			LOGGER.error("Unable to retrieve mutable issue: " + issue.getKey());
			return;
		}
		if (EventType.ISSUE_CREATED_ID.equals(eventTypeId)) {
			// Copy Request Participants to Manual Request Participant
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
	        	LOGGER.debug("Updated Manual Request Participant");
        		ISSUE_MANAGER.updateIssue(
        				CustomApprovalUtil.getAdminUser(), is, EventDispatchOption.DO_NOT_DISPATCH, false);
    		}
		} else if (EventType.ISSUE_UPDATED_ID.equals(eventTypeId)) {
			// Narrow down changed field
			try {
				List<GenericValue> changeList = issueEvent.getChangeLog().getRelated("ChildChangeItem");
				if (changeList == null) {
					LOGGER.warn("No changed field list in issue updated event");
					return;
				}
    			Iterator<GenericValue> it = changeList.iterator();
    			while (it.hasNext()) {
    				GenericValue gv = it.next();
    				LOGGER.debug("Changed fields: " + OM.writeValueAsString(gv.getAllFields()));
    				String fieldName = gv.get("field").toString();
    				if (CustomApprovalUtil.REUQEST_PARTICIPANT_FIELD_NAME.equals(fieldName)) {
    					// Check indicator, if non-null/non-empty, set to null and ignore the event
    					String value = (String) issue.getCustomFieldValue(indicatorField);
		        		LOGGER.debug("Issue updated event indicator: " + value);
    					if (value != null && value.length() != 0) {
    						// Event is from CustomApproval updating request participants
    						// Turn off indicator and ignore
    						is.setCustomFieldValue(indicatorField, null);
			        		LOGGER.debug("Issue updated event ignored");
			        		ISSUE_MANAGER.updateIssue(
			        				CustomApprovalUtil.getAdminUser(), is, 
			        				EventDispatchOption.DO_NOT_DISPATCH, false);
    						continue;
    					}
    					List<ApplicationUser> originalParticipants = (List<ApplicationUser>) 
    		        			issue.getCustomFieldValue(manualRequestParticipant);
    		        	List<String> originalValues = new ArrayList<>();
    		        	if (originalParticipants != null) {
    		        		for (ApplicationUser user : originalParticipants) {
    		        			originalValues.add(user.getKey());
    		        		}
    		        	}
    					// Update Manual Request Participant with the changes
    					// Value is comma-delimited list of user keys with space
    					String oldValue = ((String) gv.get("oldvalue")).replaceAll("\\s", "");
    			        String newValue = ((String) gv.get("newvalue")).replaceAll("\\s", "");
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
    			        LOGGER.debug("Old Values: " + OM.writeValueAsString(oldValues));
    			        LOGGER.debug("New Values: " + OM.writeValueAsString(newValues));
    			        boolean doUpdate = false;
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
    			        	doUpdate = true;
    			        } else if (newValues.containsAll(oldValues)) {
    			        	// Values added
    			        	LOGGER.debug("Values added");
    			        	newValues.removeAll(oldValues);
    			        	LOGGER.debug("Items added: " + OM.writeValueAsString(newValues));
    			        	// Add
    			        	originalValues.addAll(newValues);
    			        	doUpdate = true;
    			        } else {
    			        	LOGGER.debug("Old/New values mismatch, ignoring missing original values");
    			        	newValues.removeAll(oldValues);
    			        	originalValues.addAll(newValues);
    			        	LOGGER.debug("Items added: " + OM.writeValueAsString(newValues));
    			        	doUpdate = true;
    			        }
    			        if (doUpdate) {
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
				        	LOGGER.debug("Updated Manual Request Participant");
    			            ISSUE_MANAGER.updateIssue(
			        				CustomApprovalUtil.getAdminUser(), is, 
			        				EventDispatchOption.DO_NOT_DISPATCH, false);
    			        }
    				} else {
    					// Check if changed field is approver field
    					ApprovalSettings settings = CustomApprovalUtil.getApprovalSettings(issue);
    					if (settings == null) {
    						continue;
    					}
						String groupFieldName = null;
						String groupFieldId = settings.getApproverGroupField();
    					if (groupFieldId != null) {
    						CustomField cf = CustomApprovalSetup.getCustomFieldById(groupFieldId);
    						if (cf != null) {
    							groupFieldName = cf.getName();
    						}
    					}
    					String userFieldName = null;
    					String userFieldId = settings.getApproverUserField();
						if (userFieldId != null) {
    						CustomField cf = CustomApprovalSetup.getCustomFieldById(userFieldId);
    						if (cf != null) {
    							userFieldName = cf.getName();
    						}
    					}
						LOGGER.debug(fieldName + " vs " + userFieldName + ", " + groupFieldName);
    					if (fieldName.equals(userFieldName) || fieldName.equals(groupFieldName)) {
    						updateRequestParticipants(issue);
    					}
    				}
    			}	// For all changed fields
			} catch (Exception ex) {
				LOGGER.error("Failed to process issue updated event", ex);
			}
		}
	}
	
//	@EventListener
//    public void onIssueEvent(IssueEvent issueEvent) {
//		LOGGER.debug("Issue event received: " + issueEvent.getIssue().getKey());
//		CustomField manualRequestParticipant = CustomApprovalSetup.getManualRequestParticipantCustomField();
//        CustomField requestParticipant = CustomApprovalSetup.getRequestParticipantField();
//		if (manualRequestParticipant != null && requestParticipant != null) {
//	        Long eventTypeId = issueEvent.getEventTypeId();
//	        Issue issue = issueEvent.getIssue();
//        	MutableIssue is = ISSUE_MANAGER.getIssueObject(issue.getKey());
//        	if (is != null) {
//		        // Check eventTypeId
//		        if (eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
//		        	// Initialize manualRequestParticipant with requestParticipant
//		        	LOGGER.debug("Issue created: " + issue.getKey());
//	        		Object value = is.getCustomFieldValue(requestParticipant);
//	        		if (value != null) {
//	        			try {
//	        				LOGGER.debug("Request participant(" + value.getClass() + "): " + OM.writeValueAsString(value));
//	        			} catch (Exception ex) {
//	        				LOGGER.error("Failed to print Request Participant", ex);
//	        			}
//	        		}
//	        		if (value != null) {
//	        			is.setCustomFieldValue(manualRequestParticipant, value);
//		        		ISSUE_MANAGER.updateIssue(
//		        				CustomApprovalUtil.getAdminUser(), is, EventDispatchOption.DO_NOT_DISPATCH, false);
//			        	LOGGER.debug("Updated Manual Request Participant");
//	        		}
//		        } else if (eventTypeId.equals(EventType.ISSUE_UPDATED_ID)) {
//		        	// Update manualParticipant
//		        	LOGGER.debug("Issue updated: " + issue.getKey());
//		        	List<ApplicationUser> originalList = (List<ApplicationUser>) 
//		        			issue.getCustomFieldValue(manualRequestParticipant);
//		        	List<String> originalValues = new ArrayList<>();
//		        	if (originalList != null) {
//		        		for (ApplicationUser user : originalList) {
//		        			originalValues.add(user.getKey());
//		        		}
//		        	}
//		        	try {
//		        		List<GenericValue> changeList = issueEvent.getChangeLog().getRelated("ChildChangeItem");
//		        		if (changeList != null) {
//		        			Iterator<GenericValue> it = changeList.iterator();
//		        			while (it.hasNext()) {
//		        				GenericValue gv = it.next();
//		        				LOGGER.debug("Fields: " + OM.writeValueAsString(gv.getAllFields()));
//		        				String fieldName = gv.get("field").toString();
//		        				if (CustomApprovalUtil.REUQEST_PARTICIPANT_FIELD_NAME.equals(fieldName)) {
//		        			        // Value is comma-delimited list of user keys with space
//		        					String oldValue = ((String) gv.get("oldvalue")).replaceAll("\\s", "");
//		        			        String newValue = ((String) gv.get("newvalue")).replaceAll("\\s", "");
//		        			        LOGGER.debug("Old Value: " + oldValue);
//		        			        LOGGER.debug("New Value: " + newValue);
//		        			        Set<String> oldValues = new HashSet<>();
//		        			        for (String s : oldValue.split(",")) {
//		        			        	if (!s.isEmpty()) {
//		        			        		oldValues.add(s);
//		        			        	}
//		        			        }
//		        			        Set<String> newValues = new HashSet<>();
//		        			        for (String s : newValue.split(",")) {
//		        			        	if (!s.isEmpty()) {
//		        			        		newValues.add(s);
//		        			        	}
//		        			        }
//		        			        LOGGER.debug("Old/new values parsed");
//		        			        LOGGER.debug("Old Values: " + OM.writeValueAsString(oldValues));
//		        			        LOGGER.debug("New Values: " + OM.writeValueAsString(newValues));
//		        			        if (oldValues.equals(newValues)) {
//		        			        	// No change, no action
//		        			        	LOGGER.debug("Same set, no action");
//		        			        } else if (oldValues.containsAll(newValues)) {
//		        			        	// Values removed
//		        			        	LOGGER.debug("Values removed");
//		        			        	oldValues.removeAll(newValues);
//		        			        	LOGGER.debug("Items removed: " + OM.writeValueAsString(oldValues));
//		        			        	// Remove 
//		        			        	originalValues.removeAll(oldValues);
//		        			        } else if (newValues.containsAll(oldValues)) {
//		        			        	// Values added
//		        			        	LOGGER.debug("Values added");
//		        			        	newValues.removeAll(oldValues);
//		        			        	LOGGER.debug("Items added: " + OM.writeValueAsString(newValues));
//		        			        	// Add
//		        			        	originalValues.addAll(newValues);
//		        			        } else {
//		        			        	LOGGER.debug("Old/New values mismatch");
//		        			        }
//		        			        List<ApplicationUser> newManualRequestParticipant = new ArrayList<>();
//		        			        for (String s : originalValues) {
//		        			        	ApplicationUser u = USER_MANAGER.getUserByKey(s);
//		        			        	if (u != null) {
//		        			        		newManualRequestParticipant.add(u);
//		        			        	} else {
//		        			        		LOGGER.error("User cannot be found with key: " + s);
//		        			        	}
//		        			        }
//		        			        is.setCustomFieldValue(manualRequestParticipant, newManualRequestParticipant);
//	        			        	LOGGER.debug("Update Manual Request Participant: " + newManualRequestParticipant);
//	        		        		ISSUE_MANAGER.updateIssue(
//	        		        				CustomApprovalUtil.getAdminUser(), is, 
//	        		        				EventDispatchOption.DO_NOT_DISPATCH, false);
//	        			        	LOGGER.debug("Updated Manual Request Participant: " + newManualRequestParticipant);
//		        				}
//		        			}
//		        		}
//		        	} catch (Exception ex) {
//		        		LOGGER.error("Failed to process issue updated event", ex);
//		        	}
//		        }
//        	} else {
//        		LOGGER.error("Unable to retrieve mutable issue for issue " + issue.getKey());
//        	}
//		} else {
//			LOGGER.error("One of the custom fields not found, manualRequestParticipant: " + 
//					manualRequestParticipant + 
//					" requestParticipant: " + requestParticipant);
//		}
//	}
	
}
