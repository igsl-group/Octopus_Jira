package com.igsl.configmigration;

/**
 * Pair of uniqueKey in export/import DTOStore.
 */
public class KeyGuide {
	public String exportUniqueKey;
	public String importUniqueKey;
	
	public String getExportUniqueKey() {
		return exportUniqueKey;
	}
	public void setExportUniqueKey(String exportUniqueKey) {
		this.exportUniqueKey = exportUniqueKey;
	}
	public String getImportUniqueKey() {
		return importUniqueKey;
	}
	public void setImportUniqueKey(String importUniqueKey) {
		this.importUniqueKey = importUniqueKey;
	}
}
