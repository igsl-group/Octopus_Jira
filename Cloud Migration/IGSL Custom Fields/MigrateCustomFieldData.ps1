<#
.SYNOPSIS
Read data of CustomFieldTypes plugin from CSV and import them to Jira Cloud.

.DESCRIPTION
Export CSV from Jira DC using GenerateReport.ps1 to use in this script.

.PARAMETER ApprovalPath_Translate
Translate exported data for Approval Path.

.PARAMETER ApprovalPath
Import translated data for Approval Path. 
The approval history will be converted to a readable format.
A Paragraph field should be prepared in Jira Cloud.

.PARAMETER PackingNote
Import data for Product/File Packing Note custom fields.
A table grid should be defined in Table Grid NG in Jira Cloud.

.PARAMETER EffortTable
Import data for Effort Table custom fields.
A table grid should be defined in Table Grid NG in Jira Cloud.

.PARAMETER URL
Import data for URL custom fields.
A Paragraph custom field should be prepared in Jira Cloud.

.PARAMETER StaticImage
Import data for Static Image custom fields. 
A Paragraph custom field should be prepared in Jira Cloud. 
You can optionally make this field read-only with ScriptRunner Behaviour.

.PARAMETER ChangeRequest_Translate
Translate exported data for Change Request custom fields, converting from issue id to issue key.

.PARAMETER ChangeRequest
Import data for Change Request fields. 
The data will be imported as Linked Work Items in Jira Cloud.

.PARAMETER In
Input CSV path.

.PARAMETER JiraDomain
Jira Cloud domain, e.g. kcwong.atlassian.net

.PARAMETER JiraEmail
Jira Cloud administrator user email.

.PARAMETER JiraToken
Jira Cloud API token. You will be prompted if not provided.

.PARAMETER TableGridDomain
Table Grid NG domain. Defaults to databuckets.net

.PARAMETER TableGridToken
Table Grid NG API token. You will be prompted if not provided.

.PARAMETER SourceField
Column name in input CSV.

.PARAMETER TargetField
Destination table grid name or custom field id.

.PARAMETER ImageURL
Static image URL. This value will be updated to all issues found by ImageJQL.

.PARAMETER ImageWidth
Image width in pixels. Default is 0 for auto.

.PARAMETER ImageHeight
Image height in pixels. Default is 0 for auto.

.PARAMETER JQL
JQL to locate issues with Static Image field.

.PARAMETER DCProtocol
Jira Data Center protocol, defaults to https.

.PARAMETER DCDomain
Jira Data Center domain, e.g. localhost:8080

.PARAMETER DCUser
Jira Data Center administrator user name.

.PARAMETER DCPass
Jira Data Center passowrd. You will be prompted if not provided.

.PARAMETER IssueLinkName
Issue link name for Change Request.

.PARAMETER IssueLinkDirection
Issue link direction.
Outward means the primary issue is the subject, inward means the primary issue is the object.

.PARAMETER OutDir
Output directory, defaults to current directory.

.EXAMPLE
	# Export data from Jira DC
	.\GenerateReport.ps1 
		-Query
		-ServerDomain <Jira DC domain, e.g. localhost:8080>
		-ServerUser <Jira DC admin user name>
		[-ServerPass <Jira DC password, prompt if not provided>]
		-Jql <JQL to search for issues>
		-Fields @(<comma-delimited list of custom field ids>)

	Commandline to execute search and generate CSV. 

	# Export data from Jira DC
	.\GenerateReport.ps1 

	Interactive mode.

.EXAMPLE
	# Product/File Packing Note
	.\MigrateCustomFieldData 
		-PackingNote 
		-In <CSV input file> 
		-SourceField <Column name in CSV>
		-JiraDomain <Jira domain, e.g. kcwong.atlassian.net>
		-JiraEmail <Admin user email address>
		[-JiraToken <API token, prompt if not provided>]
		[-TableGridDomain <Table Grid NG domain, defaults to databuckets.net>]
		[-TableGridToken <Table Grid NG API token, prompt if not provided>]
		-TargetField <destination table grid name in Jira Cloud>
		[-OutDir <Output directory, defaults to current directory>]

	Imports Product/File Packing Note custom field in Jira DC into Table Grid NG in Jira Cloud.

