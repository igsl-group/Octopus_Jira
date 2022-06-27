#Jira Custom Field Types for Octopus

##List of Custom Field Types
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

### Inline Edit Mode
TODO

### Jira Service Management Compatibility
TODO
