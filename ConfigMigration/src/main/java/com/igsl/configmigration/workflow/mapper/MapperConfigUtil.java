package com.igsl.configmigration.workflow.mapper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfig;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigComparator;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigWrapper;

import net.java.ao.Query;

public class MapperConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(MapperConfigUtil.class);
	
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