.EXAMPLE
	# Effort Table
	.\MigrateCustomFieldData 
		-EffortTable 
		-In <CSV input file> 
		-SourceField <Column name in CSV>
		-JiraDomain <Jira domain, e.g. kcwong.atlassian.net>
		-JiraEmail <Admin user email address>
		[-JiraToken <API token, prompt if not provided>]
		[-TableGridDomain <Table Grid NG domain, defaults to databuckets.net>]
		[-TableGridToken <Table Grid NG API token, prompt if not provided>]
		-TargetField <destination table grid name in Jira Cloud>
		[-OutDir <Output directory, defaults to current directory>]

	Imports Effort Table custom field in Jira DC into Table Grid NG in Jira Cloud.

.EXAMPLE
	# URL
	.\MigrateCustomFieldData 
		-EffortTable 
		-In <CSV input file> 
		-SourceField <Column name in CSV>
		-JiraDomain <Jira domain, e.g. kcwong.atlassian.net>
		-JiraEmail <Admin user email address>
		[-JiraToken <API token, prompt if not provided>]
		[-TableGridDomain <Table Grid NG domain, defaults to databuckets.net>]
		[-TableGridToken <Table Grid NG API token, prompt if not provided>]
		-TargetField <destination paragraph field in Jira Cloud>
		[-OutDir <Output directory, defaults to current directory>]

	Imports URL custom field in Jira DC into a Paragraph field in Jira Cloud.

.EXAMPLE
	# Static Image
	.\MigrateCustomFieldData 
		-StaticImage 
		-JiraDomain <Jira domain, e.g. kcwong.atlassian.net>
		-JiraEmail <Admin user email address>
		[-JiraToken <API token, prompt if not provided>]
		-TargetField <destination paragraph field in Jira Cloud>
		-ImageURL <URL of static image to be displayed>
		[-ImageWidth <Width of image in pixels, skip both ImageWidth and ImageHeight for auto size>]
		[-ImageHeight <Height of image in pixels, skip both ImageWidth and ImageHeight for auto size>]
		-JQL <JQL to search for issues to update>

	Imports Static Image custom field in Jira DC into a Paragraph field in Jira Cloud.

.EXAMPLE
	# Translate Change Request
	.\MigrateCustomFieldData 
		-ChangeRequest_Translate
		-In <CSV input file> 
		-SourceField <Column name in CSV>
		[-DCProtocol <http|https, defaults to https>]
		-DCDomain <DC domain, e.g. localhost:8080>
		-DCUser <DC admin user name>
		[-DCPass <DC passord, prompt if not provided>]

	Translates Change Request field in Jira DC from issue id into issue key. 

.EXAMPLE
	# Change Request
	.\MigrateCustomFieldData 
		-ChangeRequest
		-In <CSV input file> 
		-SourceField <Column name in CSV>
		-JiraDomain <Jira domain, e.g. kcwong.atlassian.net>
		-JiraEmail <Admin user email address>
		[-JiraToken <API token, prompt if not provided>]
		-IssueLinkName <Name of issue link>
		-IssueLinkDirection <Outward|Inward>
	
	Imports translated Change Request data into Linked Issues field in Jira Cloud.
