package com.igsl.configmigration.insight;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectSchemaFacade;
import com.riadalabs.jira.plugins.insight.services.model.ObjectSchemaBean;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ObjectSchemaBeanUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(ObjectSchemaBeanUtil.class);
	
	@Override
	public String getName() {
		return "Insight Schema";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		List<Object> objects = ObjectBeanUtil.findObjectSchemaBeans();
		Integer idAsInt = Integer.parseInt(id);
		for (Object ob : objects) {
			int obid = ObjectBeanUtil.getObjectSchemaId(ob);
			if (idAsInt.equals(obid)) {
				ObjectSchemaBeanDTO dto = new ObjectSchemaBeanDTO();
				dto.setJiraObject(ob);
				return dto;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		List<Object> objects = ObjectBeanUtil.findObjectSchemaBeans();
		for (Object ob : objects) {
			String key = ObjectBeanUtil.getObjectSchemaKey(ob);
			if (uniqueKey.equals(key)) {
				ObjectSchemaBeanDTO dto = new ObjectSchemaBeanDTO();
				dto.setJiraObject(ob);
				return dto;
			}
		}
		return null;
	}

	public JiraConfigDTO merge(JiraConfigDTO oldItem, JiraConfigDTO newItem) throws Exception {
		ObjectSchemaBeanDTO original = null;
		if (oldItem != null) {
			original = (ObjectSchemaBeanDTO) oldItem;
		} else {
			original = (ObjectSchemaBeanDTO) findByDTO(newItem);
		}
		// Schema?
		return null;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return ObjectSchemaBeanDTO.class;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		// Filter ignored
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		List<Object> objects = ObjectBeanUtil.findObjectSchemaBeans();
		for (Object ob : objects) {
			ObjectSchemaBeanDTO item = new ObjectSchemaBeanDTO();
			item.setJiraObject(ob);
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
