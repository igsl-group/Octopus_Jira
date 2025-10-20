<#
.SYNOPSIS
	Generate report as CSV file.
	
.DESCRIPTION
	Export issues from Jira Cloud. 
	
	Interactive mode:
	.\GenerateReport.ps1
	
	Query mode:
	.\GenerateReport.ps1 -Query -ServerDomain <https://IP:Port> -ServerUser <Username> [-ServerPass <Password>] -Jql <JQL> [-Fields <Comma-delimited list of field ID>] [-DateRange <Date Range JQL>] [-MaxResults #]
	.\GenerateReport.ps1 -Query -CloudDomain <https://[Domain].atlassian.net> -CloudUser <Email> [-CloudPass <API Token>] -Jql <JQL> [-Fields <Comma-delimited list of field ID>] [-DateRange <Date Range JQL>] [-MaxResults #]
	
	Output is saved in a CSV file:
		1. File encoding is UTF-8 without byte order mark.
		2. First line is header row.
		3. issue key will always appear as the first column.
	
.PARAMETER Query
	Switch. Disable interactive mode.
	
.PARAMETER ServerDomain
	e.g. http://localhost:8080

.PARAMETER CloudDomain
	e.g. https://kcwong.atlassian.net
	
.PARAMETER ServerUser
	Server username.
	
.PARAMETER CloudUser
	User email.
	
.PARAMETER ServerPass
	User passowrd.
	
.PARAMETER CloudPass
	API token.
	
.PARAMETER Jql
	JQL used to search issues.

.PARAMETER Fields
	Issue fields to export. Default is: @("summary")
	Specify *navigable to export user-readable fields. 
	Specify *all to export all fields.
	Specify a list to export specific fields, e.g. @("summary", "description", "status", "assignee", "customfield_10068")
	Note that issue key is always included even if you do not specify it.

.PARAMETER ParseFields
	Boolean. If true, fields will be parsed (to human readable format). Otherwise in raw format (could be simple literals or JSON).

.PARAMETER DateRange
	JQL clause to limit issues exported. Default is empty (no limit).
	e.g. created < -5d    (Created date more than 5 days ago)
	e.g. created >= -10d    (Created date within 10 days)

.PARAMETER MaxResults
	Max no. of issues to retrieve in one API call. Default 5000. 
	Note that server side can override this value.
	
#>
[CmdletBinding(DefaultParameterSetName = "Interactive")]
Param(
	[Parameter(Mandatory, ParameterSetName = "QueryServer")]
	[Parameter(Mandatory, ParameterSetName = "QueryCloud")]
	[switch] $Query,

	[Parameter(Mandatory, ParameterSetName = "QueryServer")]
	[string] $ServerDomain,

	[Parameter(Mandatory, ParameterSetName = "QueryServer")]
	[string] $ServerUser,
	
	[Parameter(Mandatory, ParameterSetName="QueryServer")]
	[string] $ServerPass,
	
	[Parameter(Mandatory, ParameterSetName = "QueryCloud")]
	[string] $CloudDomain,
	
	[Parameter(Mandatory, ParameterSetName = "QueryCloud")]
	[string] $CloudUser,
	
	[Parameter(Mandatory, ParameterSetName="QueryCloud")]
	[string] $CloudPass,

	[Parameter(Mandatory, ParameterSetName = "QueryServer")]
	[Parameter(Mandatory, ParameterSetName = "QueryCloud")]
	[string] $Jql,
	
	[Parameter(ParameterSetName = "QueryServer")]
	[Parameter(ParameterSetName = "QueryCloud")]
	[string] $DateRange = "",
	
	[Parameter(ParameterSetName = "QueryServer")]
	[Parameter(ParameterSetName = "QueryCloud")]
	[System.Collections.ArrayList] $Fields = @("summary"),
	
	[Parameter(ParameterSetName = "QueryServer")]
	[Parameter(ParameterSetName = "QueryCloud")]
	[bool] $ParseFields = $True,
	
	[Parameter(ParameterSetName = "QueryServer")]
	[Parameter(ParameterSetName = "QueryCloud")]
	[int] $MaxResults = 5000
)

# Constants
Set-Variable -Name CredExt -Value ".cred" -Option Constant
Set-Variable -Name DateFormatIn -Value "yyyy-MM-dd" -Option Constant
Set-Variable -Name DateFormatOut -Value "yyyy-MM-dd" -Option Constant
Set-Variable -Name DatetimeFormatIn -Value "yyyy-MM-ddTHH:mm:ss.fffzzzz" -Option Constant
Set-Variable -Name DatetimeFormatOut -Value "yyyy-MM-dd HH:mm:ss" -Option Constant

$Out = $null

$global:IsServer = $True
$global:Domain = $null

# Sort Fields
$Fields.Sort()

# Globally cached field information
$ServerFieldInfo = @{}
$ServerFieldMap = [ordered] @{}	# Key: Field ID		Value: Map of [Name, DataType, FieldType]
$CloudFieldInfo = @{}
$CloudFieldMap = [ordered] @{}	# Key: Field ID		Value: Map of [Name, DataType, FieldType]