#>
Param(
	[Parameter(Mandatory, ParameterSetName='ApprovalPath_Translate')]
	[switch] $ApprovalPath_Translate,
	[Parameter(Mandatory, ParameterSetName='ApprovalPath')]
	[switch] $ApprovalPath,
	[Parameter(Mandatory, ParameterSetName='PackingNote')]
	[switch] $PackingNote,
	[Parameter(Mandatory, ParameterSetName='EffortTable')]
	[switch] $EffortTable,
	[Parameter(Mandatory, ParameterSetName='URL')]
	[switch] $URL,
	[Parameter(Mandatory, ParameterSetName='StaticImage')]
	[switch] $StaticImage,
	[Parameter(Mandatory, ParameterSetName='ChangeRequest_Translate')]
	[switch] $ChangeRequest_Translate,
	[Parameter(Mandatory, ParameterSetName='ChangeRequest')]
	[switch] $ChangeRequest,

	[Parameter(Mandatory, ParameterSetName='ApprovalPath_Translate')]
	[Parameter(Mandatory, ParameterSetName='ApprovalPath')]
	[Parameter(Mandatory, ParameterSetName='PackingNote')]
	[Parameter(Mandatory, ParameterSetName='EffortTable')]
	[Parameter(Mandatory, ParameterSetName='URL')]
	[Parameter(Mandatory, ParameterSetName='ChangeRequest_Translate')]
	[Parameter(Mandatory, ParameterSetName='ChangeRequest')]
	[string] $In,

	[Parameter(Mandatory, ParameterSetName='ApprovalPath')]
	[Parameter(Mandatory, ParameterSetName='PackingNote')]
	[Parameter(Mandatory, ParameterSetName='EffortTable')]
	[Parameter(Mandatory, ParameterSetName='URL')]
	[Parameter(Mandatory, ParameterSetName='StaticImage')]
	[Parameter(Mandatory, ParameterSetName='ChangeRequest')]
	[string] $JiraDomain,

	[Parameter(Mandatory, ParameterSetName='ApprovalPath')]
	[Parameter(Mandatory, ParameterSetName='PackingNote')]
	[Parameter(Mandatory, ParameterSetName='EffortTable')]	
	[Parameter(Mandatory, ParameterSetName='URL')]
	[Parameter(Mandatory, ParameterSetName='StaticImage')]
	[Parameter(Mandatory, ParameterSetName='ChangeRequest')]
	[string] $JiraEmail,

	[Parameter(ParameterSetName='ApprovalPath')]
	[Parameter(ParameterSetName='PackingNote')]
	[Parameter(ParameterSetName='EffortTable')]
	[Parameter(ParameterSetName='URL')]
	[Parameter(ParameterSetName='StaticImage')]
	[Parameter(ParameterSetName='ChangeRequest')]
	[string] $JiraToken,

	[Parameter(ParameterSetName='PackingNote')]
	[Parameter(ParameterSetName='EffortTable')]
	[string] $TableGridDomain = 'databuckets.net',

	[Parameter(ParameterSetName='PackingNote')]
	[Parameter(ParameterSetName='EffortTable')]
	[string] $TableGridToken,

	[Parameter(Mandatory, ParameterSetName='ApprovalPath_Translate')]
	[Parameter(Mandatory, ParameterSetName='ApprovalPath')]
	[Parameter(Mandatory, ParameterSetName='PackingNote')]
	[Parameter(Mandatory, ParameterSetName='EffortTable')]
	[Parameter(Mandatory, ParameterSetName='URL')]
	[Parameter(Mandatory, ParameterSetName='ChangeRequest_Translate')]
	[Parameter(Mandatory, ParameterSetName='ChangeRequest')]
	[string] $SourceField,

	[Parameter(Mandatory, ParameterSetName='ApprovalPath')]
	[Parameter(Mandatory, ParameterSetName='PackingNote')]
	[Parameter(Mandatory, ParameterSetName='EffortTable')]
	[Parameter(Mandatory, ParameterSetName='URL')]
	[Parameter(Mandatory, ParameterSetName='StaticImage')]
	[string] $TargetField,

	[Parameter(Mandatory, ParameterSetName='StaticImage')]
	[string] $ImageURL,

	[Parameter(ParameterSetName='StaticImage')]
	[int] $ImageWidth = 0,

	[Parameter(ParameterSetName='StaticImage')]
	[int] $ImageHeight = 0,

	[Parameter(Mandatory, ParameterSetName='StaticImage')]
	[string] $JQL,

	[Parameter(Mandatory, ParameterSetName='ApprovalPath_Translate')]
	[Parameter(ParameterSetName='ChangeRequest_Translate')]
	[string] $DCProtocol = 'https',

	[Parameter(Mandatory, ParameterSetName='ApprovalPath_Translate')]
	[Parameter(Mandatory, ParameterSetName='ChangeRequest_Translate')]
	[string] $DCDomain,

	[Parameter(Mandatory, ParameterSetName='ApprovalPath_Translate')]
	[Parameter(Mandatory, ParameterSetName='ChangeRequest_Translate')]
	[string] $DCUser,

	[Parameter(Mandatory, ParameterSetName='ApprovalPath_Translate')]
	[Parameter(ParameterSetName='ChangeRequest_Translate')]
	[string] $DCPass,

	[Parameter(Mandatory, ParameterSetName='ChangeRequest')]
	[ArgumentCompleter({
		param (
			$commandName,
            $parameterName,
            $wordToComplete,
            $commandAst,
            $fakeBoundParameters
		)
		$possibleValues = @(
			'Blocks', 
			'Cloners', 
			'Duplicate',
			'Polaris work item link',
			'Post-Incident Reviews',
			'Problem/Incident',
			'Relates')
		$possibleValues | Where-Object {
			$_ -like "$wordToComplete*"
		}
	})]
	[string] $IssueLinkName,

	[Parameter(Mandatory, ParameterSetName='ChangeRequest')]
	[ValidateSet('Outward', 'Inward')]
	[string] $IssueLinkDirection,

	[Parameter()]
	[string] $OutDir = '.'
)

