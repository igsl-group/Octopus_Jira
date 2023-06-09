package com.igsl.configmigration.workflow.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfig;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigComparator;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigWrapper;

import net.java.ao.Query;

public class MapperConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(MapperConfigUtil.class);
	private static final ObjectMapper OM = new ObjectMapper();
	
	public static JiraConfigUtil lookupUtil(String objectType) {
		JiraConfigUtil util = JiraConfigTypeRegistry.getConfigUtil(objectType);
		return util;
	}
	
	public static JiraConfigDTO lookupDTO(String value, String objectType) {
		JiraConfigDTO result = null;
		if (value != null && objectType != null) {
			JiraConfigUtil util = lookupUtil(objectType);
			if (util != null) {
				try {
					result = util.findByInternalId(value);
				} catch (Exception e) {
					LOGGER.error("Unable to lookup item", e);
				}
			}
		}
		return result;
	}
	
	/**
	 * Find id in importStore, then locate a matching item in util according to objectType
	 * @param id
	 * @param objectType
	 * @param importStore
	 * @return JiraConfigDTO
	 */
	public static JiraConfigDTO resolveMapping(String id, String objectType, DTOStore importStore) {
		JiraConfigDTO result = null;
		JiraConfigUtil util = lookupUtil(objectType);
		if (util != null) {
			LOGGER.debug("Remapping " + objectType + ", id " + id);
			Map<String, JiraConfigDTO> typeStore = importStore.getTypeStore(util);
			if (typeStore != null) {
				LOGGER.debug("Checking importStore...");
				for (JiraConfigDTO dto : typeStore.values()) {
					LOGGER.debug("Checking importStore item: " + dto.getInternalId() + " = " + dto.getUniqueKey());
					if (dto.getInternalId().equals(id)) {
						LOGGER.debug("Found " + objectType + ", id " + id + " in importStore, uniqueKey " + dto.getUniqueKey());
						// Found import object referenced by id
						// Look for matching name in util
						try {
							JiraConfigDTO mappedDTO = util.findByDTO(dto);
							if (mappedDTO != null) {
								LOGGER.debug("Found in current server, uniqueKey " + mappedDTO.getUniqueKey() + ", id " + mappedDTO.getInternalId());
								result = mappedDTO;
							}
						} catch (Exception ex) {
							LOGGER.error("Error resolving object mapping from id to exportStore item", ex);
						}
					}
				}
			}
		}
		return result;
	}
	
	public static String constructReplacement(String replacement, int groupCount, Map<Integer, String> replacementData) {
		String result = replacement;
		for (int i = 1; i <= groupCount; i++) {
			if (replacementData.containsKey(i)) {
				result = result.replaceAll("\\$" + i, replacementData.get(i));
			}
		}
		return result;
	}
	
	public static List<Integer> parseCaptureGroups(String captureGroups) {
		List<Integer> result = null;
		if (captureGroups != null && !captureGroups.isEmpty()) {
			result = new ArrayList<>();
			String[] list = captureGroups.split(",");
			for (String s : list) {
				s = s.trim();
				try {
					int i = Integer.parseInt(s);
					result.add(i);
				} catch (NumberFormatException e) {
					// Ignore
				}
			}
		}
		return result;
	}
	
	public static Map<String, MapperConfigWrapper> getMapperConfigs(ActiveObjects ao) {
		Map<String, MapperConfigWrapper> result = new LinkedHashMap<>();
		MapperConfig[] list = ao.find(MapperConfig.class);
		Arrays.sort(list, new MapperConfigComparator());
		for (MapperConfig config : list) {
			result.put(Integer.toString(config.getID()), new MapperConfigWrapper(config));
		}
		return result;
	}
	
	public static MapperConfigWrapper getMapperConfigByXPath(ActiveObjects ao, String xPath) {
		MapperConfig[] list = ao.find(MapperConfig.class, Query.select().where("XPATH = ?", xPath));
		if (list != null && list.length == 1) {
			MapperConfigWrapper wrapper = new MapperConfigWrapper(list[0]);
			return wrapper;
		}
		return null;
	}

	public static MapperConfigWrapper getMapperConfigById(ActiveObjects ao, String id) {
		MapperConfig[] list = ao.find(MapperConfig.class, Query.select().where("ID = ?", id));
		if (list != null && list.length == 1) {
			MapperConfigWrapper wrapper = new MapperConfigWrapper(list[0]);
			return wrapper;
		}
		return null;
	}
	
	public static MapperConfigWrapper saveMapperConfig(ActiveObjects ao, MapperConfigWrapper wrapper) {
		if (wrapper.getConfig() == null) {
			LOGGER.debug("Creating new MappingConfig");
			// Create
			MapperConfig created = ao.create(MapperConfig.class);
			wrapper.setConfig(created);
		}
		MapperConfigWrapper result = new MapperConfigWrapper();
		result.setConfig(
			ao.executeInTransaction(new TransactionCallback<MapperConfig>() {
				@Override
				public MapperConfig doInTransaction() {
					LOGGER.debug("Saving MappingConfig: " + wrapper.getConfig().getID());
					wrapper.copyTo(wrapper.getConfig());
					wrapper.getConfig().save();
					return wrapper.getConfig();
				}
			})
		);
		LOGGER.debug("Saved MappingConfig: " + wrapper.getConfig().getID());
		return result;
	}
	
	public static void deleteMapperConfig(ActiveObjects ao, MapperConfigWrapper... wrappers) {
		for (MapperConfigWrapper wrapper : wrappers) {
			ao.delete(wrapper.getConfig());
		}
	}
	
}
