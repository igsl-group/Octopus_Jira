# Jira Custom Field Types for Octopus

## List of Custom Field Types
1. Change Request
    * Data table with the following columns: 
        * Issue selector (all issues)
        * Project manager (read-only)
    * Default value can be set via custom field default value.
1. Effort Table
    * Data table with the following columns: 
        * Task (free text)
        * Head count day (float)
    * Additionally, the following fields: 
        * Expenses (currency)
        * Total head count day (read-only, auto-calculated from data table)
    * This control is made to be compatible with Jira Service Management but is read-only there.
    * Default value can be set via custom field default value.
1. Product Packing Note
    * Data table with the following columns: 
        * Name (free text)
        * Version (free text)
        * MD5 signature (free text)
    * Default value can be set via custom field default value.
1. Static Image
    * A read only field displayed as an image.
    * The value is set via custom field default value.
    * This control is made to be compatible with Jira Service Management. 
1. URL Field
    * Data table to display URLs as hyperlinks.
    * Default value can be set via custom field default value.

## Implementation Notes

### Complex Custom Field
1. Always name the HTML and JavaScript elements with the custom field's ID. This is to support multiple instances of the custom field.
1. You can name additional form fields with name attribute = "${customField.id}:[Key]". Their values will be supplied as CustomFieldParams parameter in .validateFromParams(). 

### Inline Edit Mode
1. Jira's inline edit mode verifies that the edit template contains a non-hidden form field with an id attribute equals to the custom field ID.
1. If such a field is not found, Jira concludes the field isn't editable and will not display the inline edit button for the field.
1. To fool Jira into enabling inline edit mode, a dummy field is added with style "display: none" to hide it from view.

### ScriptRunner Read-Only Behavior Compatibility
1. ScriptRunner provides the feature called "Behavior" to customize fields. Unfortunately Jira provides no API access, so ScriptRunner does it by adding JavaScript.
1. To support the read-only behavior, JavaScript mutation observer is setup to monitor the control's classes. If ScriptRunner's read-only class "clientreadonly" is applied, change the controls to read-only. 
1. Effort Table is the only control with this logic applied. 

### Custom Approval

#### Features
1. Workflow approval without Jira Service Management.
	* Custom field "Approval Data" is used to store approval settings and approval history.
	* A post-function is provided to initialize Approval Data custom field.
	* Adds approve/reject buttons to operations bar in issue view.
	* Adds approval panel to issue view to display approval history.
	* Approver list is taken from user or group picker custom fields.
	* Supports multiple approval steps in a single workflow. 
	* A scheduled job to scan for issues matching approval condition (for cases caused by group membership change).
	* Configuration page to configure:
		* Scheduled job JQL (to filter out issues and improve performance).
		* Frequency of scheduled job.
		* Group that can manage delegation settings for all users.
		* Delegation settings retention period.  
1. Supports approval delegation.
	* Users can set delegation via menu in user profile.
	* Administrators can set delegation for everyone.
	* Can set delegation that never expires.
	* Can set multiple delegations.

### Unimplemented Features
1. Email notification. 
1. Approval via email.
1. Multiple delegates should not count as multiple approvers. 
1. Service Management support.

### Usage
1. Create workflow normally, creating statuses for approval steps. 
1. Add post function "Initialize custom approval" to workflow, in a transition before any approvals, e.g. Create.
1. Configure post function by adding one or more approvals, each containing the following settings: 
	* Approval name - This is displayed on screen in Approval panel in issue view.
	* Approver - Select a user or group picker custom field. 
	* Starting status - The issue status where this approval starts.
	* Approved status - Transit issue to this status when approved.
	* Rejected status - Transit issue to this status when rejected.
	* Approve count - The no. of approvers required to approve. 
	* Reject count - The no. of approvers required to reject.
	* Allow change decision - Allow approvers to change their decisions.
1. You can set approve/reject count to 0 to indicate all approvers are required. 
1. You can set approve/reject count to a fraction to indicate a percentage of approver list.
1. If you set a number larger than approver list, it will be capped to size of approver list.
1. Change decision is impossible if approve/reject count is 1, as the approval will be completed immediately. 
1. If you change group membership, the approver list will be immediately updated: 
	* Removed approvers (and their delegates) will be crossed-out in history and will not count towards the decision. 
	* Scheduled job (default every 5 minutes) is used to scan for issues that should be approved/rejected in this fashion.
1. You need to set "hide from user" condition for the approved/rejected statuses. Otherwise users will be able to transit the issue bypassing approval.