# Functions
function GetDCAuthHeader {
	if (-not $DCPass) {
		$Secret = Read-Host "Enter DC password" -AsSecureString
		$Pass = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($Secret))
	} else {
		$Pass = $DCPass
	}
	$Headers = @{}
	$Headers['Content-Type'] = "application/json"
	$Auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($DCUser + ":" + $Pass))
	$Headers['Authorization'] = "Basic " + $Auth
	$Headers
}

function GetJiraAuthHeader {
	if (-not $JiraToken) {
		$Secret = Read-Host "Enter Jira Cloud API token" -AsSecureString
		$Pass = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($Secret))
	} else {
		$Pass = $JiraToken
	}
	$Headers = @{}
	$Headers['Content-Type'] = "application/json"
	$Auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($JiraEmail + ":" + $Pass))
	$Headers['Authorization'] = "Basic " + $Auth
	$Headers
}

function GetTableGridAuthHeader {
	param (
		[string] $AccountId
	)
	if (-not $TableGridToken) {
		$Secret = Read-Host "Enter Table Grid NG API token" -AsSecureString
		$Pass = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($Secret))
	} else {
		$Pass = $TableGridToken
	}
	$Headers = @{}
	$Headers['Content-Type'] = "application/json"
	$Auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($AccountId + '@' + $JiraDomain + ":" + $Pass))
	$Headers['Authorization'] = "Basic " + $Auth
	$Headers
}

# Call Invoke-WebRequest without throwing exception on 4xx/5xx 
function WebRequest {
	param (
		[string] $Uri,
		[string] $Method,
		[hashtable] $Headers,
		[object] $Body
	)
	$Response = $null
	try {
		$script:ProgressPreference = 'SilentlyContinue'    # Subsequent calls do not display UI.
		$Response = Invoke-WebRequest -Method $Method -Header $Headers -Uri $Uri -Body $Body
	} catch {
		$Response = @{}
		$Response.StatusCode = $_.Exception.Response.StatusCode.value__
		$Response.content = $_.Exception.Message
	} finally {
		$script:ProgressPreference = 'Continue'            # Subsequent calls do display UI.
	}
	$Response
}

function GetTableGridId {
	param (
		[string] $AccountId,
		[string] $TargetField
	)
	Write-Host Auth: $($TableGridAuthHeader | ConvertTo-Json)
	$Resp = WebRequest "https://${TableGridDomain}/rest/tgc/api/v1/grids" 'GET' $TableGridAuthHeader
	Write-Host $Resp.StatusCode
	Write-Host $Resp.Content
	$Json = $Resp.Content | ConvertFrom-Json
	$Result = $Null
	foreach ($Item in $Json) {
		if ($Item.name -eq $TargetField) {
			$Result = $Item.id
			break
		}
	}
	$Result
}

function AddTableGridRow {
	param (
		[string] $AccountId,
		[string] $TableGridId,
		[string] $IssueKey,
		[object] $Data
	)
	$Json = $Data | ConvertTo-Json
	$Url = "https://${TableGridDomain}/rest/tgc/api/v1/grids/${TableGridId}/issue/${IssueKey}"
	$Method = 'POST'
	$Resp = WebRequest $Url $Method $TableGridAuthHeader $Json
	LogResult $IssueKey $Url $Method $Json $Resp
	$Resp
}

function GetJiraAccountId {
	$Resp = WebRequest "https://${JiraDomain}/rest/api/latest/user/search?query=${JiraEmail}" 'GET' $JiraAuthHeader $Null
	$Json = $Resp.Content | ConvertFrom-Json
	$Json[0].accountId
}

function LogResult {
	param (
		[string] $IssueKey,
		[string] $Url,
		[string] $Method, 
		[string] $Payload, 
		[object] $Response
	)
	$Map = [ordered]@{}
	$Map['Issue Key'] = $IssueKey
	$Map['Status'] = $Response.StatusCode
	$Map['Response'] = $Response.Content
	$Map['URL'] = $URL
	$Map['Method'] = $Method
	$Map['Payload'] = $Payload
	$Obj = New-Object PsObject -Property $Map
	Export-Csv -Path $Out -Append -NoTypeInformation -InputObject $Obj
}