class RestException : Exception {
    RestException($Message) : base($Message) {
    }
}

function AnyKeyToContinue {
	Write-Host "Press Any Key to Continue"
	$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown');
}

function RefreshFields {
	param (
		[hashtable] $Headers,
		[bool] $Refresh
	)
	# Common field type prefixes to remove to avoid clutter
	$AtlassianPrefixes = @(
		"com.atlassian.jira.plugin.system.customfieldtypes:",
		"com.atlassian.servicedesk:",
		"com.atlassian.jpo:",
		"com.pyxis.greenhopper.jira:",
		"com.atlassian.jira.plugins.work-category-field:",
		"com.atlassian.jira.ext.charting:",
		"com.atlassian.servicedesk.approvals-plugin:",
		"com.atlassian.jira.plugins.service-entity:",
		"com.atlassian.jira.plugins.jira-development-integration-plugin:",
		"com.atlassian.jconnect.jconnect-plugin:",
		"com.atlassian.jira.plugins.proforma-managed-fields:",
		"com.atlassian.jira.modules.servicemanagement.major-incident-entity:",
		"com.atlassian.servicedesk.servicedesk-lingo-integration-plugin:",
		"com.atlassian.jira.modules.servicemanagement.responders-entity:",
		"com.atlassian.servicedesk.sentiment:"
	)
	if ($global:IsServer) {
		$FieldInfo = $ServerFieldInfo
		$FieldMap = $ServerFieldMap
	} else {
		$FieldInfo = $CloudFieldInfo
		$FieldMap = $CloudFieldMap
	}	
	if ($Refresh -or ($FieldMap.Count -le 0)) {
		Write-Host "Refreshing field information..."
		$FieldInfo.Clear()
		$Info = GetFieldSchema $Headers
		foreach ($Item in $Info.GetEnumerator()) {
			$FieldInfo[$Item.Name] = $Item.Value
		}		
		# Swap key and value for access
		$FieldMap.Clear()
		foreach ($Item in $FieldInfo.GetEnumerator()) {
			$FieldID = $Item.Key
			$FieldName = $Item.Value.name
			$FieldDataType = $Item.Value.schema.type
			if ($Item.Value.schema.type -eq "array") {
				$FieldDataType = $FieldDataType + "[" + $Item.Value.schema.items + "]"
			}
			if ($Item.Value.schema.system) {
				$FieldType = $Item.Value.schema.system
			} else {
				$FieldType = $Item.Value.schema.custom
			}
			foreach ($Prefix in $AtlassianPrefixes) {
				if ($FieldType -like "${Prefix}*") {
					$FieldType = $FieldType.Substring($Prefix.Length)
				}	
			}			
			$FieldMap[$FieldID] = @{
				"Name" = $FieldName;
				"DataType" = $FieldDataType;
				"FieldType" = $FieldType;
			}
		}
	}
}

