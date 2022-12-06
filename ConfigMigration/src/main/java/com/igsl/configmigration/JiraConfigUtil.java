package com.igsl.configmigration;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.SessionData.ImportData;

@JsonDeserialize(using=JiraConfigUtilDeserializer.class)
@JsonIgnoreProperties(value={"implementation"}, allowGetters=true)
public abstract class JiraConfigUtil {
	
	protected static final ObjectMapper OM = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	/**
	 * Return true if this JiraConfigUtil is to be included in user interface.
	 * Return false if the associated JiraConfigDTO is only referenced via other JiraConfigDTOs.
	 */
	@JsonIgnore
	public abstract boolean isVisible();
	
	/**
	 * Return implementation name. This is used to identify the JiraConfigUtil to be used.
	 * @return String
	 */
	public final String getImplementation() {
		return this.getClass().getCanonicalName();
	}
	
	/**
	 * Get display name of this JiraConfigUtil.
	 * @return
	 */
	@JsonIgnore
	public abstract String getName();
	
	/**
	 * Return associated DTO class.
	 */
	@JsonIgnore
	public abstract Class<? extends JiraConfigDTO> getDTOClass();
	
	/**
	 * Read all Jira objects in current environment and store them into JiraConfigItem.
	 * @param params Parameters. Depends on implementation.
	 * @throws Exception
	 */
	public abstract Map<String, JiraConfigDTO> findAll(Object... params) throws Exception;
	
	
	/**
	 * Find JiraConfigDTO by internal ID.
	 * @param id String
	 * @param params Parameters. Depends on implementation.
	 * @throws Exception
	 */
	public abstract JiraConfigDTO findByInternalId(String id, Object... params) throws Exception;
	
	/**
	 * Find Jira object
	 * @param uniqueKey JiraConfigDTO.getUniqueKey().
	 * @param params Parameters. Depends on implementation.
	 * @throws Exception
	 */
	public abstract JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception;
	
	/**
	 * Find Jira object based on JiraConfigDTO (serialized from file)
	 * @param src JiraConfigDTO
	 * @throws Exception
	 */
	public final JiraConfigDTO findByDTO(JiraConfigDTO src) throws Exception {
		return findByUniqueKey(src.getUniqueKey(), src.getObjectParameters());
	}
	
	/**
	 * Merge items. 
	 * @param oldItem Existing item from calling find(), can be null. If non-null, .getJiraObject() is non-null.
	 * @param newItem New item. .getJiraObject() will be null.
	 * @return Item after merge.
	 * @throws Exception
	 */
	public abstract JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception;
	
	/**
	 * Merge items. 
	 * ImportData.getServer() will be updated.
	 * Result will be stored in ImportData.getImportResult().
	 * @param items Map of ImportData to be merged.
	 * @throws Exception
	 */
	public final void merge(Map<String, ImportData> items) throws Exception {
		if (items != null) {
			for (ImportData data : items.values()) {
				try {
					JiraConfigDTO result = merge(data.getServer(), data.getData());
					data.setServer(result);
					data.setImportResult("Updated");
				} catch (Exception ex) {
					data.setImportResult(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
					throw ex;
				}
			}
		}
	}
	
}
