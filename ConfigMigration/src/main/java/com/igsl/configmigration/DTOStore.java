package com.igsl.configmigration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Store for JiraConfigDTO
 * 
 * Two layers of map, first by JiraConfigUtil class, second by JiraConfigDTO's unique key
 */
public class DTOStore {

	private static final Logger LOGGER = Logger.getLogger(DTOStore.class);
	
	private static final String TITLE_TOTAL = "Total";
	private static final String TITLE_HIDDEN = "Hidden";
	
	// Group DTOs by Util type
	protected Map<String, Map<String, JiraConfigDTO>> store;
	
	public DTOStore() {
		store = new LinkedHashMap<>();
		for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(false)) {
			store.put(util.getImplementation(), new LinkedHashMap<String, JiraConfigDTO>());
		}
	}
	
	/**
	 * Get object counts as a string
	 * Includes 1 to 3 parts: 
	 * Selected object type (if currentUtilName is not null)
	 * All object types (excludes invisible utils based on showAll)
	 * Hidden object types (if showAll is false)
	 * 
	 * Each part contains: title (selected count/total count). 
	 * 
	 * @param currentUtilName
	 * @param showAll
	 * @return
	 */
	public final String getObjectCounts(String currentUtilName, boolean showAll) {
		StringBuilder result = new StringBuilder();
		long selectedObjectTypeCount = 0;
		long objectTypeCount = 0;
		long selectedTotalCount = 0;
		long totalCount = 0;
		long selectedHiddenCount = 0;
		long hiddenCount = 0;
		for (Map.Entry<String, Map<String, JiraConfigDTO>> s : this.store.entrySet()) {
			String utilName = s.getKey();
			JiraConfigUtil util = JiraConfigTypeRegistry.getConfigUtil(s.getKey());
			if (utilName.equals(currentUtilName)) {
				objectTypeCount += s.getValue().size();
			}
			if (util.isVisible()) {
				totalCount += s.getValue().size();
			} else {
				hiddenCount += s.getValue().size();
			}
			for (JiraConfigDTO dto : s.getValue().values()) {
				if (dto.isSelected()) {
					if (utilName.equals(currentUtilName)) {
						selectedObjectTypeCount++;
					}
					if (util.isVisible()) {
						selectedTotalCount++;
					} else {
						selectedHiddenCount++;
					}
				}
			}
			if (utilName.equals(currentUtilName)) {
				result	.append(util.getName())
						.append("(")
						.append(selectedObjectTypeCount)
						.append("/")
						.append(objectTypeCount)
						.append(") ");
			}
		}	
		if (showAll) {
			selectedTotalCount += selectedHiddenCount;
			totalCount += hiddenCount;
		}
		result	.append(TITLE_TOTAL)
				.append("(")
				.append(selectedTotalCount)
				.append("/")
				.append(totalCount)
				.append(") ");
		if (!showAll) {
			result	.append(TITLE_HIDDEN)
					.append("(")
					.append(selectedHiddenCount)
					.append("/")
					.append(hiddenCount)
					.append(") ");
		}
		return result.toString();
	}
	
	/**
	 * Get store for DTO class
	 * @param utilName JiraConfigUtil class name
	 * @return Map<String, JiraConfigDTO>
	 */
	public final Map<String, JiraConfigDTO> getTypeStore(String utilName) {
		if (store.containsKey(utilName)) {
			LOGGER.debug("Type store found for String: " + utilName);
			return store.get(utilName);
		}
		return new LinkedHashMap<>();
	}
	
	/**
	 * Get store for DTO class
	 * @param util JiraConfigUtil
	 * @return Map<String, JiraConfigDTO>
	 */
	public final Map<String, JiraConfigDTO> getTypeStore(JiraConfigUtil util) {
		if (store.containsKey(util.getImplementation())) {
			LOGGER.debug("Type store found for util: " + util);
			return store.get(util.getImplementation());
		}
		return new LinkedHashMap<>();
	}
	
	/**
	 * Get list of registered JiraConfigUtil
	 * @return Unmodifiable Collection of JiraConfigUtil
	 */
	public final Collection<JiraConfigUtil> getUtils() {
		return Collections.unmodifiableCollection(JiraConfigTypeRegistry.getConfigUtilList(true));
	}

	protected String getDTOKey(JiraConfigDTO dto) {
		if (dto != null) {
			return dto.getUniqueKey();
		} 
		return null;
	}
	
	/**
	 * Empties store.
	 */
	public final void clear() {
		for (Map<String, JiraConfigDTO> typeStore : store.values()) {
			typeStore.clear();
		}
	}
	
	public final JiraConfigDTO checkAndRegister(JiraConfigDTO dto) 
			throws NullPointerException, IllegalStateException, IllegalArgumentException {
		if (dto == null) {
			throw new NullPointerException("DTO cannot be null");
		}
		Map<String, JiraConfigDTO> s = getTypeStore(dto.getUtilClass().getCanonicalName());
		if (s == null) { 
			throw new IllegalStateException("Store cannot be located for class " + dto.getClass().getCanonicalName());
		}
		String key = getDTOKey(dto);
		if (s.containsKey(key)) { 
			return s.get(key);
		}
		s.put(key, dto);
		return dto;
	}
	
	/**
	 * Add DTO to store.
	 * The key used depends on if store is export (uniqueKey) or import (internal id).
	 * 
	 * @param dto JiraConfigDTO to be added to store
	 * @throws NullPointerException If provided DTO is null
	 * @throws IllegalStateException If store cannot be found for JiraConfigDTO type
	 * @throws IllegalArgumentException If store already contains DTO with same key
	 */
	public final void register(JiraConfigDTO dto) 
			throws NullPointerException, IllegalStateException, IllegalArgumentException {
		if (dto == null) {
			throw new NullPointerException("DTO cannot be null");
		}
		Map<String, JiraConfigDTO> s = getTypeStore(dto.getUtilClass().getCanonicalName());
		if (s == null) { 
			throw new IllegalStateException("Store cannot be located for class " + dto.getClass().getCanonicalName());
		}
		String key = getDTOKey(dto);
		if (s.containsKey(key)) { 
			throw new IllegalArgumentException("DTO with key " + key + " already registered");
		} 
		s.put(key, dto);
	}
	
	/**
	 * Unregister DTO from store.
	 * 
	 * @param dto JiraConfigDTO to be removed.
	 * @return Boolean, true if removed. 
	 * @throws NullPointerException If provided DTO is null
	 * @throws IllegalStateException If store cannot be found for JiraConfigDTO type
	 */
	public final boolean unregister(JiraConfigDTO dto) 
			throws NullPointerException, IllegalStateException {
		if (dto == null) {
			throw new NullPointerException("DTO cannot be null");
		}
		Map<String, JiraConfigDTO> s = getTypeStore(dto.getUtilClass().getCanonicalName());
		if (s == null) { 
			throw new IllegalStateException("Store cannot be located for class " + dto.getClass().getCanonicalName());
		}
		String key = getDTOKey(dto);
		return s.remove(key, dto);
	}
	
	/**
	 * Check if JiraConfigDTO already exists in store.
	 * @param dto JiraConfigDTO 
	 * @return JiraConfigDTO Null if not found, otherwise the JiraConfigDTO already registered in store
	 * @throws NullPointerException If provided DTO is null
	 * @throws IllegalStateException If store cannot be found for JiraConfigDTO type
	 */
	public final JiraConfigDTO check(JiraConfigDTO dto) throws NullPointerException, IllegalStateException {
		if (dto == null) {
			throw new NullPointerException("DTO cannot be null");
		}
		Map<String, JiraConfigDTO> s = getTypeStore(dto.getUtilClass().getCanonicalName());
		if (s == null) { 
			throw new IllegalStateException("Store cannot be located for class " + dto.getClass().getCanonicalName());
		}
		String key = getDTOKey(dto);
		return s.get(key);
	}
	
}