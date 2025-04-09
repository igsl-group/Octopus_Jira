package com.igsl.customfieldtypes.generictable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class GenericTableSettings {
	
	private static final Logger LOGGER = Logger.getLogger(GenericTableSettings.class);
	
	private static final String SETTINGS_GENERIC_TABLE = "genericTable_";
	
	/**
	 * Form field name in edit-config.vm.
	 */
	private static final String PARAM_JS_TOGGLE_EMPTY = "jsToggleEmpty";
	private static final String PARAM_JS_ADD_ROW = "jsAddRow";
	private static final String PARAM_JS_DELETE_ROW = "jsDelRow";
	private static final String PARAM_JS_SAVE = "jsSave";
	private static final String PARAM_JS_LOAD = "jsLoad";
	private static final String PARAM_JS_VALIDATION = "jsValidation";
	private static final String PARAM_STYLE = "style";
	private static final String PARAM_TABLE = "table";
	private static final String PARAM_ROW_TEMPLATE = "rowTemplate";
	
	private static final String CLASS_NO_DIRTY_WARNING = "ajs-dirty-warning-exempt";
	
	private static final String CUSTOM_FIELD_ID_PLACEHOLDER = "_CF_";
	private static final String READ_ONLY_PLACEHOLDER = "_RO_";
	private static final String HIDE_PLACEHOLDER = "_HIDE_";
	private static final String READ_ONLY_CLASS_PLACEHOLDER = "_ROCLASS_";
	
	private String customFieldId;
	private String customFieldName;
	private String jsToggleEmpty = 
			  "// JavaScript to show/hide no record row\r\n"
			+ "function _CF_toggleEmpty(show) {\r\n"
			+ "    if (show) {\r\n"
			+ "        $('#_CF_empty').css({display: 'table-row'});\r\n"
			+ "    } else {\r\n"
			+ "        $('#_CF_empty').css({display: 'none'});\r\n"
			+ "    }\r\n"
			+ "}\r\n";
	private String jsAddRow = 
			  "// JavaScript to add a row\r\n"
			+ "function _CF_addRow() {\r\n"
			+ "    var tbody = document.getElementById('_CF_tbody');\r\n"
			+ "    var template = document.getElementById('_CF_template');\r\n"
			+ "    var clon = template.content.cloneNode(true);\r\n"
			+ "    tbody.append(clon);\r\n"
			+ "    _CF_toggleEmpty(false);\r\n"
			+ "}\r\n";
	private String jsDeleteRow = 
			  "// JavaScript to delete a row\r\n"
			+ "function _CF_delRow(tableRow) {\r\n"
			+ "    tableRow.remove();\r\n"
			+ "    _CF_save();\r\n"
			+ "    if ($('#_CF_tbody').find('tr').length == 0) {\r\n"
			+ "        _CF_toggleEmpty(true);\r\n"
			+ "    }\r\n"
			+ "}\r\n";
	private String jsSave = 
			  "// JavaScript to update JSON value\r\n"
			+ "function _CF_save() {\r\n"
			+ "    // Example\r\n"
			+ "    var json = {};\r\n"
			+ "    json['data'] = [];\r\n"
			+ "    $('#_CF_tbody').find('tr').each(function(){\r\n"
			+ "        var map = {};\r\n"
			+ "        map['Field1'] = $(this).find('input.Field1').val();\r\n"
			+ "        map['Field2'] = $(this).find('input.Field2').val();\r\n"
			+ "        map['Field3'] = $(this).find('input.Field3').val();\r\n"
			+ "        json['data'].push(map);\r\n"
			+ "    });\r\n"
			+ "    $('#_CF_').val(JSON.stringify(json));\r\n"
			+ "    _CF_validate();\r\n"
			+ "}\r\n";
	private String jsLoad = 
			  "// JavaScript to set field value\r\n" 
			+ "function _CF_load() {\r\n"
			+ "    // Example\r\n"
			+ "    $('#_CF_tbody').empty();\r\n"
			+ "    var json = JSON.parse($('#_CF_').val());\r\n"
			+ "    for (var idx in json['data']) {\r\n"
			+ "        _CF_addRow();\r\n"
			+ "        var row = $('#_CF_tbody').find('tr:last');\r\n"
			+ "        $(row).find('input.Field1').val(json['data'][idx]['Field1']);\r\n"
			+ "        $(row).find('input.Field2').val(json['data'][idx]['Field2']);\r\n"
			+ "        $(row).find('input.Field3').val(json['data'][idx]['Field3']);\r\n"
			+ "    }\r\n"
			+ "    if ($('#_CF_tbody').find('tr').length == 0) {\r\n"
			+ "        _CF_toggleEmpty(true);\r\n"
			+ "    } else {\r\n"
			+ "        _CF_toggleEmpty(false);\r\n"
			+ "    }\r\n"
			+ "}\r\n";
	private String jsValidation = 
			  "// JavaScript to validate fields\r\n"
			+ "function _CF_validate() {\r\n"
			+ "    // Example\r\n"
			+ "    var rows = $('#_CF_tbody').find('tr');\r\n"
			+ "    for (var idx in rows) {\r\n"
			+ "        var row = rows[idx];\r\n"
			+ "        var field1 = $(row).find('input.Field1')[0];\r\n"
			+ "        field1.setCustomValidity('');\r\n"
			+ "        var field2 = $(row).find('input.Field2')[0];\r\n"
			+ "        field2.setCustomValidity('');\r\n"
			+ "        var field3 = $(row).find('input.Field3')[0];\r\n"
			+ "        field3.setCustomValidity('');\r\n"
			+ "        if (field2.val() > field3.val()) {\r\n"
			+ "            field2.setCustomValidity('Field2 must be less than or equal to Field3');\r\n"
			+ "            field3.setCustomValidity('Field3 must be larger than or equal to Field2');\r\n"
			+ "        }\r\n"
			+ "    }\r\n"
		    + "}\r\n";
	private String style = 
			  "table._CF_, table._CF_ tr, table._CF_ th, table._CF_ td {\r\n"
			+ "    border: 1px solid;\r\n"
			+ "    border-collapse: collapse;\r\n"
			+ "    padding: 2px;\r\n"
			+ "}\r\n"
			+ "table._CF_ input {\r\n"
			+ "    width: 90% !important;\r\n"
			+ "}\r\n"			
			+ "table._CF_ {\r\n"
			+ "    width: 100%\r\n"
			+ "}\r\n"
			+ "input:invalid, textarea:invalid {\r\n"
			+ "     border: 2px solid red;\r\n"
			+ "     background-color: beige;\r\n"
			+ "}\r\n";
	private String table = 
			  "<!-- HTML table with an empty tbody with an id -->\r\n"
			+ "<table class='_CF_'>\r\n"
			+ "    <thead>\r\n" 
			+ "        <th _HIDE_></th>\r\n"
			+ "        <th>Field 1</th>\r\n" 
			+ "        <th>Field 2</th>\r\n" 
			+ "        <th>Field 3</th>\r\n" 
			+ "    </thead>\r\n"
			+ "    <tbody id='_CF_tbody'></tbody>\r\n"
			+ "    <tfoot>\r\n"
			+ "        <tr id='_CF_empty'>\r\n"
			+ "            <td colspan='100%'>No record(s)</td>\r\n"
			+ "        </tr>\r\n"
			+ "        <tr _HIDE_>\r\n"
			+ "	           <td colspan='100%'>\r\n" 
			+ "                <button type='button' onclick='_CF_addRow()'>\r\n"
			+ "                    <span class='aui-icon aui-icon-small aui-iconfont-add'></span> Add\r\n"
			+ "                </button>\r\n"
			+ "            </td>\r\n"
			+ "        </tr>\r\n" 
			+ "    </tfoot>\r\n" 
			+ "</table>\r\n";
	private String rowTemplate = 
			  "<template id='_CF_template'>\r\n" 
			+ "    <tr>\r\n"
			+ "        <td _HIDE_>\r\n" 
			+ "            <button type='button' onclick='_CF_delRow(this.parentNode.parentNode)'>\r\n"
			+ "                <span class='aui-icon aui-icon-small aui-iconfont-close-dialog'></span>\r\n"
			+ "            </button>\r\n"
			+ "        </td>\r\n"
			+ "        <td><input _RO_ class='_ROCLASS_ Field1' type='text' value='' onkeyup='_CF_save()'/></td>\r\n" 
			+ "        <td><input _RO_ class='_ROCLASS_ Field2' type='number' min='0' max='99' step='1' value='' onkeyup='_CF_save()'/></td>\r\n" 
			+ "        <td><input _RO_ class='_ROCLASS_ Field3' type='number' min='0' max='99' step='1' value='' onkeyup='_CF_save()'/></td>\r\n" 
			+ "    </tr>\r\n"
			+ "</template>\r\n";
	
	public void saveSettings() {
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		String result = null;
		result = new Gson().toJson(this);
		LOGGER.debug("saveSettings: " + SETTINGS_GENERIC_TABLE + customFieldId + " = " + result);
		settings.put(SETTINGS_GENERIC_TABLE + customFieldId, result);
	}
	
	public static GenericTableSettings getSettings(CustomField customField) {
		return getSettings(customField.getId(), customField.getName());
	}	
		
	public static GenericTableSettings getSettings(String fieldId, String fieldName) {
		GenericTableSettings result = null;
		PluginSettingsFactory factory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		PluginSettings settings = factory.createGlobalSettings();
		Object o = settings.get(SETTINGS_GENERIC_TABLE + fieldId);
		if (o != null) {
			try {
				result = new Gson().fromJson(String.valueOf(o), GenericTableSettings.class);
			} catch (JsonSyntaxException jsex) {
				LOGGER.error("Settings is in invalid format, settings ignored", jsex);
				result = new GenericTableSettings();
				result.setCustomFieldId(fieldId);
				result.setCustomFieldName(fieldName);
			}
		} else {
			result = new GenericTableSettings();
			result.setCustomFieldId(fieldId);
			result.setCustomFieldName(fieldName);
		}
		return result;
	}
	
	public static GenericTableSettings parseParameters(HttpServletRequest req) {
		GenericTableSettings result = new GenericTableSettings();
		if (req != null) {
			String jsToggleEmpty = req.getParameter(PARAM_JS_TOGGLE_EMPTY);
			result.setJsToggleEmpty(jsToggleEmpty);
			String jsAddRow = req.getParameter(PARAM_JS_ADD_ROW);
			result.setJsAddRow(jsAddRow);
			String jsDeleteRow = req.getParameter(PARAM_JS_DELETE_ROW);
			result.setJsDeleteRow(jsDeleteRow);
			String jsSave = req.getParameter(PARAM_JS_SAVE);
			result.setJsSave(jsSave);
			String jsLoad = req.getParameter(PARAM_JS_LOAD);
			String jsValidation = req.getParameter(PARAM_JS_VALIDATION);
			result.setJsValidation(jsValidation);
			String style = req.getParameter(PARAM_STYLE);
			result.setStyle(style);
			result.setJsLoad(jsLoad);
			String table = req.getParameter(PARAM_TABLE);
			result.setTable(table);
			String rowTemplate = req.getParameter(PARAM_ROW_TEMPLATE);
			result.setRowTemplate(rowTemplate);
		}
		LOGGER.debug("Parsed: " + (new Gson().toJson(result)));
		return result;
	}

	private static String getJSFunctionName(String func) {
		Pattern pattern = Pattern.compile(".*function\\s+([^(]+)\\(");
		Matcher matcher = pattern.matcher(func);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
	
	private String replacePlaceHolder(String s, boolean readOnly) {
		if (readOnly) {
			s = s.replaceAll(READ_ONLY_PLACEHOLDER, "readonly='readonly'");
			s = s.replaceAll(HIDE_PLACEHOLDER, "style='display: none'");
		} else {
			s = s.replaceAll(READ_ONLY_PLACEHOLDER, "");
			s = s.replaceAll(HIDE_PLACEHOLDER, "");
		}
		s = s.replaceAll(CUSTOM_FIELD_ID_PLACEHOLDER, 
				getCustomFieldId() + (readOnly? "_VIEW_" : ""));
		s = s.replaceAll(READ_ONLY_CLASS_PLACEHOLDER, 
				(readOnly? CLASS_NO_DIRTY_WARNING : CLASS_NO_DIRTY_WARNING));
		return s;
	}
	
	/**
	 * Return concatenation of all scripts, with placeholder replaced
	 */
	public String getScript() {
		return getScript(false);
	}
	public String getScript(boolean readOnly) {
		StringBuilder sb = new StringBuilder();
		if (!readOnly) {
			sb.append(getJsValidation());
			sb.append(getJsDeleteRow());
			sb.append(getJsSave());
		}
		sb.append(getJsAddRow());
		sb.append(getJsToggleEmpty());
		sb.append(getJsLoad());
		String script = sb.toString();
		script = replacePlaceHolder(script, readOnly);
		return script;
	}

	/**
	 * Return substitubuted style
	 * @return
	 */
	public String getStyleContent() {
		return replacePlaceHolder(getStyle(), false);	// readOnly does not matter here
	}
	
	public String getInitScript() {
		return getInitScript(false);
	}
	public String getInitScript(boolean readOnly) {
		String funcName = getJSFunctionName(getJsLoad());
		if (funcName != null) {
			funcName = replacePlaceHolder(funcName, readOnly);
			return funcName + "();";
		}
		return "";
	}
	
	public String getHiddenField(String value) {
		return getHiddenField(value, false, false);
	}
	public String getHiddenField(String value, boolean readOnly) {
		return getHiddenField(value, readOnly, false);
	}
	public String getHiddenField(String value, boolean readOnly, boolean show) {
		// To enable inline edit, the value field needs to be non-hidden
		StringBuilder sb = new StringBuilder();
		sb.append("<input type='" + (show? "text" : "hidden") + "' " + 
					"value='" + value + "' name='_CF_'" + 
					"id='_CF_'/>");
		String result = replacePlaceHolder(sb.toString(), readOnly);
		return result;
	}
	
	/**
	 * Return table and row template
	 * @return
	 */
	public String getHtml() {
		return getHtml(false);
	}
	public String getHtml(boolean readOnly) {
		StringBuilder sb = new StringBuilder();
		sb.append(getTable());
		sb.append(getRowTemplate());
		String html = sb.toString();
		html = replacePlaceHolder(html, readOnly);
		return html;
	}
	
	public String getCustomFieldId() {
		return customFieldId;
	}

	public void setCustomFieldId(String customFieldId) {
		this.customFieldId = customFieldId;
	}

	public String getCustomFieldName() {
		return customFieldName;
	}

	public void setCustomFieldName(String customFieldName) {
		this.customFieldName = customFieldName;
	}

	public String getJsAddRow() {
		return jsAddRow;
	}

	public void setJsAddRow(String jsAddRow) {
		this.jsAddRow = jsAddRow;
	}

	public String getJsDeleteRow() {
		return jsDeleteRow;
	}

	public void setJsDeleteRow(String jsDeleteRow) {
		this.jsDeleteRow = jsDeleteRow;
	}

	public String getJsSave() {
		return jsSave;
	}

	public void setJsSave(String jsSave) {
		this.jsSave = jsSave;
	}

	public String getJsLoad() {
		return jsLoad;
	}

	public void setJsLoad(String jsLoad) {
		this.jsLoad = jsLoad;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getRowTemplate() {
		return rowTemplate;
	}

	public void setRowTemplate(String rowTemplate) {
		this.rowTemplate = rowTemplate;
	}

	public String getJsToggleEmpty() {
		return jsToggleEmpty;
	}

	public void setJsToggleEmpty(String jsToggleEmpty) {
		this.jsToggleEmpty = jsToggleEmpty;
	}

	public String getJsValidation() {
		return jsValidation;
	}

	public void setJsValidation(String jsValidation) {
		this.jsValidation = jsValidation;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}
}
