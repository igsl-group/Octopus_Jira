package com.igsl.configmigration;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
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

	private static final UserManager USER_MANAGER = ComponentAccessor.getUserManager();
	private static final String ADMIN_USER_NAME = "admin";
	
	private static final Logger LOGGER = Logger.getLogger(JiraConfigUtil.class);
	protected static final ObjectMapper OM = new ObjectMapper()
												.enable(SerializationFeature.INDENT_OUTPUT)
												.setSerializationInclusion(Include.NON_NULL);
	private static final String NEWLINE = "\r\n";

	/**
	 * To receive ActiveObjects from ExportAction2, to access data from MapperConfigUtil.		
	 */
	protected ActiveObjects ao;
	public void setActiveObjects(ActiveObjects ao) {
		this.ao = ao;
	}
	public ActiveObjects getActiveObjects() {
		return this.ao;
	}
	
	protected DTOStore importStore;
	public void setImportStore(DTOStore importStore) {
		this.importStore = importStore;
	}
	public DTOStore getImportStore() {
		return this.importStore;
	}
	
	protected DTOStore exportStore;
	public void setExportStore(DTOStore exportStore) {
		this.exportStore = exportStore;
	}
	public DTOStore getExportStore() {
		return this.exportStore;
	}
	
	/**
	 * Get admin user.
	 * @return ApplicationUser
	 */
	public static ApplicationUser getAdminUser() {
		return USER_MANAGER.getUserByName(ADMIN_USER_NAME);
	}
	
	/**
	 * Returns if this JiraConfigUtil require manual mapping.
	 * Default is false, override if needed.
	 * 
	 * When true, interface will display object mapping controls.
	 * In merge(), use DTO's mappedObject first. If null, then falls back to findByDTO().
	 * 
	 * @return boolean
	 */
	public boolean isManualMatch() {
		return false;
	}
	
	/**
	 * Checks if provided object is a default object and should not be modified.
	 * Default implementation only checks if uniqueKey is NULL_KEY or internalId is null
	 * Override when needed.
	 * 
	 * @param dto JiraConfigDTO to check.
	 * @return boolean
	 */
	public boolean isDefaultObject(JiraConfigDTO dto) {
		if (dto != null && 
			(
				JiraConfigDTO.NULL_KEY.equals(dto.getUniqueKey()) || 
				dto.getInternalId() == null
			)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Return Comparator for the DTO if there is one, null otherwise
	 */
	@SuppressWarnings("rawtypes")
	@JsonIgnore
	public Comparator getComparator() {
		return null;
	}
	
	/**
	 * Return true if this JiraConfigUtil is to be included in user interface.
	 * Return false if the associated JiraConfigDTO is only referenced via other JiraConfigDTOs.
	 */
	@JsonIgnore
	public abstract boolean isVisible();
	
	/**
	 * Return true if this object cannot be created by itself.
	 * On UI this object will not be given a checkbox, and will not be counted.
	 * @return
	 */
	@JsonIgnore
	public abstract boolean isReadOnly();
	
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
	 * @param exportStore DTOStore containing the current items.
	 * @param oldItem Existing item from calling find(), can be null. If non-null, .getJiraObject() is non-null.
	 * @param importStore DTOStore containing the import items.
	 * @param newItem New item. .getJiraObject() will be null.
	 * @return Item after merge.
	 * @throws Exception
	 */
	public abstract MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception;
	
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
	public final void merge(DTOStore exportStore, DTOStore importStore, Map<String, ImportData> items) 
			throws Exception {
		if (items != null) {
			for (ImportData data : items.values()) {
				try {
					MergeResult result = merge(exportStore, data.getServer(), importStore, data.getData());
					data.setServer(result.getNewDTO());
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
			// Check for nested DTOs
			BeanInfo info = Introspector.getBeanInfo(dto.getClass());
			for (PropertyDescriptor propDesc : info.getPropertyDescriptors()) {
				if (propDesc.getReadMethod() == null) {
					continue;
				}
				if (propDesc.getWriteMethod() == null) {
					continue;
				}
				if (JiraConfigDTO.MAP_EXCLUDE_METHODS.indexOf(propDesc.getReadMethod().getName()) != -1) {
					continue;
				}
				if (dto.getMapIgnoredMethods().indexOf(propDesc.getReadMethod().getName()) != -1) {
					continue;
				}
				if (JiraConfigDTO.class.isAssignableFrom(propDesc.getPropertyType())) {
					// Nested DTO found
					JiraConfigDTO nestedDTO = (JiraConfigDTO) propDesc.getReadMethod().invoke(dto);
					if (nestedDTO != null) {
						JiraConfigUtil nestedUtil = JiraConfigTypeRegistry.getConfigUtil(nestedDTO.getUtilClass());
						JiraConfigDTO checkedNestedDTO  = nestedUtil.register(store, nestedDTO);
						propDesc.getWriteMethod().invoke(dto, checkedNestedDTO);
					} 
				} else if (Collection.class.isAssignableFrom(propDesc.getPropertyType())) {
					if (Set.class.isAssignableFrom(propDesc.getPropertyType())) {
						Set oldData = (Set) propDesc.getReadMethod().invoke(dto);
						if (oldData != null) {
							Set newData = new HashSet();	// TODO How to get concrete class?
							for (Object item : oldData) {
								if (item != null && JiraConfigDTO.class.isAssignableFrom(item.getClass())) {
									JiraConfigDTO dtoItem = (JiraConfigDTO) item;
									JiraConfigUtil nestedUtil = JiraConfigTypeRegistry.getConfigUtil(dtoItem.getUtilClass());
									JiraConfigDTO checkedItem = nestedUtil.register(store, dtoItem);
									newData.add(checkedItem);
								} else {
									newData.add(item);
								}
							}
							propDesc.getWriteMethod().invoke(dto, newData);
						}
					} else {
						Collection oldData = (Collection) propDesc.getReadMethod().invoke(dto);
						if (oldData != null) {
							Collection newData = new ArrayList();	// TODO How to get concrete class?
							for (Object item : oldData) {
								if (item != null && JiraConfigDTO.class.isAssignableFrom(item.getClass())) {
									JiraConfigDTO dtoItem = (JiraConfigDTO) item;
									JiraConfigUtil nestedUtil = JiraConfigTypeRegistry.getConfigUtil(dtoItem.getUtilClass());
									JiraConfigDTO checkedItem = nestedUtil.register(store, dtoItem);
									newData.add(checkedItem);
								} else {
									newData.add(item);
								}
							}
							propDesc.getWriteMethod().invoke(dto, newData);
						}
					}
				} else if (Map.class.isAssignableFrom(propDesc.getPropertyType())) {
					Map oldData = (Map) propDesc.getReadMethod().invoke(dto);
					if (oldData != null) {
						Map newData = new LinkedHashMap();	// TODO How to get concrete class?
						for (Object entry : oldData.entrySet()) {
							Map.Entry e = (Map.Entry) entry;
							if (e.getValue() != null && JiraConfigDTO.class.isAssignableFrom(e.getValue().getClass())) {
								JiraConfigDTO dtoItem = (JiraConfigDTO) e.getValue();
								JiraConfigUtil nestedUtil = JiraConfigTypeRegistry.getConfigUtil(
										dtoItem.getUtilClass());
								JiraConfigDTO checkedItem = nestedUtil.register(store, dtoItem);
								newData.put(e.getKey(), checkedItem);
							} else {
								newData.put(e.getKey(), e.getValue());
							}
						}
						propDesc.getWriteMethod().invoke(dto, newData);
					}
				} else if (propDesc.getPropertyType().isArray() && 
						JiraConfigDTO.class.isAssignableFrom(propDesc.getPropertyType().getComponentType())) {
					JiraConfigDTO[] oldData = (JiraConfigDTO[]) 
							propDesc.getReadMethod().invoke(dto);
					List<JiraConfigDTO> newData = new ArrayList<>();
					for (JiraConfigDTO item : oldData) {
						JiraConfigUtil nestedUtil = JiraConfigTypeRegistry.getConfigUtil(item.getUtilClass());
						JiraConfigDTO checkedItem = nestedUtil.register(store, item);
						newData.add(checkedItem);
					}
					propDesc.getWriteMethod().invoke(dto, (Object[]) newData.toArray(new JiraConfigDTO[0]));
				}
			}
			JiraConfigDTO registered = store.checkAndRegister(dto);
			// Merge relatedObjects
			for (JiraConfigRef ref : dto.getRelatedObjectList()) {
				registered.addRelatedObject(ref);
			}
			// Merge referencedObjects
			for (JiraConfigRef ref : dto.getReferencedObjectList()) {
				registered.addReferencedObject(ref);
			}
			return registered;
		}
		return dto;
	}
	
	/**
	 * For resolving JiraConfigDTO with matching problem (i.e. isManualMatch returns true)
	 * Override as needed.
	 * Default implementation returns empty list.
	 * 
	 * @param store DTOStore to find matches from.
	 * @param params Map of parameters to match against. Depends on implementation.
	 * @return List of JiraConfigDTO with matching data.
	 */
	public List<JiraConfigDTO> findMatches(DTOStore store, Map<String, String> params) throws Exception {
		List<JiraConfigDTO> result = new ArrayList<>();
		return result;
	}
	
	/**
	 * Get compare key guide for pairing up export and import objects.
	 * Default implementation is based on uniqueKey and sorted using JiraConfigUtil.getComparator().
	 * Override as needed.
	 * @param exportStore DTOStore
	 * @param importStore DTOStore
	 * @return List of KeyGuide
	 */
	public List<KeyGuide> getCompareGuide(DTOStore exportStore, DTOStore importStore) throws Exception {
		List<KeyGuide> result = new ArrayList<>();
		List<JiraConfigDTO> list = new ArrayList<>();
		Comparator comparator = this.getComparator();
		Map<String, JiraConfigDTO> exportMap = exportStore.getTypeStore(this);
		if (exportStore != null) {
			list.addAll(exportMap.values());
		}
		Map<String, JiraConfigDTO> importMap = importStore.getTypeStore(this);
		if (importStore != null) {
			list.addAll(importMap.values());
		}
		if (comparator != null) {
			list.sort(comparator);
		}
		HashSet<String> keySet = new LinkedHashSet<>();
		for (JiraConfigDTO dto : list) {
			keySet.add(dto.getUniqueKey());
		}
		for (String s : keySet) {
			KeyGuide kg = new KeyGuide();
			if (exportMap.containsKey(s)) {
				kg.exportUniqueKey = s;
			}
			if (importMap.containsKey(s)) {
				kg.importUniqueKey = s;
			}
			result.add(kg);
		}
		return result;
	}

	public List<JiraConfigSearchType> getSearchTypes() {
		return null;
	}
	
	public JiraConfigSearchType parseSearchType(String s) {
		List<JiraConfigSearchType> list = getSearchTypes();
		if (list != null) {
			for (JiraConfigSearchType type : list) {
				if (type.toString().equals(s)) {
					return type;
				}
			}		
		}
		return null;
	}
}
