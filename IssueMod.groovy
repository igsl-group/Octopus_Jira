import com.atlassian.jira.user.ApplicationUser
import com.onresolve.scriptrunner.parameters.annotation.Checkbox
import java.lang.Boolean
import com.atlassian.jira.issue.fields.rest.json.JsonType
import com.onresolve.scriptrunner.parameters.annotation.FieldPicker
import com.atlassian.jira.issue.fields.Field
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.scriptrunner.parameters.annotation.Select
import com.onresolve.scriptrunner.parameters.annotation.meta.Option
import com.onresolve.scriptrunner.parameters.annotation.ShortTextInput
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import com.onresolve.scriptrunner.runner.customisers.PluginModule
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean 
import org.apache.log4j.Logger
import org.apache.log4j.Level
import com.atlassian.jira.issue.customfields.option.Options 
import java.sql.Timestamp
import java.sql.Date
import java.text.ParseException
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.workflow.JiraWorkflow
import com.atlassian.jira.issue.status.Status
import com.atlassian.jira.issue.IssueInputParametersImpl
import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.issue.priority.Priority

@WithPlugin(['com.atlassian.servicedesk', 'com.riadalabs.jira.plugins.insight'])

@ShortTextInput(description = 'Issue key (e.g. "TICKET-123")', label = 'Issue Key')
String paramIssueKey

@FieldPicker(description = 'Select field to modify', label = 'Field', multiple = false)
Field paramFieldName

@Select(description = '^ If <b><u>Clear Field Value = Yes</u></b> , clear target field value (set as <b><u>null</u></b>)<br>^ If <b><u>Clear Field Value = No</u></b>  , update target field value with <b><u>Field Value</u></b>', label = 'Clear Field Value?', options = [
    @Option(label = 'No', value = 'No'),
    @Option(label = 'Yes', value = 'Yes')
], multiple = false)
String paramClearField

@ShortTextInput(description = '^ This value only used when <b><u>Clear Field Value = No</u></b>', label = 'Field Value')
String paramFieldValue

@Select(description = 'For Insight fields', label = 'Insight Schema', options = [
    @Option(label = 'CI Schema', value = '7'),
    @Option(label = 'Department', value = '5'),
    @Option(label = 'JSM Custom Config', value = '6')
], multiple = false)
String paramInsightSchema

@Select(description = '', label = 'Log Level', options = [
    @Option(label = 'Info', value = 'Info'),
    @Option(label = 'All', value = 'All'),
    @Option(label = 'Trace', value = 'Trace'),
    @Option(label = 'Debug', value = 'Debug'),
    @Option(label = 'Warn', value = 'Warn'),
    @Option(label = 'Error', value = 'Error'),
    @Option(label = 'Fatal', value = 'Fatal'),
    @Option(label = 'Off', value = 'Off')
], multiple = false)
String paramLogLevel

public Logger getLogger() {
    return Logger.getLogger('IssueModifier')
}

public JsonType getFieldType(Field f) {
    final Logger LOG = getLogger()
    if (f != null) {
        JsonType jsonType = (JsonType) f.getProperties().get('jsonSchema')
        LOG.debug('JSONSchema: [' + jsonType + ']')
        LOG.debug('getType: [' + jsonType.getType() + ']')
        LOG.debug('getSystem: [' + jsonType.getSystem() + ']')
        LOG.debug('getItems: [' + jsonType.getItems() + ']')
        LOG.debug('getCustom: [' + jsonType.getCustom() + ']')
        LOG.debug('getCustomId: [' + jsonType.getCustomId() + ']')
        LOG.debug('getMetaClass: [' + jsonType.getMetaClass() + ']')
        LOG.debug('getMetaPropertyValues: [' + jsonType.getMetaPropertyValues() + ']')
        LOG.debug('getProperties: [' + jsonType.getProperties() + ']')
        return jsonType
    }
    return null
}

