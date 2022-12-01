package com.igsl.configmigration.insight;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
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
	public Map<String, JiraConfigDTO> findAll(Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		List<ObjectBean> objects = IQL_FACADE.findObjects("Name like \"\"");
		for (ObjectBean ob : objects) {
			ObjectBeanDTO item = new ObjectBeanDTO();
			item.setJiraObject(ob);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}
	
	/**
	 * #0: Schema ID
	 */
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Integer schemaId = (Integer) params[0];
		List<ObjectBean> objects = IQL_FACADE.findObjects(schemaId, "Name like \"\"");
		Integer idAsInt = Integer.parseInt(id);
		for (ObjectBean ob : objects) {
			if (ob.getId().equals(idAsInt)) {
				ObjectBeanDTO item = new ObjectBeanDTO();
				item.setJiraObject(ob);
				return item;
			}
		}
		return null;
	}

	/**
	 * #0: Schema ID
	 */
	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		Integer schemaId = (Integer) params[0];
		String iql = "Name LIKE \"" + ((uniqueKey != null)? uniqueKey : "") + "\"";
		List<ObjectBean> results;
		if (schemaId != null) {
			results = IQL_FACADE.findObjects(schemaId, iql);
		} else {
			results = IQL_FACADE.findObjects(iql);
		}
		if (results.size() == 1) {
			ObjectBeanDTO dto = new ObjectBeanDTO();
			dto.setJiraObject(results.get(0), schemaId);
			return dto;
		}
		return null;
	}

	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		ObjectBeanDTO original = null;
		if (oldItem != null) {
			original = (ObjectBeanDTO) oldItem;
		} else {
			original = (ObjectBeanDTO) findByDTO(newItem);
		}		
		// Schema?
		
		// Object
		
		// Attributes
		
		return null;
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ObjectBeanDTO.class;
	}

	@Override
	public boolean isPublic() {
		// Referenced by other DTOs
		return false;
	}

}
