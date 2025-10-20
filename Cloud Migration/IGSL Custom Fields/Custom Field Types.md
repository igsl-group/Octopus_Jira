# ChangeRequestData
1. Long issueId is the only data.
1. It's a issue link custom field, migrate to linked work items instead.
1. Alternatively use plugins like https://marketplace.atlassian.com/apps/1227136/issue-picker-for-jira

# EffortTable
1. Rows of:
    1. String task
    1. String headCountDay (should be number)
1. String expenses (Table Grid NG does not support this, expenses will be present in each table row, plus a total)
1. String totalHeadCountDay (auto-calculated)
1. Can be replaced by Table Grid NG https://marketplace.atlassian.com/apps/1217571/table-grid-next-generation

# ProductPackingNote
1. Rows of: 
    1. String name
    1. String version
    1. String md5Signature
1. Can be replaced by Table Grid NG https://marketplace.atlassian.com/apps/1217571/table-grid-next-generation

# StaticImage
1. This stores a URL in field config and display it as image.
1. Replace with rich-text field with a default value, made read-only with ScriptRunner. With the new issue view, the location of the image cannot be fully controlled, it can only appear in the main panel.
1. Default markdown value: !ImageURL!
1. Alternatively use a paragraph field in Forms, but that is JSM only.

# URLField
1. List of:
    1. String url
    1. String displayText
1. Cloud has URL field, but it does not support display text. Stored data format is the URL.
1. Alternatively use paragraph field, but user can enter anything unless ScriptRunner validation is used. 