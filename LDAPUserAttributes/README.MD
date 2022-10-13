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
return ps.getAsActualType("jira.meta.email")