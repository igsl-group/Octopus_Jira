package com.igsl.customfieldtypes;

import java.util.Locale;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;

public class I18nResource {
	private static I18nHelper i18n = ComponentAccessor.getI18nHelperFactory().getInstance(Locale.getDefault());

	// General
	public static final String NO_BULK_EDIT = "CustomFieldTypes.noBulkEdit";
	public static final String NO_DATA = "CustomFieldTypes.noData";
	public static final String NO_IMAGE = "CustomFieldTypes.noImage";
	
	// StaticImage
	public static final String STATICIMAGE_NAME = "StaticImage.name";
	
	// GenericTable
	public static final String GENERICTABLE_CONFIG_NAME = "GenericTable.config.name";
	public static final String GENERICTABLE_CONFIG_TITLE = "GenericTable.config.title";
	public static final String GENERICTABLE_DELETE_BUTTON = "GenericTable.deleteButton";
	public static final String GENERICTABLE_ADD_BUTTON = "GenericTable.addButton";
	
	// EffortTable
	public static final String EFFORTTABLE_NAME = "EffortTable.name";
	public static final String EFFORTTABLE_TASK_LABEL = "EffortTable.task.label";
	public static final String EFFORTTABLE_TASK_HELPTEXT = "EffortTable.task.helpText";
	public static final String EFFORTTABLE_HEADCOUNTDAY_LABEL = "EffortTable.headCountDay.label";
	public static final String EFFORTTABLE_HEADCOUNTDAY_HELPTEXT = "EffortTable.headCountDay.helpText";
	public static final String EFFORTTABLE_TOTALHEADCOUNTDAY_LABEL = "EffortTable.totalHeadCountDay.label";
	public static final String EFFORTTABLE_EXPENSES_LABEL = "EffortTable.expenses.label";
	public static final String EFFORTTABLE_EXPENSES_HELPTEXT = "EffortTable.expenses.helpText";
	public static final String EFFORTTABLE_DELETE_BUTTON = "EffortTable.deleteButton";
	public static final String EFFORTTABLE_ADD_BUTTON = "EffortTable.addButton";
	public static final String EFFORTTABLE_CURRENCY_SYMBOL = "EffortTable.currencySymbol";
	public static final String EFFORTTABLE_CONFIG_NAME = "EffortTable.config.name";
	public static final String EFFORTTABLE_CONFIG_TITLE = "EffortTable.config.title";
	
	// ProductPackingNote
	public static final String PRODUCTPACKINGNOTE_NAME = "ProductPackingNote.name";
	public static final String PRODUCTPACKINGNOTE_NAME_LABEL = "ProductPackingNote.name.label";
	public static final String PRODUCTPACKINGNOTE_NAME_HELPTEXT = "ProductPackingNote.name.helpText";
	public static final String PRODUCTPACKINGNOTE_VERSION_LABEL = "ProductPackingNote.version.label";
	public static final String PRODUCTPACKINGNOTE_VERSION_HELPTEXT = "ProductPackingNote.version.helpText";
	public static final String PRODUCTPACKINGNOTE_MD5SIGNATURE_LABEL = "ProductPackingNote.md5Signature.label";
	public static final String PRODUCTPACKINGNOTE_MD5SIGNATURE_HELPTEXT = "ProductPackingNote.md5Signature.helpText";
	public static final String PRODUCTPACKINGNOTE_DELETE_BUTTON = "ProductPackingNote.deleteButton";
	public static final String PRODUCTPACKINGNOTE_ADD_BUTTON = "ProductPackingNote.addButton";
	
	// ChangeRequest
	public static final String CHANGEREQUEST_NAME = "ChangeRequest.name";
	public static final String CHANGEREQUEST_ISSUEID_LABEL = "ChangeRequest.issueId.label";
	public static final String CHANGEREQUEST_ISSUEID_HELPTEXT = "ChangeRequest.issueId.helpText";
	public static final String CHANGEREQUEST_PROJECTMANAGER_LABEL = "ChangeRequest.projectManager.label";
	public static final String CHANGEREQUEST_PROJECTMANAGER_HELPTEXT = "ChangeRequest.projectManager.helpText";
	public static final String CHANGEREQUEST_DELETE_BUTTON = "ChangeRequest.deleteButton";
	public static final String CHANGEREQUEST_ADD_BUTTON = "ChangeRequest.addButton";
	
	// URLField
	public static final String URLFIELD_NAME = "URLField.name";
	public static final String URLFIELD_URL_LABEL = "URLField.url.label";
	public static final String URLFIELD_URL_HELPTEXT = "URLField.url.helpText";
	public static final String URLFIELD_DISPLAYTEXT_LABEL = "URLField.displayText.label";
	public static final String URLFIELD_DISPLAYTEXT_HELPTEXT = "URLField.displayText.helpText";
	public static final String URLFIELD_DELETE_BUTTON = "URLField.deleteButton";
	public static final String URLFIELD_ADD_BUTTON = "URLField.addButton";
	public static final String URLFIELD_CONFIG_NAME = "URLField.config.name";
	public static final String URLFIELD_CONFIG_TITLE = "URLField.config.title";
	
	public static String getText(String key, Object... param) {
		return i18n.getText(key, param);
	}
}
