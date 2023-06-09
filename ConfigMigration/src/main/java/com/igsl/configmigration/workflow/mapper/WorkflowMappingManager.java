package com.igsl.configmigration.workflow.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	
	public static final String PAGE_URL = "/secure/admin/plugins/handler/WorkflowMappingManager.jspa";
	private static final String SERVLET_URL = "/plugins/servlet/configmigrationmapping";
	
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_MAPPING = "mapping";
	
	private static final String ACTION_EXPORT = "export";
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
		return this.getServletContext().getContextPath() + 
				"/secure/admin/plugins/handler/WorkflowMapper.jspa?action=loadMapping&mapping=" + id;
	}
	
	private String downloadURL;
	public String getDownloadURL() {
		return downloadURL;
	}
	
	private List<String> downloadParameters;
	public List<String> getDownloadParameters() {
		return downloadParameters;
	}
	
	@Override
	protected void doValidation() {
		LOGGER.debug("doValidation");
	}

	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		HttpServletRequest req = this.getHttpRequest();
		
		// Clear download
		this.downloadURL = null;
		this.downloadParameters = null;
		
		String action = req.getParameter(PARAM_ACTION);
		LOGGER.debug("action: " + action);
		
		String[] mappingList = req.getParameterValues(PARAM_MAPPING);
		if (mappingList != null) {
			if (ACTION_DELETE.equals(action)) {
				for (String mapping : mappingList) {
					MapperConfigWrapper wrapper = MapperConfigUtil.getMapperConfigById(ao, mapping);
					if (wrapper != null) {
						MapperConfigUtil.deleteMapperConfig(ao, wrapper);
					}
				}
			} else if (ACTION_ENABLE.equals(action)) {
				for (String mapping : mappingList) {
					MapperConfigWrapper wrapper = MapperConfigUtil.getMapperConfigById(ao, mapping);
					if (wrapper != null) {
						wrapper.setDisabled(false);
						MapperConfigUtil.saveMapperConfig(ao, wrapper);
					}
				}
			} else if (ACTION_DISABLE.equals(action)) {
				for (String mapping : mappingList) {
					MapperConfigWrapper wrapper = MapperConfigUtil.getMapperConfigById(ao, mapping);
					if (wrapper != null) {
						wrapper.setDisabled(true);
						MapperConfigUtil.saveMapperConfig(ao, wrapper);
					}
				}
			} else if (ACTION_EXPORT.equals(action)) {
				this.downloadURL = this.getServletContext().getContextPath() + SERVLET_URL;
				this.downloadParameters = new ArrayList<>();
				String[] idList = req.getParameterValues(PARAM_MAPPING);
				for (String id : idList) {
					downloadParameters.add(id);
				}
			}
		}
		
		return JiraWebActionSupport.INPUT;
	}

}
