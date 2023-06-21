package com.igsl.configmigration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.igsl.configmigration.workflow.mapper.MapperConfigUtil;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigWrapper;

@Component
public class PluginSetup implements InitializingBean, DisposableBean {

	public static final String PLUGIN_KEY = "com.igsl.ConfigMigration";
	private static final String MAPPING_LIST = "/mapping/list.txt";
	private static final String COMMA = ",";
	
	private static final Logger LOGGER = Logger.getLogger(PluginSetup.class);
	private static final ObjectMapper OM = new ObjectMapper();
	
	private EventPublisher eventPublisher;
	private ActiveObjects ao;
	
	public PluginSetup(@ComponentImport ActiveObjects ao, @JiraImport EventPublisher eventPublisher) {
		this.ao = ao;
		this.eventPublisher = eventPublisher;
	}
	
	@Override
	public void destroy() throws Exception {
		this.eventPublisher.unregister(this);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.eventPublisher.register(this);
	}
	
	@EventListener
    public void onPluginEnabled(PluginEnabledEvent event) {
		if (PLUGIN_KEY.equals(event.getPlugin().getKey())) {
			LOGGER.debug("Plugin enabled");
			// Setup mappings if not already exist
			try {
				ObjectReader or = OM.readerFor(MapperConfigWrapper.class);
				InputStream mappingList = ClassLoaderUtils.getResourceAsStream(MAPPING_LIST, this.getClass());
				BufferedReader br = new BufferedReader(new InputStreamReader(mappingList));
				String mappingListString = br.readLine();
				// Mapping list is a comma-delimited list of resource paths
				String[] list = mappingListString.split(COMMA);
				for (String mappingPath : list) {
					LOGGER.debug("Processing mapping " + mappingPath + "...");
					InputStream mappingIn = ClassLoaderUtils.getResourceAsStream(mappingPath, this.getClass());
					if (mappingIn != null) {
						try {
							MapperConfigWrapper mapping = or.readValue(mappingIn);
							if (mapping != null) {
								MapperConfigWrapper existing = MapperConfigUtil.getMapperConfigByName(ao, mapping.getDescription());
								if (existing == null) {
									MapperConfigUtil.saveMapperConfig(ao, mapping);
									LOGGER.debug("Mapping " + mapping.getDescription() + " created");
								} else {
									LOGGER.debug("Mapping " + mapping.getDescription() + " already exists, it will not be modified");
								}
							}
						} catch (Exception ex) {
							LOGGER.error("Error processing " + mappingPath, ex);
						}
					}
				}
			} catch (Exception ex) {
				LOGGER.error("Failed to setup default workflow mappings", ex);
			}
		}
	}

}
