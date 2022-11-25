package com.igsl.configmigration.insight;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.SessionData.ImportData;
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectSchemaFacade;
import com.riadalabs.jira.plugins.insight.services.model.ObjectSchemaBean;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ObjectSchemaBeanUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ObjectSchemaBeanUtil.class);
	private static ObjectSchemaFacade OBJECT_SCHEMA_FACADE;			
			
	static {
		Logger logger = Logger.getLogger("com.igsl.configmigration.insight.InsightUtil");
		try {
			OBJECT_SCHEMA_FACADE = 
					(ObjectSchemaFacade) ComponentAccessor.getOSGiComponentInstanceOfType(
					ComponentAccessor.getPluginAccessor().getClassLoader()
					.loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectSchemaFacade"));
			logger.debug("OBJECT_FACADE: " + OBJECT_SCHEMA_FACADE);
		} catch (Exception ex) {
			logger.error("Failed to initialize InsightUtil", ex);
		}
	}
	
	@Override
	public String getName() {
		return "Insight Schema";
	}
	
	@Override
	public TypeReference<?> getTypeReference() {
		return new TypeReference<Map<String, ObjectSchemaBeanDTO>>() {};
	}
	
	@Override
	public Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigItem> result = new TreeMap<>();
		List<ObjectSchemaBean> objects = OBJECT_SCHEMA_FACADE.findObjectSchemaBeans();
		for (ObjectSchemaBean ob : objects) {
			ObjectSchemaBeanDTO item = new ObjectSchemaBeanDTO();
			item.setJiraObject(ob);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	/**
	 * params[0]: name (String)
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		String name = (String) params[0];
		List<ObjectSchemaBean> objects = OBJECT_SCHEMA_FACADE.findObjectSchemaBeans();
		for (ObjectSchemaBean ob : objects) {
			if (name.equals(ob.getName())) {
				return ob;
			}
		}
		return null;
	}
	
	public Object merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception {
		ObjectSchemaBean original = null;
		if (oldItem != null) {
			if (oldItem.getJiraObject() != null) {
				original = (ObjectSchemaBean) oldItem.getJiraObject();
			} else {
				original = (ObjectSchemaBean) findObject(oldItem.getUniqueKey());
			}
		} else {
			original = (ObjectSchemaBean) findObject(newItem.getUniqueKey());
		}
		// Schema?
		return null;
	}
	
	@Override
	public void merge(Map<String, ImportData> items) throws Exception {
		for (ImportData data : items.values()) {
			try {
				merge(data.getServer(), data.getData());
				data.setImportResult("Updated");
			} catch (Exception ex) {
				data.setImportResult(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
				throw ex;
			}
		}
	}

}
