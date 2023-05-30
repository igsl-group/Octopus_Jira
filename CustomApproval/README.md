### Custom Approval

#### Features
1. Workflow approval without requiring Jira Service Management.
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
	* Delegated user will approve on behalf of delegator. 
	* If a delegated user has multiple delegators, the delegated user's decision will be applied for all delegators. 
	* If change decision is allowed, delegator can overwrite delegated user's decision and vice versa. 

### Supporting Jira Service Management
1. Jira Service Management has Customer Portal(s) which are separate from Jira's interface. 
1. Jira does not provide any plugin modules for customizing Customer Portal interface, so JavaScript is used to create the approval panel and approve/reject buttons. 
1. Issue access rights in Customer Portal is determined by a custom field "Request Participants". Delegated approvers are automatically added to Request Participants so they can access the issue.
1. An additional custom field "Manual Request Participant" is used to keep track of changes made to Request Participants by user action.

### Scheduled Tasks
This plugin creates background tasks to support its operations: 
1. Run-once tasks for delegation settings becoming effective or expired.
	* This will search for open issues with Custom Approval enabled, and recalculate approver list and update Request Participants. 
1. Run-once tasks for group membership changed events.
	* This will search for open issues with Custom Approval enabled, and recalculate approver list and update Request Participants. 
1. Run-once tasks for issue updated events.
	* This will check the issue if approver list custom fields have been changed, and recalculate approver list and update Request Participants. 
1. A repeating task for checking approval status.
	* Because approver list can change due to change in group membership, resulting in an issue reaching the approve/reject count without an approve/reject action.
	* This background task will regularly scan issues and transit them if needed.
	* Frequency can be configured.

### Java API
1. To retrieve approver list (not including delegated approvers): 
	* Map<String, ApplicationUser> com.igsl.customapproval.CustomApprovalUtil.getApproverList(Issue issue).
	* Key of the map is user key.
	* Value of the map is ApplicationUser object.
1. To retrieve delegated approver list: 
	* Map<String, ApplicationUser> com.igsl.customapproval.CustomApprovalUtil.getDelegates(Map<String, ApplicationUser> approverList).
	* Parameter approverList is the return value of getApproverList().
	* Key of the map is user key.
	* Value of the map is ApplicationUser object.
1. To check if user is delegation target of delegatingUser on specified approvalDate: 
	* boolean com.igsl.customapproval.delegation.DelegationUtil.isDelegate(String user, String delegatingUser, Date approvalDate)
	* boolean com.igsl.customapproval.delegation.DelegationUtil.isDelegate(ApplicationUser user, ApplicationUser delegatingUser, Date approvalDate)

### REST API
1. To approve an issue: 
	* Endpoint: /rest/igsl/latest/customApprove 
	* Request Data: Issue key, e.g. "PROJ-1"
	* Response Data: None
	
1. To reject an issue: 
	* Endpoint: /rest/igsl/latest/customReject 
	* Request Data: Issue key, e.g. "PROJ-1"
	* Response Data: None
	
### Unimplemented Features
1. Email notification. 
1. Approval via email.

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
