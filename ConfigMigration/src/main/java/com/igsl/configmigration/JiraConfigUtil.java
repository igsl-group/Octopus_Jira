package com.igsl.configmigration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private static final String NEWLINE = "\r\n";

	/**
	 * Return true if this JiraConfigUtil is to be included in user interface.
	 * Return false if the associated JiraConfigDTO is only referenced via other JiraConfigDTOs.
	 */
	@JsonIgnore
	public abstract boolean isVisible();
	
	/**
	 * Override and return true if the objects need to be reordered after create/update.
	 * Default is false.
	 * @return
	 */
	@JsonIgnore
	public boolean isPostSequenced() {
		return false;
	}
	
	/**
	 * Update object sequence according to provided list.
	 * Override and implement reorder logic. 
	 * Default is do nothing.
	 * @param list Import items to be reordered.
	 */
	public void updateSequence(List<JiraConfigDTO> list) throws Exception {
		// Do nothing
	}
	
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
	 * Search Jira object with partial match
	 * @param filter String for partial match with unique key or id. If empty, find all.
	 * @param params Parameters. Depends on implementation. 
	 * @return Map<String, JiraConfigDTO> as search result
	 * @throws Exception
	 */
	// TODO Should be abstract after all implementations are updated
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new HashMap<>();
		if (filter == null || filter.isEmpty()) {
			return findAll(params);
		} else {
			// TODO Perform partial match
			JiraConfigDTO dto = findByUniqueKey(filter, params);
			if (dto != null) {
				result.put(dto.getUniqueKey(), dto);
			}
		}
		return result;
	}
	
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
	
	public static final String printException(Throwable t) {
		StringBuilder sb = new StringBuilder();
		if (t != null) {
			sb.append("Exception: " + t.getClass().getCanonicalName()).append(NEWLINE);
			sb.append("Message: [" + t.getMessage() + "]").append(NEWLINE);
			for (StackTraceElement e : t.getStackTrace()) {
				sb	.append(e.getClassName())
					.append(".")
					.append(e.getMethodName())
					.append("(")
					.append(e.getFileName())
					.append("@")
					.append(e.getLineNumber())
					.append(")")
					.append(NEWLINE);
			}
			if (t.getCause() != null) {
				sb.append("Caused by").append(NEWLINE);
				sb.append(printException(t.getCause()));
			}
		}
		return sb.toString();
	}
	
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
					data.setImportResult(printException(ex));
					throw ex;
				}
			}
		}
	}
	
}
