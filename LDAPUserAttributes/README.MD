# Jira LDAP User Attribute Sync Plugin

## Purpose
Jira Data Center can only synchronize names and email from Active Directory to Jira user profile. 
This plugin allows additional LDAP attributes to be synchronized to Jira user properties. 

## Implementation Details
This plugin provides: 
1. A configuration page.
1. A scheduled job to synchronize LDAP attributes to Jira user properties. 

## Configuration Details
Configuration page is accessed from Gear | Add-ons | LDAP User Attributes Synchronization. It contains: 
1. Provider URL
1. Base DN
1. Search Scope (Object | One Level | Subtree)
1. User Search Filter
1. Filter Expired User (Ignore | Include)
1. Account Expiry Attribute
1. LDAP Query Page Size
1. LDAP Referral
1. Username Attribute
1. User
1. Password
1. Synchronize Time
1. Synchronize Frequency
1. LDAP Attribute to Jira User Property map

## Scheduled Job Details
You can access scheduled jobs from Gear | System | Scheduler details. 
The entry is named "com.igsl.ldapuserattributes.LDAPUserAttributeSyncJob".
The message is limited to 255 characters (WTF Jira), so only the user counts are logged here.
For details, search atlassian-jira.log for "LDAPUserAttribute" class. 
For extra details, turn on DEBUG log level for com.igsl.ldapuserattributes.LDAPUserAttributes.

## User Properties
You can view user properties in Gear | User Management | Users, click on a user and scroll to bottom.
You can edit them via the Action button in upper right.

## Notes about Jira Properties
1. The User Management page only displays properties with names prefixed "jira.meta.". This plugin adds the prefix on all properties. 
1. Jira Java API allows several specific data types for properties. String is limited to 255 characters, Text is unlimited length but won't be displayed on User Management page. 
1. Octopus considers 255 characters sufficient, so this plugin uses String property type only.

## Access Jira User Properties
1. If you use ScriptRunner scripted custom field, the user property will be always synchronized to the field.
1. If you want to snapshot a property, you should assign the value to a custom field during a specific event. 
1. You can access the properties via this Groovy script: 
```
import com.atlassian.jira.user.UserPropertyManager
import com.opensymphony.module.propertyset.PropertySet
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import com.atlassian.jira.component.ComponentAccessor
UserManager uMan = ComponentAccessor.getUserManager()
if (issue.assignee == null) {
    return null
}
ApplicationUser u = uMan.getUserByName(issue.assignee.username)
UserPropertyManager upm = ComponentAccessor.getUserPropertyManager()
PropertySet ps = upm.getPropertySet(u)
return ps.getAsActualType("jira.meta.[property name]")
```