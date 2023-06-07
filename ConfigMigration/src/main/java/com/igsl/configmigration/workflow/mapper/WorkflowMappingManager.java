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
	
	public WorkflowMappingManager(@ComponentImport ActiveObjects ao) {
		LOGGER.debug("Inject ActiveObjects: " + ao);
		this.ao = ao;
	}
	
	public Map<String, MapperConfigWrapper> getMappings() {
		return MapperConfigUtil.getMapperConfigs(this.ao);
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
		
		return JiraWebActionSupport.INPUT;
	}

}
