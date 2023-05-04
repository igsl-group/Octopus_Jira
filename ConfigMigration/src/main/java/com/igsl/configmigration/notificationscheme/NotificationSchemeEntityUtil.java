package com.igsl.configmigration.notificationscheme;

import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class NotificationSchemeEntityUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(NotificationSchemeEntityUtil.class);
	
	@Override
	public String getName() {
		return "Priority Scheme Entity";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		return null;
	}

	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		throw new Exception("Notification Scheme Entity is read only");
	}

	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return NotificationSchemeEntityDTO.class;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		return Collections.emptyMap();
	}

}