function FormatEffortTable {
	param (
		[string] $Task,
		[string] $HeadCountDay,
		[string] $Expense
	)
	$NewRow = @{}
	$NewRow['task'] = $Task
	$NewRow['headCountDay'] = $HeadCountDay
	$NewRow['expense'] = $Expense
	$NewRow
}

function ProcessEffortTable {
	param (
		[string] $AccountId, 
		[string] $TableGridName, 
		[string] $TableGridId, 
		[object] $CsvData
	)
	$Total = 0
	$Success = 0
	foreach ($Row in $CsvData) {
		$Total = $Total + 1
		$IssueKey = $Row."Issue Key"
		Write-Host -NoNewLine Processing $IssueKey ...
		$Data = $Row."Effort Table"
		$Json = $Data | ConvertFrom-Json
		$OutRows = [System.Collections.ArrayList]::new()
		$ExpenseSet = $False
		foreach($Row in $Json.rows) {
			if ($ExpenseSet) {
				$NewRow = FormatEffortTable $Row.task $Row.headCountDay 0
			} else {
				$NewRow = FormatEffortTable $Row.task $Row.headCountDay $Json.expenses
				$ExpenseSet = $True
			}
			[void] $OutRows.Add($NewRow)
		}
		$Output = @{}
		$Output['rows'] = $OutRows
		$Resp = AddTableGridRow $AccountId $TableGridId $IssueKey $Output
		Write-Host $Resp.StatusCode
		if (($Resp.StatusCode -band 200) -eq 200) {
			$Success = $Success + 1
		}
	}
	Write-Host Issues updated/total: ${Success}/${Total}
}

function FormatPackingNote {
	param (
		[string] $Name,
		[string] $Version,
		[string] $Md5Signature
	)
	$NewRow = @{}
	$NewRow['name'] = $Name
	$NewRow['version'] = $Version
	$NewRow['md5Signature'] = $Md5Signature
	$NewRow
}
function ProcessPackingNote {
	param (
		[string] $AccountId,
		[string] $TableGridName, 
		[string] $TableGridId, 
		[object] $CsvData
	)
	$Total = 0
	$Success = 0
	foreach ($Row in $CsvData) {
		$Total = $Total + 1
		$IssueKey = $Row."Issue Key"
		Write-Host -NoNewLine Processing $IssueKey ...
		$Json = $Row."Product / File Packing Note" | ConvertFrom-Json
		$OutRows = [System.Collections.ArrayList]::new()
		foreach($Row in $Json) {
			$NewRow = FormatPackingNote $Row.name $Row.version $Row.md5Signature
			[void] $OutRows.Add($NewRow)
		}
		$Output = @{}
		$Output['rows'] = $OutRows
		$Resp = AddTableGridRow $AccountId $TableGridId $IssueKey $Output
		Write-Host $Resp.StatusCode
		if (($Resp.StatusCode -band 200) -eq 200) {
			$Success = $Success + 1
		}
	}
	Write-Host Issues updated/total: ${Success}/${Total}
}

function ProcessURL {
	param (
		[string] $SourceField, 
		[string] $TargetField, 
		[object] $CsvData
	)
	$Total = 0
	$Success = 0
	foreach ($Row in $CsvData) {
		$Total = $Total + 1
		$IssueKey = $Row."Issue Key"
		Write-Host -NoNewLine Processing $IssueKey ...
		# Export formats the JSON array using toString(), so brackets are lost and no comma
		$RowData = '[' + $Row."$SourceField".Replace('} {', '},{') + ']'
		$Json = $RowData | ConvertFrom-Json
		# v3 API still requires ADF, but latest accept markdown again
		$Text = ""
		foreach ($Item in $Json) {
			$Text = $Text + '[' + $Item.displayText + '|' + $Item.url + "]`n"
		}
		$Body = @{
			"fields" = @{
				"$TargetField" = $Text
			}
		}
		$Payload = $Body | ConvertTo-Json -Depth 100
		$Url = "https://${JiraDomain}/rest/api/latest/issue/${IssueKey}"
		$Method = 'PUT'
		$Resp = WebRequest $Url $Method $JiraAuthHeader $Payload
		Write-Host $Resp.StatusCode
		if (($Resp.StatusCode -band 200) -eq 200) {
			$Success = $Success + 1
		}
		LogResult $IssueKey $Url $Method $Payload $Resp
	}
	Write-Host Issues updated/total: ${Success}/${Total}
}

