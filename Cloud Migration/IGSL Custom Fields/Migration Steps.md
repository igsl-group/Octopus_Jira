# Migration for IGSL Custom Fields

## Scope
### Custom Approval
1. Custom Approval is replaced by plugin Approval Path.
1. Data migration to Approval Path is not possible. 
1. Existing Custom Approval data will be converted to text format and stored in a new custom field.

### Production/File Packing Note
1. Packing Note is replaced by Table Grid NG. 
1. Data will be migrated.

### Effort Table
1. Effort Table is replaced by Table Grid NG.
1. Data will be migrated.

### URL 
1. URL is replaced by paragraph custom field.
1. Data will be migrated.

### Static Image
1. Static Image is replaced by paragraph custom field.
1. Data will be migrated.

### Change Request
1. Change Request is replacecd by linked issue, a Jira system field.
1. Data will be migrated.

## Requirements
1. PowerShell v7+.
1. GenerateReport.ps1 requires network connection to Jira server.
1. MigrateCustomFieldData.ps1 requires network connections to both Jira Server and Jira cloud (but not at the same time, depends on the command).

## Migration Steps
### Export data from Jira Server
1. Run PowerShell. 
1. Choose to export via command or interface (more user-friendly).
1. Export results will be stored in a CSV file named \{Timestamp\}.csv.
#### Export via command
1. Login to Jira Server.
1. Gear | Issues | Custom fields.
1. In Type dropdown, type "IGSL".
1. Put a check to the types displayed.
1. For each custom field displayed, click ... in the Actions column.
1. Middle-click on "Edit details" to open a new tab.
1. The new tab's URL will be similar to this: 
    > https://localhost:8080/secure/admin/EditCustomField!default.jspa?id=10204
1. Note the number at the end. In the above example, it is 10204. 
1. The custom field's id is customfield_10204.
1. Note the custom field ids for all custom fields found. 
1. Clear the Types filter. In Search, type "Approval Data".
1. Note its custom field id. 
1. Run command: 
    >.\GenerateReport.ps1 -Query -ServerDomain https://\{Jira Server IP\}:\{Port\} -ServerUser \{Username\} -Jql \{JQL to select issues\} -Fields @(\{Comma-delimited field ids\})
1. For example: 
    >.\GenerateReport.ps1 -Query -ServerDomain https://localhost:8080 -ServerUser admin -Jql 'project is not null' -Fields @('customfield_10210', 'customfield_10212')
#### Export via interface
1. Run command:
    >.\GenerateReport.ps1
1. Enter M to toggle mode to Server/Data Center.
1. Enter D. Enter Jira Server's address.
1. Enter U. Enter username.
1. Enter P. Enter password.
1. Enter J. Enter JQL to select issues to export.
1. Enter F. This opens the interface to select custom fields.
    1. The top section, "Selected Fields", lists custom fields that will be exported. By default, summary is selected. Issue key is always included. 
    1. The middle section, "Avaialble Fields", allows you to select custom fields. 
1. Enter C to remove all selected fields.
1. Enter N. Enter "Approval Data". Enter S.
1. Enter 1 to select the field.
1. Enter N. Press Enter to clear the name filter.
1. Enter F. Enter "com.igsl". Enter S.
1. Enter the number of all located custom fields. 
1. Enter X to return to the main menu.
1. Enter S to execute the export. 
1. Enter X to exit.

### Data Translation
1. Two custom fields require additional processing. 
1. Requires network connection to Jira Server.
### Custom Approval
1. Run command:
    >.\MigrateCustomFieldData.ps1 -ApprovalPath_Translate -In
    \{CSV file from export\} -SourceField 'Approval Data' -DCDomain \{Jira server address\} -DCUser \{Username\}
1. A new CSV file will be created. Rename it to ApprovalData.csv.
1. This converts existing approval history data into readable format.
### Change Request
1. Run command:
    >.\MigrateCustomFieldData.ps1 -ChangeRequest_Translate -In
    \{CSV file from export\} -SourceField \{Custom field name\} -DCDomain \{Jira server address\} -DCUser \{Username\}
1. A new CSV file will be created. Rename it to ChangeRequest.csv.
1. This converts existing Change Request data into a format suitable for linked issue system field.

### Jira Cloud Configuration
### Plugin Installation
1. Install Table Grid NG: https://marketplace.atlassian.com/apps/1217571/table-grid-next-generation
1. Install Approval Path for Jira: https://marketplace.atlassian.com/apps/1220490/approval-path-for-jira
### Custom Approval
Create a new custom field to store legacy approval history for viewing.
1. Gear | Work items | Fields.
1. Click Create new field.
1. Choose "Paragraph" in Field type.
1. Enter "Legacy Approvals" in name.
1. Click Create.
1. Click ... in Actions column, click Contexts and default values.
1. Adjust context to associate with projects and issue types as needed.
1. Click ... in Actions column, click Add field to screen.
1. Add the field to appropriate screens.
1. Note the custom field id.
### Production/File Packing Note
1. Gear | Marketplace Apps | Table Grid.
1. Click Add Grid.
1. Select Grid Field and click Confirm.
1. Enter "Product/File Packing Note" in Name.
1. Enter a description if needed.
1. In Scopes, select the target project(s) and issue type(s).
1. In Configuration, delete the default columns by clicking on their X.
1. For each row, click Add new column, enter the following details and click Add: 
    | Column type | Identifier | Title | Default value | Cell value required | Summary label | Aggregation operation | 
    |-------------|------------|-------|---------------|---------------------|---------------|-----------------------|
    | String | Name | Name | -Blank- | Yes | -Blank- | -Blank- | 
    | String | Version | Version | -Blank- | No | -Blank- | -Blank- |
    | String | MD5Signature | MD5 Signature | -Blank- | No | -Blank- | -Blank- |
