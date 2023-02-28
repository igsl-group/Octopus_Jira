package com.igsl.configmigration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Store for JiraConfigDTO
 * 
 * Two layers of map, first by JiraConfigDTO class, second by JiraConfigDTO's unique key
 */
public class DTOStore {

	private static final Logger LOGGER = Logger.getLogger(DTOStore.class);
	
	// Sort by type - always need this, so okay to split storage into types
	private Map<String, Map<String, JiraConfigDTO>> store;
	
	/**
	 *  Is this store for export or import
	 *  
	 *  Export:
	 *  - DTOs are looked up by internal ID
	 *  
	 *  Import:
	 *  - DTOs are looked up by unique key (to compare with deserialized data)
	 */
	private boolean export;
	
	public DTOStore(boolean export) {
		this.export = export;
		store = new LinkedHashMap<>();
		for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(true)) {
			store.put(util.getImplementation(), new LinkedHashMap<String, JiraConfigDTO>());
		}
	}
	
	/**
	 * Get store for DTO class
	 * @param utilName JiraConfigUtil class name
	 * @return Map<String, JiraConfigDTO>
	 */
	public Map<String, JiraConfigDTO> getTypeStore(String utilName) {
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
	public Map<String, JiraConfigDTO> getTypeStore(JiraConfigUtil util) {
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
	public Collection<JiraConfigUtil> getUtils() {
		return Collections.unmodifiableCollection(JiraConfigTypeRegistry.getConfigUtilList(true));
	}

	private String getDTOKey(JiraConfigDTO dto) {
		if (export) {
			return dto.getUniqueKey();
		} else {
			return dto.getInternalId();
		}
	}
	
	/**
	 * Empties store.
	 */
	public void clear() {
		for (Map<String, JiraConfigDTO> typeStore : store.values()) {
			typeStore.clear();
		}
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
	public void register(JiraConfigDTO dto) 
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
	public boolean unregister(JiraConfigDTO dto) 
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
	public JiraConfigDTO check(JiraConfigDTO dto) throws NullPointerException, IllegalStateException {
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