function SelectFields {
	param (
		[hashtable] $Headers,
		[System.Collections.ArrayList] $Fields
	)
	RefreshFields $Headers $False
	# Initial search string and result
	$NameSearchString = ""
	$DataTypeSearchString = ""
	$FieldTypeSearchString = ""
	$Matches = [System.Collections.ArrayList]::new()
	# Initalize SelectedFields from Fields
	$SelectedFields = [System.Collections.ArrayList]::new()
	if ($global:IsServer) {
		$FieldMap = $ServerFieldMap
	} else {
		$FieldMap = $CloudFieldMap
	}
	foreach ($Item in $Fields) {
		if ($FieldMap.Contains($Item)) {
			[void] $SelectedFields.Add($Item)
		}
	}
	$AddAll = $false
	if ($Fields.Contains("*all")) {
		$AddAll = $true
	}
	$AddNavigable = $false
	if ($Fields.Contains("*navigable")) {
		$AddNavigable = $true
	}
	$Quit = $false
	$Msg = ''
	$Columns = "[{0,-2}] {1,-20} {2,-40} {3,-20} {4,-20}"		
	do {
		$AddList = [System.Collections.ArrayList]::new()
		$RemoveList = [System.Collections.ArrayList]::new()
		Clear-Host
		Write-Host $Msg
		Write-Host "================================================================================"
		Write-Host "Selected Fields"
		Write-Host "================================================================================"
		Write-Host "[C] Remove All Selected Fields"
		if ($AddAll) {
			Write-Host "[F] All Fields (*all)"
		}
		if ($AddNavigable) {
			Write-Host "[N] Navigable Fields (*navigable)"
		}
		if ($SelectedFields.Count -gt 0) {
			Write-Host $($Columns -f "", "ID", "Name", "Data Type", "Field Type")
		}
		$Idx = 0
		foreach ($Item in ($SelectedFields | Sort-Object)) {
			$Idx++
			$FieldID = $Item
			$FieldName = $FieldMap[$Item].Name
			$DataType = $FieldMap[$Item].DataType
			$FieldType = $FieldMap[$Item].FieldType
			[void] $RemoveList.Add($FieldID)
			Write-Host $($Columns -f "R${Idx}", $FieldID, $FieldName, $DataType, $FieldType)
		}		
		Write-Host
		Write-Host "================================================================================"
		Write-Host "Available Fields"
		Write-Host "================================================================================"
		Write-Host "[I] Refresh Field Information"
		if (-not $AddAll) {
			Write-Host "[A] Add All Fields (*all)"
		}
		if (-not $AddNavigable) {
			Write-Host "[V] Add Navigable Fields (*navigable)"
		}
		Write-Host "[D] Add Fields Directly"
		Write-Host "[N] Search by name: [${NameSearchString}]"
		Write-Host "[T] Search by data type: [${DataTypeSearchString}]"
		Write-Host "[F] Search by field type: [${FieldTypeSearchString}]"
		Write-Host "[S] Search"
		if ($Matches.Count -gt 0) {
			Write-Host $($Columns -f "", "ID", "Name", "Data Type", "Field Type")
		}
		$Idx = 0
		foreach ($Item in ($Matches | Sort-Object)) {
			$Idx++
			$FieldID = $Item
			$FieldName = $FieldMap[$Item].Name
			$DataType = $FieldMap[$Item].DataType
			$FieldType = $FieldMap[$Item].FieldType
			[void] $AddList.Add($FieldID)
			Write-Host $($Columns -f $Idx, $FieldID, $FieldName, $DataType, $FieldType)
		}
		Write-Host
		Write-Host "================================================================================"
		Write-Host "NOTE: Issue key is always included as the first column"
		Write-Host "[X] Return"
		Write-Host "================================================================================"
		$All = "All"
		$Option = Read-Host "Option"
		switch ($Option) {
			"d" {
				$DirectFields = Read-Host "Enter comma-delimited field IDs"
				if ($DirectFields) {
					$List = $DirectFields -Split ","
					foreach ($Item in $List) {
						if ($FieldMap[$Item]) {
							[void] $SelectedFields.Add($Item)
						}
					}
				}
				break
			}
			"i" {
				RefreshFields $Headers $True
				break
			}
			"c" {
				[void] $SelectedFields.Clear()
				$AddAll = $false
				$AddNavigable = $false
				break
			}
			"a" {
				$AddAll = -not $AddAll
				break
			}
			"v" {
				$AddNavigable = -not $AddNavigable
				break
			}
			"n" {
				$NameSearchString = Read-Host "Search by name"
				break
			}
			"t" {
				$DataTypeSearchString = Read-Host "Search by data type"
				break
			}
			"f" {
				$FieldTypeSearchString = Read-Host "Search by field type"
				break
			}
			"s" {
				$Matches.Clear()
				foreach ($Item in $FieldMap.GetEnumerator()) {
					$NameMatch = $False
					$DataTypeMatch = $False
					$FieldTypeMatch = $False
					if ($NameSearchString.Length -gt 0) {
						$NameMatch = $Match && $Item.Value.Name -like ("*" + $NameSearchString + "*")
					} else {
						$NameMatch = $True
					}
					if ($DataTypeSearchString.Length -gt 0) {
						$DataTypeMatch = $Match && $Item.Value.DataType -like ("*" + $DataTypeSearchString + "*")
					} else {
						$DataTypeMatch = $True
					}
					if ($FieldTypeSearchString.Length -gt 0) {
						$FieldTypeMatch = $Match && $Item.Value.FieldType -like ("*" + $FieldTypeSearchString + "*")
					} else {
						$FieldTypeMatch = $True
					}
					if ($NameMatch -eq $True -and $DataTypeMatch -eq $True -and $FieldTypeMatch -eq $True) {
						[void] $Matches.Add($Item.Key)
					}
				}
				break
			}
			{$_ -match "^[0-9]+$"} {
				$Target = $Option
				$Cnt = $AddList.Count
				if ($Target -gt 0 -and $Target -le $Cnt) {
					$ID = $AddList[$Target - 1]
					if (-not $SelectedFields.Contains($ID)) {
						[void] $SelectedFields.Add($ID)
					}
				}
				break
			}
			{$_ -match "^[rR][0-9]+$"} {
				$Target = $Option.Substring(1)
				$Cnt = $RemoveList.Count
				if ($Target -gt 0 -and $Target -le $Cnt) {
					$ID = $RemoveList[$Target - 1]
					if ($SelectedFields.Contains($ID)) {
						[void] $SelectedFields.Remove($ID)
					}
				}
				break
			}
			"x" {
				$Quit = $true
				break
			}
		} # switch 
	} while (-not $Quit)
	$Result = [System.Collections.ArrayList]::new()
	foreach ($Item in $SelectedFields) {
		[void] $Result.Add($Item)
	}
	if ($AddAll) {
		[void] $Result.Add("*all")
	}
	if ($AddNavigable) {
		[void] $Result.Add("*navigable")
	}
	$Result.Sort()
	Write-Output -NoEnumerate $Result
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

function GetFieldSchema {
	param (
		[hashtable] $Headers
	)
	$Result = @{}
	try {
		$StartAt = 0
		$IsLast = $false
		do {
			$Body = @{
				"startAt" = $StartAt;
				"query" = "";
				"maxResults" = $MaxResults;
			}
			if ($global:IsServer) {
				$Uri = $Domain + "/rest/api/latest/field"
			} else {
				$Uri = $Domain + "/rest/api/latest/field/search"
			}
			$Response = WebRequest $Uri "GET" $Headers $Body
			if ($Response.StatusCode -ne 200) {
				throw $Response.Content
			}
			$Json = $Response.Content | ConvertFrom-Json
			if ($global:IsServer) {
				foreach ($Item in $Json) {
					$Id = $Item.id
					$Result."$Id" = $Item
				}
				$IsLast = $True
			} else {
				foreach ($Item in $Json.values) {
					$Id = $Item.id
					$Result."$Id" = $Item
				}
				$IsLast = $Json.isLast
				$StartAt += $Json.values.Count
			}
		} while (-not $IsLast)
	} catch [RestException] {
		throw $PSItem
	}
	$Result
}

function SearchIssue {
	param (
		[hashtable] $Headers,
		[string] $Jql,
		[string[]] $Fields,
		[int] $Max = $MaxResults,
		[int] $StartAt = 0,
		[string] $NextPageToken = $null
	)
	[hashtable] $Result = @{}
	try {
		if ($global:IsServer) {
			$Uri = $Domain + "/rest/api/latest/search"
		} else {
			$Uri = $Domain + "/rest/api/latest/search/jql"
		}
		$Body = @{}
		$Body["maxResults"] = $Max
		$Body["expand"] = "names,renderedFields"
		if ($global:IsServer) {
			$Body["startAt"] = $StartAt
			$Body["fields"] = $Fields -Join ",";
		} else {
			$Body["fields"] = @($Fields)
			if ($NextPageToken) {
				$Body["nextPageToken"] = $NextPageToken
			}
		}
		if ($Jql) {
			$Body["jql"] = $Jql
		}
		if ($global:IsServer) {
			$Response = WebRequest $Uri "GET" $Headers -Body $Body
		} else {
			$JsonBody = $Body | ConvertTo-Json
			$Response = WebRequest $Uri "POST" $Headers -Body $JsonBody
		}
		switch ($Response.StatusCode) {
			200 {
				break
			}
			400 {
				throw "Bad request (400). Please verify JQL is valid."
			}
			401 {
				throw "Unauthorized (401). Please verify credential file."
			}
			default {
				throw "Faile to search: " + $Response.Content
			}
		}
		$Response.Content | ConvertFrom-Json
	} catch [RestException] {
		throw $PSItem
	}
}

function WriteCSVHeader {
	param (
		[string] $Csv,
		[PSCustomObject] $Names
	)
	$Line = ""
	foreach ($Item in ($Names.PSObject.Properties | Sort-Object)) {
		$FieldName = $Item.Value
		# Escape double quotes
		$FieldName = $FieldName -Replace '"', '""'
		$Line += ",`"${FieldName}`""
	}
	$Line = "Issue Key" + $Line
	Set-Content -Path $Csv -Value $Line
}

function ParseDate {
	param(
		[PSObject] $Data
	)
	# PowerShell 7 will automatically convert dates, so check both types
	if ($Data) {
		if ($Data.GetType().Name -eq "date") {
			$Data.ToString($DateFormatOut)
		} else {
			[datetime]::ParseExact($Data, $DateFormatIn, $null).ToString($DateFormatOut)
		}
	} else {
		""
	}
}

function ParseDateTime {
	param(
		[PSObject] $Data
	)
	# PowerShell 7 will automatically convert dates, so check both types
	if ($Data) {
		if ($Data.GetType().Name -eq "date") {
			$Data.ToString($DatetimeFormatOut)
		} elseif ($Data.GetType().Name -eq "datetime") {
			$Data.ToString($DatetimeFormatOut)
		} else {
			[datetime]::ParseExact($Data, $DatetimeFormatIn, $null).ToString($DatetimeFormatOut)
		}
	} else {
		""
	}
}

function ParseContentNode {
	param (
		[PSObject] $Node, 
		[string] $Prefix = ""
	)
	$NL = "`r`n"
	$Indent = "    "
	$Result = ""
	switch ($Node."type") {
		# Top/child level block nodes
		"blockquote" {
			$Result = $NL
			foreach ($QuoteItem in $Node."content") {
				$R = ParseContentNode $QuoteItem $Prefix
				$Result += $Prefix + $R + $NL
			}
			break
		}
		"bulletList" {
			$Result = $NL
			foreach ($ListItem in $Node."content") {
				$R = ParseContentNode $ListItem ($Prefix + $Indent)
				$Result += $Prefix + "* " + $R + $NL
			}
			break
		}
		"codeBlock" {
			$Result = $NL
			foreach ($CodeItem in $Node."content") {
				$R = ParseContentNode $CodeItem $Prefix
				$Result += $Prefix + $R + $NL
			}
			break
		}
		"heading" {
			$Result = $NL
			foreach ($HeadingItem in $Node."content") {
				$R = ParseContentNode $HeadingItem $Prefix
				$Result += $Prefix + $R + $NL
			}
			break
		}
		"mediaGroup" {
			# Not translated to text
			$Result = ""
			break
		}
		"mediaSingle" {
			# Not translated to text
			$Result = ""
			break
		}
		"orderedList" {
			$Result = $NL
			$Count = 1
			foreach ($ListItem in $Node."content") {
				$R = ParseContentNode $ListItem ($Prefix + $Indent)
				$Result += $Prefix +  + $Count + ") " + $R + $NL
				$Count++
			}
			break
		}
		"panel" {
			$Result = $NL
			foreach ($PanelItem in $Node."content") {
				$R = ParseContentNode $PanelItem $Prefix
				$Result += $Prefix + $R + $NL
			}
			break
		}
		"paragraph" {
			$Result = ""
			$Count = 0
			foreach ($ParagraphItem in $Node."content") {
				$Count++
				$R = ParseContentNode $ParagraphItem $Prefix
				$Result += $Prefix + $R + $NL
			}
			break
		}
		"rule" {
			$Result = $NL
			break
		}
		"table" {
			$Result = ""
			foreach ($TableItem in $Node."content") {
				$R = ParseContentNode $TableItem $Prefix
				$Result += $Prefix + $R + $NL
			}
			break
		}
		# Child block nodes
		"listItem" {
			$Result = ""
			foreach ($ListItem in $Node."content") {
				$R = ParseContentNode $ListItem $Prefix
				$Result += $Prefix + $R
			}
			break
		}
		"media" {
			# Not translated to text
			$Result = ""
			break
		}
		{$_ -eq "tableCell" -or $_ -eq "tableHeader"} {
			$Result = ""
			foreach ($Row in $Node."content") {
				$R = ParseContentNode $Row $Prefix
				$Result += $Prefix + $R
			}
			break
		}
		"tableRow" {
			$Result = $Prefix
			foreach ($Row in $Node."content") {
				$R = ParseContentNode $Row $Prefix
				$Result += "|" + $R
			}
			# Add trailing separator
			if ($Result.Length -ge 1) {
				$Result += "|"
			}
			break
		}
		# Inline nodes
		"emoji" {
			$Result = $Node."attrs"."shortName"
			break
		}
		"hardBreak" {
			$Result = "`r`n"
			break
		}
		"inlineCard" {
			$Result = $Node."attrs"."url"
			break
		}
		"mention" {
			$Result = $Node."attrs"."text" + " (" + $Node."attrs"."id" + ")"
			break
		}
		"text" {
			# Check markup
			$IsHyperLink = $False
			if ($Node."marks") {
				foreach ($Mark in $Node."marks") {
					if ($Mark."type" -eq "link") {
						$Result = $Node."text" + " (" + $Mark."attrs"."href" + ")"
						$IsHyperLink = $True
						break
					}
				}
			}
			if (-not $IsHyperLink) {
				$Result = $Node."text"
			}
			break
		}
		default {
			$Msg = "Node type `"" + $Node."type" + "`" is not supported"
			throw $Msg
		}
	}
	return $Result
}

