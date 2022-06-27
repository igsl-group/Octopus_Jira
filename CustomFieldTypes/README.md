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

### Jira Service Management Compatibility
#### Compatibility Issues
1. Jira Service Management is another beast entirely when compared to Jira Core. 
1. Unlike Jira Core, Service Management only supports a few specific types of custom fields. Namely,
	* Checkboxes
	* Date pickers
	* Date time pickers
	* Labels
	* Number fields
	* Radio Buttons
	* Select lists
	* Text fields
	* URL fields
	* User pickers
	* Reference: https://confluence.atlassian.com/jirakb/list-of-supported-custom-fields-for-request-types-in-jira-service-management-customer-portals-867182673.html
1. Speaking in API, it only supports custom fields that extends GenericTextCFType.
1. It does not loads the view/edit template of custom fields. Instead it renders them as a text label/field. 
#### Workaround
1. Custom fields to be used in Service Management must extend GenericTextCFType.
1. You still need to implement view/edit template for use in Jira Core.
1. Inject a JavaScript via web-resource in atlassian-plugin.xml. This JavaScript should be applied to the following contexts to cover all bases: 
    * atl.general
    * atl.admin
    * customerportal
    * servicedesk.general
    * servicedesk.admin
1. Service Management can display the custom fields in a standalone page, or inside an iFrame. The JavaScript needs to check for both to detect if a custom field is being displayed inside customer portal.
1. When confirmed, the JavaScript then updates the HTML page: 
	* Hide the text field with id attribute of custom field ID. 
	* Adds controls to display the custom field content.
