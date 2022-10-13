package com.igsl.ldapuserattributes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.opensymphony.module.propertyset.PropertySet;

@Named
public class LDAPUserAttributeSyncJob implements JobRunner {
	
	private final Logger LOGGER = Logger.getLogger(LDAPUserAttributeSyncJob.class);
	private static final ObjectMapper OM = new ObjectMapper();
	private static final String NEWLINE = "; ";
	public static final String JIRA_USER_PROPERTY_PREFIX = "jira.meta.";
	public static final int STRING_LIMIT = 255;	// Max data size for String property (text is unlimited)
	
	@Override
	public JobRunnerResponse runJob(JobRunnerRequest request) {
		try {
			LOGGER.debug("runJob");
			UserSearchService uss = ComponentAccessor.getUserSearchService();
			UserPropertyManager upm = ComponentAccessor.getUserPropertyManager();
			StringBuilder sb = new StringBuilder();
			LDAPUserAttributesConfigData data = LDAPUserAttributesAction.getData();
			String userNameAttribute = data.getUserNameAttribute();
			Map<String, String> attributeMap = data.getAttributeMap();
			Set<String> attributeList = new HashSet<String>();
			attributeList.addAll(attributeMap.keySet());
			attributeList.add(data.getUserNameAttribute());	// Always include user name attribute
			Map<String, Map<String, List<String>>> output = LDAPUserAttributes.getLDAPUsers(data);
			sb.append("LDAP filter: ").append(data.getFilter()).append(NEWLINE);
			sb.append("LDAP users found: ").append(output.size()).append(NEWLINE);
			int jiraUserFound = 0;
			int jiraUserSynced = 0;
			for (Map.Entry<String, Map<String, List<String>>> entry : output.entrySet()) {
				String user = entry.getKey();
				Map<String, List<String>> attributes = entry.getValue();
				List<String> userName = attributes.get(userNameAttribute);
				if (userName != null && userName.size() == 1) {
					ApplicationUser jiraUser = uss.getUserByName(ComponentAccessor.getComponent(JiraServiceContext.class), userName.get(0));
					if (jiraUser == null) {
						LOGGER.error("Cannot find Jira user [" + userName.get(0) + "]");
					} else {
						jiraUserFound++;
						PropertySet ps = upm.getPropertySet(jiraUser);
						// According to Jira API doc, PropertySet is a live object and changes will be persisted.
						// Each key can be associated to a data type (String, text, etc.).
						// A key is unique across all data types.
						// If a key has an existing data of type 1, but you try to assign a new data of type 2, exception will be thrown.
						// String data type is limited to 255 character. This is also the only type displayed in Jira web UI.
						// Text data type has unlimited length, but Jira web UI won't display them.
						for (Map.Entry<String, String> attr : data.getAttributeMap().entrySet()) {
							List<String> values = attributes.get(attr.getKey());
							ps.remove(JIRA_USER_PROPERTY_PREFIX + attr.getKey());
							if (values != null) {
								String valueAsString = null;
								if (values.size() == 1) {
									valueAsString = values.get(0);
								} else {
									valueAsString = OM.writeValueAsString(values);
								}
								if (valueAsString != null && valueAsString.length() > STRING_LIMIT) {
									valueAsString = valueAsString.substring(0, STRING_LIMIT);
									LOGGER.warn("Value " + attr.getKey() + " for user " + userName.get(0) + " is too long and has been truncated");
								}
								ps.setString(JIRA_USER_PROPERTY_PREFIX + attr.getValue(), valueAsString);
								LOGGER.debug("Set [" + JIRA_USER_PROPERTY_PREFIX + attr.getValue() + "] = [" + valueAsString + "]");
							} else {
								LOGGER.debug("Clear [" + JIRA_USER_PROPERTY_PREFIX + attr.getValue() + "]");
							}
						}
						jiraUserSynced++;
					}
				} else {
					LOGGER.error("No user name attribute for LDAP user [" + user + "]");
				}
			}
			sb.append("Matching Jira users found: ").append(jiraUserFound).append(NEWLINE);
			sb.append("Jira users synchronized: ").append(jiraUserSynced).append(NEWLINE);
			return JobRunnerResponse.success(sb.toString());
		} catch (Exception ex) {
			LOGGER.error("SyncJob Exception", ex);
			return JobRunnerResponse.failed(ex.getMessage());
		}
	}

}