# Helper function to convert Jira Document structure into text
# https://developer.atlassian.com/cloud/jira/platform/apis/document/structure/
function ParseContent {
	param (
		[PSObject] $ContentJSON
	)
	$Result = ""
	# Check version
	if ($ContentJSON."version" -ne 1) {
		# Unsupported Format
		throw "Jira document version `"" + $ContentJSON."version" + "`" is not supported"
	}
	# Check type, root node should be "doc"
	if ($ContentJSON."type" -ne "doc") {
		throw "Jira document type `"" + $ContentJSON."type" + "`" is not supported"
	}
	foreach ($Item in $ContentJSON."content") {
		$Result += ParseContentNode $Item
	}
	$Result
}

function ParseFieldValue {
	param (
		[hashtable] $Headers,
		[string] $Type,
		[string] $Items,
		[PSObject] $FieldValue
	)
	$NL = "`r`n"
	$Result = ""
	if ($FieldValue -ne $null -and $FieldValue -ne "") {
		switch ($Type) {
			"array" {
				$List = [System.Collections.ArrayList]::new()
				foreach ($Item in $FieldValue) {
					$Value = ParseFieldValue $Headers $Items $null $Item
					[void] $List.Add($Value)
				}
				$Result = "`"" + ($List -join "`",`"") + "`""
				break
			}		
			# Handled types
			"attachment" {
				$Result = $FieldValue."filename"
				break
			}
			"date" {
				$Result = ParseDate $FieldValue
				break
			}
			"datetime" {
				$Result = ParseDateTime $FieldValue
				break
			}
			"group" {
				$Result = $FieldValue."name"
				break
			}
			"issuelinks" {
				foreach ($Link in $FieldValue) {
					$Result += $NL
					if ($Link."outwardIssue") {
						$Result += $Link."type"."outward" + " " + $Link."outwardIssue"."key"
					} elseif ($Link."inwardIssue") {
						$Result += $Link."type"."inward" + " " + $Link."inwardIssue"."key"
					}
				}
				if ($Result.Length -gt 2) {
					$Result = $Result.Substring(2)
				}
				break
			}
			"issuetype" {
				$Result = $FieldValue."name"
				break
			}
			"number" {
				$Result = $FieldValue
				break
			}
			"option" {
				$Result = $FieldValue."value"
				break
			}
			"option-with-child" {
				$Result = $FieldValue."value"
				if ($FieldValue."child") {
					$Result += " | " + $FieldValue."child"."value"
				}
				# Note: Jira Cloud only supports 2-level cascading list... so no need for recursion.
				break
			}
			"priority" {
				$Result = $FieldValue."name"
				break
			}
			"progress" {
				$Result = $FieldValue."progress".ToString() + "/" + $FieldValue."total".ToString()
				break
			}			
			"project" {
				$Result = $FieldValue."name"
				break
			}
			"resolution" {
				$Result = $FieldValue."name"
				break
			}
			"sd-request-lang" {
				$Result = $FieldValue."displayName"
				break
			}
			"sd-servicelevelagreement" {
				if ($FieldValue."completedCycles") {
					foreach ($Cycle in $FieldValue."completedCycles") {
						$Result += $NL
						$StartTime = ParseDateTime $Cycle."startTime"."jira"
						$StopTime = ParseDateTime $Cycle."stopTime"."jira"
						$BreachTime = ParseDateTime $Cycle."breachTime"."jira"
						$Breached = $Cycle."breached"
						$Goal = $Cycle."goalDuration"."friendly"
						$Elapsed = $Cycle."elapsedTime"."friendly"
						$Remaining = $Cycle."remainingTime"."friendly"
						$Result += 	"Started:" + $StartTime + ",Stopped:" + $StopTime + ",Elapsed:" + $Elapsed + ",Remaining:" + $Remaining + ",Breach:" + $BreachTime
						if ($Breached) {
							$Result += ",Breached"
						}
					}
				}
				if ($FieldValue."ongoingCycle") {
					$Result += $NL
					$StartTime = ParseDateTime $FieldValue."ongoingCycle"."startTime"."jira"
					$BreachTime = ParseDateTime $FieldValue."ongoingCycle"."breachTime"."jira"
					$Breached = $FieldValue."breached"
					$Goal = $FieldValue."goalDuration"."friendly"
					$Elapsed = $FieldValue."elapsedTime"."friendly"
					$Remaining = $FieldValue."remainingTime"."friendly"
					$Result += "Started:" + $StartTime + ",Elapsed:" + $Elapsed + ",Remaining:" + $Remaining + ",Breach:" + $BreachTime
					if ($Breached) {
						$Result += " [Breached]"
					}
				}
				if ($Result.Length -gt 2) {
					$Result = $Result.Substring(2)
				}
				break
			}
			"status" {
				$Result = $FieldValue."name"
				break
			}
			"string" {
				# Special handling for paragraph types
				if ($FieldValue."content") {
					try {
						$Result = ParseContent $FieldValue
					} catch {
						$Result = $PSItem
					}
				} else {
					# Simple string
					$Result = $FieldValue
				}
				break
			}
			"timetracking" {
				$Result = "Spent:" + $FieldValue."timeSpent" + ",Remaining:" + $FieldValue."remainingEstimate"
				break
			}
			"user" {
				if ($global:IsServer) {
					$Result = $FieldValue."displayName" + " [" + $FieldValue."key" + ", " + $FieldValue."emailAddress" + "]"
				} else {
					$Result = $FieldValue."displayName" + " [" + $FieldValue."accountId" + "]"
				}
				break
			}
			"version" {
				$Result = $FieldValue."version"
				break
			}
			"votes" {
				$Response = WebRequest $FieldValue."self" "GET" $Headers 
				if ($Response.StatusCode -eq 200) {
					$Json = $Response.Content | ConvertFrom-Json
					foreach ($Watcher in $Json.voters) {
						$Result += "," + $Watcher.displayName + " [" + $Watcher.accountId + "]"
					}
					if ($Result.Length -gt 1) {
						$Result = $Result.Substring(1)
					}
				} else {
					$Result = "Error connecting to " + $FieldValue."self"
				}
				break
			}
			"watches" {
				$Response = WebRequest $FieldValue."self" "GET" $Headers 
				if ($Response.StatusCode -eq 200) {
					$Json = $Response.Content | ConvertFrom-Json
					foreach ($Watcher in $Json.watchers) {
						$Result += "," + $Watcher.displayName + " [" + $Watcher.accountId + "]"
					}
					if ($Result.Length -gt 1) {
						$Result = $Result.Substring(1)
					}
				} else {
					$Result = "Error connecting to " + $FieldValue."self"
				}
				break
			}
			default {
				$Result = $FieldValue
				break
			}
			# Unhandled types
			<#
			"" 
			"any" 
			"comments-page" 
			"component" 
			"issuerestriction" 
			"json"
			"object" 
			"sd-approvals"
			"sd-customerorganization" 
			"sd-customerrequesttype" 
			"sd-feedback" 
			"securitylevel" 
			"service-entity-field" 
			"worklog" 
			#>
		}
	}
	$Result
}