1. Click Save.
### Effort Table
1. Gear | Marketplace Apps | Table Grid.
1. Click Add Grid.
1. Select Grid Field and click Confirm.
1. Enter "Effort Table" in Name.
1. Enter a description if needed.
1. In Scopes, select the target project(s) and issue type(s).
1. In Configuration, delete the default columns by clicking on their X.
1. For each row, click Add new column, enter the following details and click Add: 
    | Column type | Identifier | Title | Default value | Cell value required | Summary label | Aggregation operation | 
    |-------------|------------|-------|---------------|---------------------|---------------|-----------------------|
    | String | Task | Task | -Blank- | Yes | -Blank- | -Blank- | 
    | Number | HeadCountDay | Head Count Day | 0 | Yes | Total Head Count Day | Sum |
    | Number | Expenses | Expenses | 0 | Yes | Total Expenses | Sum |
1. Click Save.
### URL 
Create a new custom field to store URLs.
1. Gear | Work items | Fields.
1. Click Create new field.
1. Choose "Paragraph" in Field type.
1. Enter name.
1. Click Create.
1. Click ... in Actions column, click Contexts and default values.
1. Adjust context to associate with projects and issue types as needed.
1. Click ... in Actions column, click Add field to screen.
1. Add the field to appropriate screens.
1. Note the custom field id.
### Static Image
Create a new custom field to store static images.
1. Gear | Work items | Fields.
1. Click Create new field.
1. Choose "Paragraph" in Field type.
1. Enter name.
1. Click Create.
1. Click ... in Actions column, click Contexts and default values.
1. Adjust context to associate with projects and issue types as needed.
1. Edit the default value to: 
    > !\{Image URL\}
    e.g. !https://mysite/myImage.jpg
1. You can control the image size by adding:
    > !\{Image URL\}|width=\{width\},height=\{height\}
    e.g. !https://mysite/myImage.jpg|width=200,height=80
1. Click ... in Actions column, click Add field to screen.
1. Add the field to appropriate screens.
1. Note the custom field id.
### Change Request
Define new linked issue relationship for Change Request. 
1. Gear | Work items | Work item linking.
1. Enter name, outward and inward link description.
1. Click Add.

## Data Migration
1. Import data from exported CSV file into Jira cloud.
1. Requires network connection to Jira cloud.
### Custom Approval
1. Run command:
    > .\MigrateCustomFieldData.ps1 -ApprovalPath -In 'ApprovalPath.csv'
    -JiraDomain 'xxxx.atlassian.net' -JiraEmail \{Email\} -SourceField 'Approval Data' -TargetField \{Custom field id\}
### Production/File Packing Note
1. Run command:
    > .\MigrateCustomFieldData.ps1 -PackingNote -In \{CSV file\}
    -JiraDomain 'xxxx.atlassian.net' -JiraEmail \{Email\} -SourceField \{CSV column name\} -TargetField \{Custom field id\}
### Effort Table
1. Run command:
    > .\MigrateCustomFieldData.ps1 -EffortTable -In \{CSV file\}
    -JiraDomain 'xxxx.atlassian.net' -JiraEmail \{Email\} -SourceField \{CSV column name\} -TargetField \{Custom field id\}
### URL 
1. Run command: 
    > .\MigrateCustomFieldData.ps1 -URL -In \{CSV file\} -JiraDomain 'xxxx.atlassian.net' -JiraEmail \{Email\} -SourceField \{CSV column name\} -TargetField \{Custom field id\}
### Static Image
The default value in the new custom field will only cover new work items, so we need to update existing work items as well.
1. Run command:
    > .\MigrateCustomFieldData.ps1 -StaticImage -JiraDomain 'xxxx.atlassian.net' -JiraEmail \{Email\} -TargetField \{Custom field id\} -ImageURL \{Image URL\} [-ImageWidth \{#\}]
    [-ImageHeight \{#\}] -JQL \{JQL to select work items to update\}
1. If ImageWidth and ImageHeight are not provided, the original image size will be used.
### Change Request
1. Run command:
    > .\MigrateCustomFieldData.ps1 -ChangeRequest -In \{CSV file\}
    -JiraDomain 'xxx.atlassian.net' -JiraEmail \{Email\} -SourceField \{CSV column\} -IssueLinkName \{Name of issue link type\}
    -IssueLinkDirection \{Outward or Inward\}