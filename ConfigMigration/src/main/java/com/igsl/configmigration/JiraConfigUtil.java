package com.igsl.configmigration;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.SessionData.ImportData;

@JsonDeserialize(using=JiraConfigUtilDeserializer.class)
@JsonIgnoreProperties(value={"implementation"}, allowGetters=true)
public abstract class JiraConfigUtil {
	
	private static final Logger LOGGER = Logger.getLogger(JiraConfigUtil.class);
	protected static final ObjectMapper OM = new ObjectMapper()
												.enable(SerializationFeature.INDENT_OUTPUT)
												.setSerializationInclusion(Include.NON_NULL);
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
	 * Return a CSS class friendly name (replacing dots with dashes).
	 * @return String
	 */
	@JsonIgnore
	public final String getImplementationCSS() {
		String s = getImplementation();
		if (s != null) {
			s = s.replaceAll("\\.", "-");
		}
		return s;
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
	 * Find JiraConfigDTO by internal ID.
	 * @param id String
	 * @param params Parameters. Depends on implementation.
	 * @throws Exception
	 */
	public abstract JiraConfigDTO findByInternalId(String id, Object... params) throws Exception;
	
	/**
	 * Check if provided DTO matches filter.
	 * Default implementation uses .getConfigName() for a case-insensitive wildcard match.
	 * Override if necessary.
	 * @param dto
	 * @param filter
	 * @return
	 */
	public boolean matchFilter(JiraConfigDTO dto, String filter) {
		if (filter != null && !filter.isEmpty()) {
			filter = filter.toLowerCase();
			String name = dto.getConfigName();
			if (name != null) {
				name = name.toLowerCase();
			} else {
				name = "";
			}
			if (!name.contains(filter)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Search Jira object with partial match. 
	 * The exact fields to be matched depends on the implementation.
	 * Implementations should put matching logic in matchFilter(). 
	 * @param filter String for partial match with unique key or id. If empty, find all.
	 * @param params Parameters. Depends on implementation. 
	 * @return Non-null Map<String, JiraConfigDTO> as search result
	 * @throws Exception
	 */
	@Nonnull
	public abstract Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception;
	
	/**
	 * Required for implementations that returns true for isVisible().
	 * Return description of filter in search().
	 * Override if you override matchFilter().
	 * @return String
	 */
	@JsonIgnore
	public String getSearchHints() {
		return "Case-insensitive wildcard match";
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
	
	/**
	 * Register DTO and nested DTOs.
	 * If already registered, return the copy in store.
	 * Returns provided DTO if it get registered.
	 * Uses reflection to handle nested DTOs. Override if necessary.
	 * 
	 * @param store DTOStore to register in. If null, then no registration is performed.
	 * @param dto DTO to register
	 * @return JiraConfigDTO Registered object in store
	 * @throws Exception Failed to process DTO class using reflection
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JiraConfigDTO register(DTOStore store, JiraConfigDTO dto) throws Exception {
		if (store != null && dto != null) {
			LOGGER.debug("[TEST] " + dto.getUtilClass().getSimpleName() + ", dto: " + dto.getUniqueKey());
			// Check for nested DTOs
			BeanInfo info = Introspector.getBeanInfo(dto.getClass());
			for (PropertyDescriptor propDesc : info.getPropertyDescriptors()) {
				if (propDesc.getReadMethod() == null) {
					LOGGER.debug("[TEST] No read method");
					continue;
				}
				if (propDesc.getWriteMethod() == null) {
					LOGGER.debug("[TEST] No write method");
					continue;
				}
				if (JiraConfigDTO.MAP_EXCLUDE_METHODS.indexOf(propDesc.getReadMethod().getName()) != -1) {
					LOGGER.debug("[TEST] Is default ignored method");
					continue;
				}
				if (dto.getMapIgnoredMethods().indexOf(propDesc.getReadMethod().getName()) != -1) {
					LOGGER.debug("[TEST] Is ignored method");
					continue;
				}
				LOGGER.debug("[TEST] Processing property name: " + propDesc.getName());
				if (JiraConfigDTO.class.isAssignableFrom(propDesc.getPropertyType())) {
					// Nested DTO found
					JiraConfigDTO nestedDTO = (JiraConfigDTO) propDesc.getReadMethod().invoke(dto);
					if (nestedDTO != null) {
						LOGGER.debug("[TEST] Checking nested DTO: " + nestedDTO.getUniqueKey() + " (" + nestedDTO.getClass() + ")");
						JiraConfigUtil nestedUtil = JiraConfigTypeRegistry.getConfigUtil(nestedDTO.getUtilClass());
						JiraConfigDTO checkedNestedDTO  = nestedUtil.register(store, nestedDTO);
						propDesc.getWriteMethod().invoke(dto, checkedNestedDTO);
						LOGGER.debug("[TEST] Updated nested DTO: " + checkedNestedDTO.getUniqueKey());
					} 
				} else if (Collection.class.isAssignableFrom(propDesc.getPropertyType())) {
					Collection oldData = (Collection) propDesc.getReadMethod().invoke(dto);
					if (oldData != null) {
						Collection newData = new ArrayList();	// TODO How to get concrete class?
						for (Object item : oldData) {
							if (item != null && JiraConfigDTO.class.isAssignableFrom(item.getClass())) {
								JiraConfigDTO dtoItem = (JiraConfigDTO) item;
								LOGGER.debug("[TEST] Checking nested DTO: " + dtoItem.getUniqueKey() + " (" + dtoItem.getClass() + ")");
								JiraConfigUtil nestedUtil = JiraConfigTypeRegistry.getConfigUtil(dtoItem.getUtilClass());
								JiraConfigDTO checkedItem = nestedUtil.register(store, dtoItem);
								newData.add(checkedItem);
								LOGGER.debug("[TEST] Updated nested DTO: " + checkedItem.getUniqueKey());
							} else {
								newData.add(item);
							}
						}
						propDesc.getWriteMethod().invoke(dto, newData);
						LOGGER.debug("[TEST] Updated list property: " + propDesc.getName());
					}
				} else if (Map.class.isAssignableFrom(propDesc.getPropertyType())) {
					Map oldData = (Map) propDesc.getReadMethod().invoke(dto);
					if (oldData != null) {
						Map newData = new LinkedHashMap();	// TODO How to get concrete class?
						for (Object entry : oldData.entrySet()) {
							Map.Entry e = (Map.Entry) entry;
							if (e.getValue() != null && JiraConfigDTO.class.isAssignableFrom(e.getValue().getClass())) {
								JiraConfigDTO dtoItem = (JiraConfigDTO) e.getValue();
								LOGGER.debug("[TEST] Checking nested DTO: " + dtoItem.getUniqueKey() + " (" + dtoItem.getClass() + ")");
								JiraConfigUtil nestedUtil = JiraConfigTypeRegistry.getConfigUtil(
										dtoItem.getUtilClass());
								JiraConfigDTO checkedItem = nestedUtil.register(store, dtoItem);
								newData.put(e.getKey(), checkedItem);
								LOGGER.debug("[TEST] Updated nested DTO: " + checkedItem.getUniqueKey());
							} else {
								newData.put(e.getKey(), e.getValue());
							}
						}
						propDesc.getWriteMethod().invoke(dto, newData);
						LOGGER.debug("[TEST] Updated map property: " + propDesc.getName());
					}
				} else if (propDesc.getPropertyType().isArray() && 
						JiraConfigDTO.class.isAssignableFrom(propDesc.getPropertyType().getComponentType())) {
					JiraConfigDTO[] oldData = (JiraConfigDTO[]) 
							propDesc.getReadMethod().invoke(dto);
					List<JiraConfigDTO> newData = new ArrayList<>();
					for (JiraConfigDTO item : oldData) {
						LOGGER.debug("[TEST] Checking nested DTO: " + item.getUniqueKey() + " (" + item.getClass() + ")");
						JiraConfigUtil nestedUtil = JiraConfigTypeRegistry.getConfigUtil(item.getUtilClass());
						JiraConfigDTO checkedItem = nestedUtil.register(store, item);
						newData.add(checkedItem);
						LOGGER.debug("[TEST] Updated nested DTO: " + checkedItem.getUniqueKey());
					}
					propDesc.getWriteMethod().invoke(dto, (Object[]) newData.toArray(new JiraConfigDTO[0]));
					LOGGER.debug("[TEST] Updated array property: " + propDesc.getName());
				}
			}
			JiraConfigDTO registered = store.checkAndRegister(dto);
			// Merge relatedObjects
			for (JiraConfigRef ref : dto.getRelatedObjects()) {
				registered.addRelatedObject(ref);
			}
			// Merge referencedObjects
			for (JiraConfigRef ref : dto.getReferencedObjects()) {
				registered.addReferencedObject(ref);
			}
			LOGGER.debug("[TEST] Registered to store, type: " + dto.getClass() + ", dto: " + registered.getUniqueKey());
			return registered;
		}
		return dto;
	}
	
}
