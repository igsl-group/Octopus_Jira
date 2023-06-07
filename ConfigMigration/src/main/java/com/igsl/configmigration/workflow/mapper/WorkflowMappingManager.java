package com.igsl.configmigration.workflow.mapper;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigWrapper;

public class WorkflowMappingManager extends JiraWebActionSupport {

	@ComponentImport
	private final ActiveObjects ao;

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(WorkflowMappingManager.class);
	
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_MAPPING = "mapping";
	
	private static final String ACTION_DELETE = "delete";
	private static final String ACTION_ENABLE = "enable";
	private static final String ACTION_DISABLE = "disable";
	
	public WorkflowMappingManager(@ComponentImport ActiveObjects ao) {
		LOGGER.debug("Inject ActiveObjects: " + ao);
		this.ao = ao;
	}
	
	public Map<String, MapperConfigWrapper> getMappings() {
		return MapperConfigUtil.getMapperConfigs(this.ao);
	}

	public String getEditLink(String id) {
		return this.getServletContext().getContextPath() + "/secure/admin/plugins/handler/WorkflowMapper.jspa?action=loadMapping&mapping=" + id;
	}
	
	@Override
	protected void doValidation() {
		LOGGER.debug("doValidation");
	}

	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		HttpServletRequest req = this.getHttpRequest();
		
		String action = req.getParameter(PARAM_ACTION);
		LOGGER.debug("action: " + action);
		
		String[] mappingList = req.getParameterValues(PARAM_MAPPING);
		if (mappingList != null) {
			for (String mapping : mappingList) {
				MapperConfigWrapper wrapper = MapperConfigUtil.getMapperConfigById(ao, mapping);
				if (wrapper != null) {
					if (ACTION_DELETE.equals(action)) {
						MapperConfigUtil.deleteMapperConfig(ao, wrapper);
					} else if (ACTION_ENABLE.equals(action)) {
						wrapper.setDisabled(false);
						MapperConfigUtil.saveMapperConfig(ao, wrapper);
					} else if (ACTION_DISABLE.equals(action)) {
						wrapper.setDisabled(true);
						MapperConfigUtil.saveMapperConfig(ao, wrapper);
					}
				}
			}
		}
		
		return JiraWebActionSupport.INPUT;
	}

}
