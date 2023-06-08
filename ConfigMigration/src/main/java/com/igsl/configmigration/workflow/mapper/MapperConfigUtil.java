package com.igsl.configmigration.workflow.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
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
	
	public static JiraConfigDTO lookupDTO(String value, String objectType) {
		JiraConfigDTO result = null;
		if (value != null && objectType != null) {
			JiraConfigUtil util = JiraConfigTypeRegistry.getConfigUtil(objectType);
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
	
	public static List<String> parseArrayValue(String value) {
		List<String> result = new ArrayList<>();
		ObjectReader or = OM.readerFor(String.class);
		MappingIterator<String> it;
		try {
			it = or.readValues(value);
			if (it != null) {
				while (it.hasNext()) {
					result.add(it.next());
				}
			}
		} catch (IOException e) {
			LOGGER.error("Failed to parse value as array: " + value, e);
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