function ProcessImage {
	param (
		[string] $TargetField,
		[string] $ImageURL,
		[int] $ImageWidth,
		[int] $ImageHeight,
		[string] $ImageJQL
	)
	if (($ImageWidth -eq 0) -and ($ImageHeight -eq 0)) {
		# Calculate image size
		$WebClient = New-Object System.Net.WebClient
		$ImageStream = $Null
		try {
			$ImageStream = $WebClient.OpenRead($ImageURL)
			$Image = [System.Drawing.Image]::FromStream($ImageStream)
			$ImageWidth = $Image.Width
			$ImageHeight = $Image.Height
		} finally {
			$WebClient.Dispose()
			if ($ImageStream) {
				$ImageStream.Dispose()
			}
		}
	}
	$NextPageToken = $Null
	$Total = 0
	$Success = 0
	do {
		$Resp = WebRequest "https://${JiraDomain}/rest/api/3/search/jql?jql=${ImageJQL}&nextPageToken=${NextPageToken}&fields=key" 'GET' $JiraAuthHeader
		$Json = $Resp.Content | ConvertFrom-Json
		foreach ($Issue in $Json.issues) {
			$Total = $Total + 1
			$IssueKey = $Issue.key
			Write-Host -NoNewLine Processing $IssueKey ...
			$Url = "https://${JiraDomain}/rest/api/latest/issue/${IssueKey}"
			$Method = 'PUT'
			$Markdown = "!${ImageURL}"
			if (($ImageWidth -ne 0) -and ($ImageHeight -ne 0)) {
				$Markdown += "|width=${ImageWidth},height=${ImageHeight}"
			} elseif (($ImageWidth -ne 0) -and ($ImageHeight -eq 0)) {
				$Markdown += "|width=${ImageWidth}"
			} elseif (($ImageHeight -ne 0) -and ($ImageWidth -eq 0)) {
				$Markdown += "|height=${ImageHeight}"
			}
			$Markdown += "!"
			Write-Host Markdown: $Markdown
			$Body = @{
				'fields' = @{
					"${TargetField}" = "${Markdown}"
				}
			}
			$Payload = $Body | ConvertTo-Json -Depth 100
			$Resp = WebRequest $Url $Method $JiraAuthHeader $Payload
			Write-Host $Resp.StatusCode
			if (($Resp.StatusCode -band 200) -eq 200) {
				$Success = $Success + 1
			}
			LogResult $IssueKey $Url $Method $Payload $Resp
		}
		$NextPageToken = $Json.nextPageToken
	} while ($NextPageToken)
	Write-Host Issues updated/total: ${Success}/${Total}
}

function TranslateChangeRequest {
	param (
		[string] $SourceField, 
		[object] $CsvData
	)
	$Total = 0
	$Success = 0
	foreach ($Row in $CsvData) {
		$Total = $Total + 1
		$IssueKey = $Row."Issue Key"
		Write-Host Processing $IssueKey ...
		$Data = $Row."$SourceField" | ConvertFrom-Json
		$List = [System.Collections.ArrayList]::new()
		$HasError = $False
		foreach ($Item in $Data) {
			$URL = "${DCProtocol}://${DCDomain}/rest/api/latest/issue/${Item}?fields=key"
			$Resp = WebRequest $URL 'GET' $DCAuthHeader
			if (($Resp.StatusCode -band 200) -eq 200) {
				$Json = $Resp.Content | ConvertFrom-Json
				[void] $List.Add($Json.key)
			} else {
				$HasError = $True
				Write-Host "`tIssue Id ${Item} not found for issue ${IssueKey}"
			}
		}
		if (-not $HasError) {
			$Success = $Success + 1
		}
		$Map = @{}
		$Map."Issue Key" = $IssueKey
		$Map."$SourceField" = $List | ConvertTo-Json -Depth 100
		$Obj = New-Object PsObject -Property $Map
		Export-Csv -Path $Out -Append -NoTypeInformation -InputObject $Obj
	}
	Write-Host Rows updated/total: ${Success}/${Total}
}

