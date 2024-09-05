use identityiq;

With
src AS (
	SELECT
		'I1' as issue,
		'{"history":{"Ticket Approval":{"JIRAUSER10100":{"approver":"JIRAUSER10100","delegated":null,"approved":true,"approvedDate":1686020040762},"JIRAUSER10116":{"approver":"JIRAUSER10116","delegated":null,"approved":true,"approvedDate":1686020044840}},"Test":{"U1":{"approver":"U1","delegated":null,"approved":false,"approvedDate":1686020040762},"U2":{"approver":"U2","delegated":"U1","approved":false,"approvedDate":1686020044840}}}}'
        as data
	UNION ALL
    SELECT
		'I2' as issue,
		'{"history":{"Ticket Approval 2":{"U3":{"approver":"U3","delegated":null,"approved":true,"approvedDate":1686020040762}},"Test 2":{"U4":{"approver":"U4","delegated":null,"approved":false,"approvedDate":1686020040762},"U5":{"approver":"U5","delegated":"U1","approved":false,"approvedDate":1686020044840}}}}'
        as data
),
ApprovalNames AS (
	SELECT src.issue, ApprovalNames.* FROM src 
    JOIN
	JSON_TABLE(JSON_KEYS(src.data, '$.history'), '$[*]' COLUMNS (ApprovalName VARCHAR(1000) PATH '$')) AS ApprovalNames 
),
ApproverNames AS (
	SELECT ApprovalNames.issue, ApprovalNames.ApprovalName, ApproverNames.* FROM src 
    JOIN ApprovalNames
    JOIN
    JSON_TABLE(JSON_KEYS(src.data, CONCAT('$.history."', ApprovalNames.ApprovalName, '"')), '$[*]' COLUMNS (ApproverName VARCHAR(1000) PATH '$')) AS ApproverNames
),
ApprovalRecord AS (
	SELECT
		ApproverNames.issue,
		ApproverNames.ApprovalName,
        ApproverNames.ApproverName,
        JSON_EXTRACT(src.data, CONCAT('$.history."', ApproverNames.ApprovalName, '"."', ApproverNames.ApproverName, '".approved')) AS Approved,
        JSON_UNQUOTE(JSON_EXTRACT(src.data, CONCAT('$.history."', ApproverNames.ApprovalName, '"."', ApproverNames.ApproverName, '".delegated'))) AS Delegator,
        FROM_UNIXTIME(JSON_EXTRACT(src.data, CONCAT('$.history."', ApproverNames.ApprovalName, '"."', ApproverNames.ApproverName, '".approvedDate')) / 1000) AS ApprovedDate
    FROM src
    JOIN ApproverNames ON src.issue = ApproverNames.issue
)
SELECT * FROM ApprovalRecord;

With 
MyTable AS (
	SELECT '{"map": {"key1": "value1","key2": "value2","key3": "value3"}}' as data
)
select j.key, json_unquote(json_extract(m.data, concat('$.map.', j.key))) as value from mytable as m
cross join 
json_table(json_keys(m.data, '$.map'), '$[*]' columns (`key` varchar(10) path '$')) as j