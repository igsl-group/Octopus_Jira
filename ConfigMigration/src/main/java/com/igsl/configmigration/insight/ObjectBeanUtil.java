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
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade;
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade;
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ObjectBeanUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ObjectBeanUtil.class);
	private static Class<?> IQL_FACADE_CLASS;
	private static ObjectFacade OBJECT_FACADE;			
	private static IQLFacade IQL_FACADE; 
			
	static {
		Logger logger = Logger.getLogger("com.igsl.configmigration.insight.InsightUtil");
		try {
			IQL_FACADE_CLASS = 
					ComponentAccessor.getPluginAccessor().getClassLoader()
					.loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade");
			logger.debug("IQL_FACADE_CLASS: " + IQL_FACADE_CLASS);
			OBJECT_FACADE = 
					(ObjectFacade) ComponentAccessor.getOSGiComponentInstanceOfType(
					ComponentAccessor.getPluginAccessor().getClassLoader()
					.loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade"));
			logger.debug("OBJECT_FACADE: " + OBJECT_FACADE);
			IQL_FACADE = 
					(IQLFacade) ComponentAccessor.getOSGiComponentInstanceOfType(IQL_FACADE_CLASS);
			logger.debug("IQL_FACADE: " + IQL_FACADE);
		} catch (Exception ex) {
			logger.error("Failed to initialize InsightUtil", ex);
		}
	}
	
	@Override
	public String getName() {
		return "Insight";
	}
	
	@Override
	public TypeReference<?> getTypeReference() {
		return new TypeReference<Map<String, ObjectBeanDTO>>() {};
	}
	
	@Override
	public Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception {
		Map<String, JiraConfigItem> result = new TreeMap<>();
		List<ObjectBean> objects = IQL_FACADE.findObjects("Name like \"\"");
		for (ObjectBean ob : objects) {
			ObjectBeanDTO item = new ObjectBeanDTO();
			item.setJiraObject(ob);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

	/**
	 * params[0]: name (String, required)
	 * params[1]: schema ID (Integer, optional)
	 */
	@Override
	public Object findObject(Object... params) throws Exception {
		String name = (String) params[0];
		String iql = "Name LIKE \"" + ((name != null)? name : "") + "\"";
		Integer schemaId = (Integer) params[1];
		List<ObjectBean> results;
		if (schemaId != null) {
			results = IQL_FACADE.findObjects(schemaId, iql);
		} else {
			results = IQL_FACADE.findObjects(iql);
		}
		if (results.size() == 1) {
			return results.get(0);
		}		
		return null;
	}
	
	public Object merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception {
		ObjectBean original = null;
		if (oldItem != null) {
			if (oldItem.getJiraObject() != null) {
				original = (ObjectBean) oldItem.getJiraObject();
			} else {
				original = (ObjectBean) findObject(oldItem.getUniqueKey());
			}
		} else {
			original = (ObjectBean) findObject(newItem.getUniqueKey());
		}
		
		// Schema?
		
		// Object
		
		// Attributes
		
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