function ProcessChangeRequest {
	param (
		[string] $SourceField,
		[string] $TargetField,
		[string] $IssueLinkName,
		[string] $IssueLinkDirection,
		[object] $CsvData
	)
	$IssueCount = 0
	$Total = 0
	$Success = 0
	foreach ($Row in $CsvData) {
		$IssueCount = $IssueCount + 1
		$IssueKey = $Row."Issue Key"
		$Data = $Row."$SourceField" | ConvertFrom-Json
		Write-Host Processing $IssueKey ...
		$Url = "https://${JiraDomain}/rest/api/3/issueLink"
		$Method = 'POST'
		foreach ($Item in $Data) {
			$Total = $Total + 1
			Write-Host -NoNewLine "`t" Adding link ($IssueLinkName) to $Item ...  
			switch ($IssueLinkDirection) {
				'Outward' {
					$Body = @{
						'inwardIssue' = @{
							'key' = $IssueKey
						}
						'outwardIssue' = @{
							'key' = $Item
						}
						'type' = @{
							'name' = $IssueLinkName
						}
					}
				}
				'Inward' {
					$Body = @{
						'inwardIssue' = @{
							'key' = $Item
						}
						'outwardIssue' = @{
							'key' = $IssueKey
						}
						'type' = @{
							'name' = $IssueLinkName
						}
					}
				}
			}
			$Payload = $Body | ConvertTo-Json -Depth 100
			$Resp = WebRequest $Url $Method $JiraAuthHeader $Payload
			Write-Host $Resp.StatusCode
			if (($Resp.StatusCode -band 200) -eq 200) {
				$Success = $Success + 1
			}
			LogResult $IssueKey $Url $Method $Payload $Resp
		}
	}
	Write-Host Issue count: $IssueCount
	Write-Host Links added/total: ${Success}/${Total}
}

function GetDCUserInfo {
	param (
		[string] $UserKey
	)
	$Resp = WebRequest "${DCProtocol}://${DCDomain}/rest/api/latest/user?key=${UserKey}" 'GET' $DCAuthHeader
	if ($Resp.StatusCode -eq 200) {
		$Json = $Resp.Content | ConvertFrom-Json
		$Output = $Json.displayName + ' (' + $Json.emailAddress + ')'
		$Output
	} else {
		$UserKey
	}
}

function TranslateApprovalPath {
	param (
		[string] $SourceField,
		[object] $CsvData
	)
	$Total = 0
	foreach ($Row in $CsvData) {
		$Total += 1
		$IssueKey = $Row."Issue Key"
		$Json = $Row."$SourceField" | ConvertFrom-Json
		Write-Host Processing issue $IssueKey ...
		$Output = ''
		# Get approval names from settings
		if ($Json.settings) {
			foreach ($ApprovalSetting in $Json.settings.psobject.Properties) {
				$ApprovalName = $ApprovalSetting.Name
				$Output += '- ' + $ApprovalName + ": `n"
				if ($Json.history."${ApprovalName}") {
					$ApprovalItems = $Json.history."${ApprovalName}"
					if ($ApprovalItems -and $ApprovalItems.psobject.Properties.Count -gt 0) {
						foreach ($ApprovalItem in $ApprovalItems.psobject.Properties) {
							$ApprovalData = $ApprovalItem.Value
							$Output += "`t- "
							if ($ApprovalData.approved) {
								$Output += 'Approved by '
							} else {
								$Output += 'Rejected by '
							}
							if ($ApprovalData.delegated) {
								$Approver = (GetDCUserInfo($ApprovalData.delegated)) + ' on behalf of ' + (GetDCUserInfo($ApprovalData.approver))
							} else {
								$Approver = GetDCUserInfo($ApprovalData.approver)
							}
							$Output += $Approver + ' at ' + $ApprovalData.approvalDate + "`n"
						}
					} else {
						$Output += "`t- No approval record`n"
					}
				} else {
					$Output += "`t- No approval record`n"
				}
			}
		}
		$Map = @{}
		$Map."Issue Key" = $IssueKey
		$Map."$SourceField" = $Output
		$Obj = New-Object PsObject -Property $Map
		Export-Csv -Path $Out -Append -NoTypeInformation -InputObject $Obj
	}
	Write-Host Issues translated: ${Total}
}