public void setFieldValue(MutableIssue issue, Field field, Boolean clearValue, String value, int schema) {
    final String INSIGHT_CUSTOM = 'com.riadalabs.jira.plugins.insight:rlabs-customfield-default-object'
    @PluginModule IQLFacade iqlFacade
    @PluginModule ObjectFacade objectFacade
    final Logger LOG = getLogger()
    Object newValue = null
    JsonType jsonType = getFieldType(field)
    String jt = jsonType.getType()
    if (!clearValue) {
        switch (jt) {
            case JsonType.ANY_TYPE:
                // Check custom
                if (INSIGHT_CUSTOM.equals(jsonType.getCustom())) {
                    String iql = 'Name = "' + value + '"'                                               
                    List<ObjectBean> insightObjects = iqlFacade.findObjectsByIQLAndSchema(schema, iql)
                    LOG.debug('Insight objects found for [' + value + ']: ' + insightObjects)
                    if (insightObjects.size() == 1) {
                        List<ObjectBean> list = new ArrayList<ObjectBean>()
                        list.add(objectFacade.loadObjectBean(insightObjects.get(0).getId()))
                        newValue = list
                    } else {
                        throw new Exception('Value "' + value + '" is not valid Insight object name. Is the correct schema selected?')
                    }
                } else {
                    throw new Exception('Data type: ' + jt + ' with custom ' + jsonType.getCustom() + ' is not supported')
                }
                break
            case JsonType.ARRAY_TYPE:
                // TODO Item type? 
                break
            case JsonType.BOOLEAN_TYPE:
                newValue = Boolean.parseBoolean(value)
                break
            case JsonType.DATETIME_TYPE:
                try {
                    newValue = new java.util.Date().parse('yyyyMMdd HHmmss', value).toTimestamp()
                } catch (ParseException pex) {
                    throw new Exception('Value "' + value + '" does not conform to datetime format "                                                                yyyyMMdd HHmmss"', pex)    
                }
                break
            case JsonType.DATE_TYPE:
                try {
                    newValue = new java.util.Date().parse('yyyyMMdd', value).toTimestamp()
                } catch (ParseException pex) {
                    throw new Exception('Value "' + value + '" does not conform to date format "yyyyMMdd"', pex)  
                }
                break
            case JsonType.STRING_TYPE:
                newValue = value
                break
            case JsonType.NUMBER_TYPE:
                try {
                    newValue = Float.parseFloat(value)
                } catch (NumberFormatException nfex) {
                    throw new Exception('Value "' + value + '" is not a valid number', nfex)  
                }
                break
            case JsonType.USER_TYPE:
                try {
                    newValue = ComponentAccessor.getUserManager().getUserByName(value)
                } catch (Exception ex) {
                    throw new Exception('Value "' + value + '" is not a valid user name', ex)
                }
                break
            case JsonType.GROUP_TYPE: {
                    Object grp = ComponentAccessor.getGroupManager().getGroup(value)
                    if (grp == null) {
                        throw new Exception('Value "' + value + '" is not a valid group name')
                    }
                    newValue = grp
                }
                break
            case JsonType.OPTION_TYPE: {
                    boolean optionFound = false
                    CustomField cf = (CustomField) field
                    Options optionList = ComponentAccessor.getOptionsManager().getOptions(cf.getRelevantConfig(issue))
                    for (com.atlassian.jira.issue.customfields.option.Option option : optionList) {
                        if (option.getValue().equals(value)) {
                            newValue = option
                            optionFound = true
                            break
                        }
                    }
                    if (!optionFound) {
                        throw new Exception('Value "' + value + '" is not a valid option for field "' + field.getName() + '"')
                    }
                }
                break
            case JsonType.PRIORITY_TYPE: {
                    boolean priorityFound = false
                    for (Priority p : ComponentAccessor.getConstantsManager().getPriorities()) {
                        if (p.getName().equals(value)) {
                            newValue = p
                            priorityFound = true
                            break
                        }
                    }
                    if (!priorityFound) {
                        throw new Exception('Value "' + value + '" is not a valid priority')
                    }
                }
                break
            // Unsupported types, use UI
            case JsonType.OPTION_WITH_CHILD_TYPE: 
            case JsonType.PROJECT_TYPE:
            case JsonType.RESOLUTION_TYPE:
            case JsonType.STATUS_TYPE:
                // Need to read workflow to find suitable action ID for given status...
                // For now, do it via UI
                // try {
                //     JiraWorkflow wf = ComponentAccessor.getWorkflowManager().getWorkflow(issue)
                //     List<Status> statusList = wf.getLinkedStatusObjects()
                //     boolean statusFound = false
                //     for (Status s : statusList) {
                //         LOG.debug('[' + value + '] vs [' + s.getName() + ']')
                //         s.
                //         if (s.getName().equals(value)) {
                //             newValue = s
                //             statusFound = true
                //             break
                //         }
                //     }
                //     if (!statusFound) {
                //         throw new Exception('Value "' + value + '" is not a valid status')
                //     }
                // } catch (Exception ex) {
                //     throw new Exception('Value "' + value + '" is not a valid status', ex)
                // }
                // break
            case JsonType.ATTACHMENT_TYPE:
            case JsonType.COMMENT_TYPE:
            case JsonType.COMMENTS_PAGE_TYPE:
            case JsonType.COMPONENT_TYPE:
            case JsonType.ISSUELINKS_TYPE:
            case JsonType.ISSUETYPE_TYPE:
            case JsonType.PROGRESS_TYPE:
            case JsonType.SECURITY_LEVEL_TYPE:
            case JsonType.TIME_TRACKING_TYPE:
            case JsonType.VERSION_TYPE:
            case JsonType.VOTES_TYPE:
            case JsonType.WATCHES_TYPE:
            case JsonType.WORKLOG_TYPE:
                throw new Exception('Data type: ' + jt + ' is not supported')
        }
    }
    // Set field value
    Object originalValue = null
    if (field instanceof CustomField) {
        CustomField cf = (CustomField) field
        originalValue = issue.getCustomFieldValue(cf)
        issue.setCustomFieldValue(cf, newValue)
    } else {
        // TODO Need to call specific method based on field name
        switch (field.getName()) {
            case 'Assignee': 
                originalValue = issue.getAssignee()
                issue.setAssignee((ApplicationUser) newValue)
                break
            case 'Created': 
                originalValue = issue.getCreated()
                issue.setCreated((Timestamp) newValue)
                break
            case 'Description': 
                originalValue = issue.getDescription()
                issue.setDescription((String) newValue)
                break
            case 'Due Date': 
                originalValue = issue.getDueDate()
                issue.setDueDate((Timestamp) newValue)
                break
            case 'Reporter': 
                originalValue = issue.getReporter()
                issue.setReporter((ApplicationUser) newValue)
                break
            // case 'Status': 
            //     originalValue = issue.getStatus()
            //     issue.setStatus((Status) newValue)
            //     break
            case 'Summary':
                originalValue = issue.getSummary()
                issue.setSummary((String) newValue)
                break
            case 'Priority': 
                originalValue = issue.getPriority()
                issue.setPriority((Priority) newValue)
                break
            default: 
                throw new Exception('Field ' + field.getName() + ' is not supported')
        }
    }
    ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
    // if (field.getName() == 'Status') {
    //     Long issueId = issue.getId()
    //     int actionId = Integer.parseInt(((Status) newValue).getId())
    //     IssueService.TransitionValidationResult vr = ComponentAccessor.getIssueService().validateTransition(user, issueId, actionId, new IssueInputParametersImpl())
    //     if (vr.isValid()) {
    //         ComponentAccessor.getIssueService().transition(user, vr)
    //     } else {
    //         throw new Exception('Status "' + newValue + '" violates workflow')
    //     }
    // } else {
        ComponentAccessor.getIssueManager().updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, false)
    // }
    LOG.info('Issue [' + issue + '] updated, field [' + field.getName() + '] from [' + originalValue + '] to [' + newValue + ']')
}

public String modifyIssue(String logLevel, String issueKey, Field field, String clear, String value, int schema) {
    final Logger LOG = getLogger()
    try {
        LOG.setLevel(Level.toLevel(logLevel))
        MutableIssue issue = (MutableIssue) ComponentAccessor.getIssueManager().getIssueObject(issueKey)
        if (issue == null) {
            return 'Issue [' + issueKey + '] cannot be found'
        } 
        if (field == null) {
            return 'Please select a field'
        }

        // ClearField Handling
        def BoolClear = false
        if (clear == 'Yes'){
            BoolClear = true
        }
        setFieldValue(issue, field, BoolClear, value, schema)
        return 'Success'
    } catch (Exception ex) {
        LOG.error('Failed to update issue: ' + ex.getClass() + ': ' + ex.getMessage())
        return 'Error'
    }
}

return modifyIssue(paramLogLevel, paramIssueKey, paramFieldName, paramClearField, paramFieldValue, Integer.parseInt(paramInsightSchema))