function WriteCSVEntry {
	param (
		[hashtable] $Headers,
		[string] $Csv,
		[PSCustomObject] $Names,
		[PSCustomObject] $Issue
	)
	if ($global:IsServer) {
		$FieldInfo = $ServerFieldInfo
	} else {
		$FieldInfo = $CloudFieldInfo
	}
	$Line = ""
	foreach ($Item in ($Names.PSObject.Properties | Sort-Object)) {
		$FieldId = $Item.Name
		if ($ParseFields) {
			$Type = $FieldInfo."$FieldId".schema.type
			$Items = $FieldInfo."$FieldId".schema.items
		} else {
			$Type = $null
			$Items = $null
		}
		if ($Issue.fields."$FieldId") {
			$FieldValue = $Issue.fields."$FieldId"
		} else {
			# Certain fields like "key" are at issue level
			$FieldValue = $Issue."$FieldId"
		}
		$ParsedValue = ParseFieldValue $Headers $Type $Items $FieldValue
		# Escape double quotes
		$CSVValue = $ParsedValue -replace '"', '""'
		#Write-Output "${FieldId}.${Type}.${Items}=`"${CSVValue}`""
		$Line += ",`"${CSVValue}`""
	}
	$Line = $Issue.key + $Line
	Add-Content -Path $Csv -Value $Line
}

function GetAuthHeader {
	[hashtable] $Headers = @{
		"Content-Type" = "application/json"
	}
	$Auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($User + ":" + $Pass))
	$Headers.Authorization = "Basic " + $Auth
	$Headers
}

function WriteReport {
	param (
		[hashtable] $Headers,
		[string] $MainJql,
		[string] $ExtraJql,
		[string[]] $FieldList,
		[string] $Out
	)
	$StartTime = $(Get-Date)
	try {
		[int] $Start = 0
		[int] $Total = 0
		[int] $IssueCount = 0
		[string] $NextPageToken = $null
		$FieldNames = $FieldList -join ","
		if ($ExtraJql) {
			$FinalJQL = "(" + $ExtraJql + ")"
			if ($MainJql) {
				$FinalJQL += " and " + $MainJql
			}
		} else {
			$FinalJQL = $MainJql
		}
		Write-Host
		Write-Host "JQL: $FinalJQL"
		Write-Host "Fields: $FieldNames"
		Write-Host "Output: ${Out}"
		Write-Host
		$WriteHeader = $True
		$HasNextPage = $False
		do {
			$Json = SearchIssue $Headers $FinalJQL $FieldList $MaxResults $Start $NextPageToken
			$Count = $Json.issues.Count
			if ($global:IsServer) {
				$Total = $Json.total
				$Start += $Count
				$HasNextPage = $Start -lt $Total
			} else {
				$Total += $Count
				$NextPageToken = $Json.nextPageToken
				if ($NextPageToken) {
					$HasNextPage = $True
				} else {
					$HasNextPage = $False
				}
			}
			if ($Total -ne 0) {
				Write-Progress -Id 1 -Activity "Processing issue ${IssueCount}/${Total}" -PercentComplete ($IssueCount / $Total * 100)
			} else {
				Write-Progress -Id 1 -Activity "Processing issue ${IssueCount}/${Total}" -PercentComplete 1
			}
			if ($WriteHeader) {
				WriteCSVHeader $Out $Json.names
				$WriteHeader = $false
			}
			# Save data to CSV
			foreach ($Issue in $Json.issues) {
				$IssueKey = $Issue.key
				WriteCSVEntry $Headers $Out $Json.names $Issue
				$IssueCount++
				Write-Progress -Id 1 -Activity "Processing issue ${IssueCount}/${Total}" -PercentComplete ($IssueCount / $Total * 100)
			}
		} while ($HasNextPage)
		Write-Progress -Id 1 -Activity "Processed ${Total} issue(s)" -Completed
		Write-Host "${IssueCount} issue(s) written to ${Out}"
		$EndTime = $(Get-Date)
		$ElapsedTime = $EndTime - $StartTime
		$TotalTime = "{0:HH:mm:ss}" -f ([datetime] $ElapsedTime.Ticks)
		Write-Host "From: ${StartTime} To: ${EndTime} Elapsed: ${TotalTime}"
	} catch {
		Write-Host $PSItem
	}
}

function CheckCredential {
	if ($global:IsServer) {
		$global:Domain = $ServerDomain
		$User = $ServerUser
		$Pass = $ServerPass
	} else {
		$global:Domain = $CloudDomain
		$User = $CloudUser
		$Pass = $CloudPass
	}
	if ((-not $Domain) -or (-not $User) -or (-not $Pass)) {
		Write-Host "Please provide domain, user/email and password/API token"
		$null
	} else {
		GetAuthHeader
	}
}

# Query mode
if ($Query) {
	if ($ServerDomain) {
		$global:IsServer = $True
		if (-not $ServerPass) {
			$pwd = Read-Host "Enter Password" -AsSecureString
			$ServerPass = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($pwd))
		}
	} else {
		$global:IsServer = $False
		if (-not $CloudPass) {
			$pwd = Read-Host "Enter API token" -AsSecureString
			$CloudPass = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($pwd))
		}
	}
	$ExportDate = Get-Date -Format yyyyMMddHHmmss
	$Out = "${ExportDate}.csv"
	$Headers = CheckCredential
	if ($Headers) {
		RefreshFields $Headers $False
		WriteReport $Headers $Jql $DateRange $Fields $Out
	}
	Exit
}

# Interactive mode
$Quit = $false
do {
	$Cred = @{}
	if ($global:IsServer) {
		$Cred = $ServerCred
		$FieldMap = $ServerFieldMap
	} else {
		$Cred = $CloudCred
		$FieldMap = $CloudFieldMap
	}
	Clear-Host
	$FieldCount = $Fields.Count
	Write-Host ================================================================================
	Write-Host Generate Report
	Write-Host --------------------------------------------------------------------------------
	if ($global:IsServer) {
		Write-Host "[M] Mode             | Server/Data Center"
		Write-Host "[D] Domain           |" $ServerDomain
		Write-Host "[U] User             |" $ServerUser
		if ($ServerPass) {
			Write-Host "[P] Password         | Provided"
		} else {
			Write-Host "[P] Password         | Not Provided"
		}
	} else {
		Write-Host "[M] Mode             | Cloud"
		Write-Host "[D] Domain           |" $CloudDomain
		Write-Host "[U] User             |" $CloudUser
		if ($CloudPass) {
			Write-Host "[P] Password         | Provided"
		} else {
			Write-Host "[P] Password         | Not Provided"
		}
	}
	Write-Host --------------------------------------------------------------------------------
	Write-Host "[J] JQL              | ${Jql}"
	Write-Host "[T] Date Range       | ${DateRange}"
	Write-Host "[F] Output Fields    | ${FieldCount} Field(s)"
	foreach ($Field in $Fields) {
		if ($FieldMap.Contains($Field)) {
			Write-Host "                     | " $FieldMap[$Field].Name " (" $Field ")"
		} else {
			Write-Host "                     | " $Field
		}
	}	
	Write-Host "[L] Max Results      | ${MaxResults}"
	Write-Host "[R] Parse Fields     | ${ParseFields}"
	Write-Host --------------------------------------------------------------------------------
	Write-Host "[S] Search           |"
	Write-Host "[X] Exit             |"
	Write-Host ================================================================================
	$Option = Read-Host Option
	try {
		switch ($Option) {
			"t" {
				Clear-Host
				Write-Host ================================================================================
				Write-Host "Current JQL | ${Jql}"
				Write-Host --------------------------------------------------------------------------------
				Write-Host "Examples"
				Write-Host --------------------------------------------------------------------------------
				Write-Host "No limit                                | Empty string"
				Write-Host "Created within 5 days                   | Created > -5d"
				Write-Host "Updated within 10 days                  | Updated > -10d"
				Write-Host "Expected Deadline is on or before today | `"Expected Deadline`" <= 0d"
				Write-Host "Planned Start Date is a specific date   | `"Planned Start Date`" = 2023-11-01"
				Write-Host "Actual Start Date is within range       | `"Actual Start Date`" >= 2023-11-01 and `"Actual Start Date`" <= 2023-12-01"
				Write-Host ================================================================================
				$DateRange = Read-Host "Date Range JQL"
				break
			}
			"l" {
				$MaxResults = Read-Host "Max Results"
				break
			}
			"m" {
				$global:IsServer = -not $global:IsServer
				break
			}
			"d" {
				Write-Host "Examples: "
				Write-Host "	http://localhost:8080" 
				Write-Host "	https://kcwong.atlassian.net"
				Write-Host "	https://jira-plike.pccwglobal.com"
				Write-Host "	https://consoleconnect-sandbox-824.atlassian.net"
				$Domain = Read-Host "Domain"
				if ($global:IsServer) {
					$ServerDomain = $Domain
				} else {
					$CloudDomain = $Domain
				}
				break
			}
			"u" {
				if ($global:IsServer) {
					$User = Read-Host "Username"
					$ServerUser = $User
				} else {
					$User = Read-Host "Email"
					$CloudUser = $User
				}
				break
			}
			"p" {
				if ($global:IsServer) {
					$pwd = Read-Host "Enter Password" -AsSecureString
					$ServerPass = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($pwd))
				} else {
					$pwd = Read-Host "Enter API token" -AsSecureString
					$CloudPass = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($pwd))
				}
				break
			}
			"j" {
				$Jql = Read-Host JQL
				break
			}
			"f" {
				$Headers = CheckCredential
				if ($Headers) {
					$Fields = SelectFields $Headers $Fields
				} else {
					AnyKeyToContinue
				}
				break
			}
			"r" {
				$ParseFields = -not $ParseFields
				break
			}
			"s" {
				$Headers = CheckCredential
				if ($Headers) {
					RefreshFields $Headers $False
					if (-not $Jql) {
						Write-Host "Please provide JQL"
					} else {
						$ExportDate = Get-Date -Format yyyyMMddHHmmss
						$Out = "${ExportDate}.csv"
						WriteReport $Headers $Jql $DateRange $Fields $Out
					}
				}
				AnyKeyToContinue
				break
			}
			"x" {
				$Quit = $true
				break
			}
		}
	} catch {
		Write-Host $_
		AnyKeyToContinue
	}
} while (-not $Quit)