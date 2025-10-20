# Approval
1. Condition is used to restrict when an approval can be started manually.
1. Start approval automatically using Approval Path's workflow post-function.
1. Block approve transition with workflow rule, Approval Path's block until approval.
1. Reject transition must be hidden using role restriction for "atlassian-addon-project-access". 
1. Can define "automation" to transit/update issue after approve/reject. This is not Jira's automation, just the same name.
1. When you loop back issue status for a new round of approval, you must add post-function to archive or delete all approvals, or the logic will include the previous approval result.
1. Users can manually start approval. Can turn this off globally in settings. 
1. Can define no. of votes to approve and reject separately.
1. Can allow user to abstain.
1. Cannot change decision.
1. Can require comments for decisions. Can configure separately for approve/reject.
1. Has delegations, can define singular delegator and list of delegatees. Can add groups. Can restrict projects. 
1. Normal users can create/edit delegations where they are the delegator. 
1. Normal users can view others' delegations if they are delegatees.
1. Delegations has time range. Can has timeless delegations.
1. Approval definition is locked in existing issues. Updating definition will not affect existing issues. 
1. When approval only requires 1 vote, abstain is counted as approve... so better to not allow abstain for such approval definitions.

# JSM
1. JSM is supported. 
1. In email, can configure to generate links to both/Jira/JSM. 

# Email notification
1. Issue reporter/watcher receives email about an approval being started.
1. Approver receives email about consent requested.
1. Issue reporter/watcher receives email about approval result.
1. Can customize email template.
1. Can allow users to trigger notification email to approvers. 
1. Has automatic reminders. Time period configurable.
1. Can enable approve/reject links in email.

# Logging
1. Comments are logged by Approval Path in issues when approved/rejected. 
1. Nothing in History unless there are status/field updates.
1. Has its own Approval Path category for details.
1. Apps page contains Activity for logs.

# Pros
1. Covers almost all features provided by CustomApproval.

# Cons
1. Low no. of installs (294).
1. Users cannot change decision.
1. "Automation" or webhook after approve/reject has no logs, can fail but Approval Path will still consider the approval done.
1. Webhook authentication headers seem to be not provided automatically. Which means you have to hardcode the credentials/API key.
1. Data migration from CustomApproval is not possible. 
1. Approval Path controls will be available in all projects (no action can be done there without approvals defined).

# Data migration
1. To save effort, we can propose migrating CustomApproval data by:
    1. Exporting issues with CustomApproval. 
    1. Convert the JSON to a markdown table.
    1. Import the text to Jira Cloud as a new rich text custom field.

# Bugs Found
1. The Approval Path panels don't refresh properly if status loops back for a new approval. Have to manually refresh page.
    https://warsaw-dynamics.atlassian.net/servicedesk/customer/portal/1/SUP-2626
1. Right Approval Path panel sometimes do not render correctly.
    https://warsaw-dynamics.atlassian.net/servicedesk/customer/portal/1/SUP-2623
1. Call for Action email template cannot be rendered (invalid issueLink variable).
    https://warsaw-dynamics.atlassian.net/servicedesk/customer/portal/1/SUP-2628