function ProcessApprovalPath {
	param (
		[string] $SourceField,
		[string] $TargetField,
		[object] $CsvData
	)
	$Total = 0
	$Success = 0
	foreach ($Row in $CsvData) {
		$IssueKey = $Row."Issue Key"
		$Message = $Row."$SourceField"
		$Total = $Total + 1
		Write-Host -NoNewLine Processing $IssueKey ...
		$Url = "https://${JiraDomain}/rest/api/latest/issue/${IssueKey}"
		$Method = 'PUT'
		$Body = @{
			'fields' = @{
				"${TargetField}" = "${Message}"
			}
		}
		$Payload = $Body | ConvertTo-Json -Depth 100
		$Resp = WebRequest $Url $Method $JiraAuthHeader $Payload
		Write-Host $Resp.StatusCode
		if (($Resp.StatusCode -band 200) -eq 200) {
			$Success = $Success + 1
		}
		LogResult $IssueKey $Url $Method $Payload $Resp
	}
	Write-Host Issues updated/total: ${Success}/${Total}
}

# Main
$Timestamp = $(Get-Date -Format 'yyyyMMddHHmmss')
$JiraAuthHeader = @{}
$TableGridAuthHeader = @{}
$DCAuthHeader = @{}
if ($ApprovalPath_Translate) {
	$DCAuthHeader = GetDCAuthHeader
	$FileName = $In | Split-Path -LeafBase
	$Out = "${OutDir}\${FileName}.Translated.csv"
	$CsvData = Import-Csv -Path $In
	TranslateApprovalPath $SourceField $CsvData
} elseif ($ApprovalPath) {
	$JiraAuthHeader = GetJiraAuthHeader
	$FileName = $In | Split-Path -LeafBase
	$Out = "${OutDir}\${FileName}.Result.${Timestamp}.csv"
	$CsvData = Import-Csv -Path $In
	ProcessApprovalPath $SourceField $TargetField $CsvData
} elseif ($EffortTable) {
	$JiraAuthHeader = GetJiraAuthHeader
	$AccountId = GetJiraAccountId
	$TableGridAuthHeader = GetTableGridAuthHeader $AccountId
	$FileName = $In | Split-Path -LeafBase
	$Out = "${OutDir}\${FileName}.Result.${Timestamp}.csv"
	$CsvData = Import-Csv -Path $In
	$TableGridId = GetTableGridId $AccountId $TargetField
	Write-Host Table Grid Id: $TableGridId
	ProcessEffortTable $AccountId $TargetField $TableGridId $CsvData
} elseif ($PackingNote) {
	$JiraAuthHeader = GetJiraAuthHeader
	$AccountId = GetJiraAccountId
	$TableGridAuthHeader = GetTableGridAuthHeader $AccountId
	$FileName = $In | Split-Path -LeafBase
	$Out = "${OutDir}\${FileName}.Result.${Timestamp}.csv"
	$CsvData = Import-Csv -Path $In
	$TableGridId = GetTableGridId $AccountId $TargetField
	Write-Host Table Grid Id: $TableGridId
	ProcessPackingNote $AccountId $TargetField $TableGridId $CsvData
} elseif ($URL) {
	$JiraAuthHeader = GetJiraAuthHeader
	$FileName = $In | Split-Path -LeafBase
	$Out = "${OutDir}\${FileName}.Result.${Timestamp}.csv"
	$CsvData = Import-Csv -Path $In
	ProcessURL $SourceField $TargetField $CsvData
} elseif ($StaticImage) {
	$JiraAuthHeader = GetJiraAuthHeader
	$Out = "${OutDir}\StaticImage.Result.${Timestamp}.csv"
	ProcessImage $TargetField $ImageURL $ImageWidth $ImageHeight $JQL
} elseif ($ChangeRequest_Translate) {
	$DCAuthHeader = GetDCAuthHeader
	$FileName = $In | Split-Path -LeafBase
	$Out = "${OutDir}\${FileName}.Translated.csv"
	$CsvData = Import-Csv -Path $In
	TranslateChangeRequest $SourceField $CsvData
} elseif ($ChangeRequest) {
	$JiraAuthHeader = GetJiraAuthHeader
	$FileName = $In | Split-Path -LeafBase
	$Out = "${OutDir}\${FileName}.Result.${Timestamp}.csv"
	$CsvData = Import-Csv -Path $In
	ProcessChangeRequest $SourceField $TargetField $IssueLinkName $IssueLinkDirection $CsvData
}
Write-Host Output written to $Out