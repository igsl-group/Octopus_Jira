package com.igsl.configmigration.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Logger;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigRef;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.applicationuser.ApplicationUserDTO;
import com.igsl.configmigration.applicationuser.ApplicationUserUtil;
import com.igsl.configmigration.status.StatusDTO;
import com.igsl.configmigration.status.StatusUtil;
import com.igsl.configmigration.workflow.mapper.MapperConfigUtil;
import com.igsl.configmigration.workflow.mapper.WorkflowMapper;
import com.igsl.configmigration.workflow.mapper.WorkflowPart;
import com.igsl.configmigration.workflow.mapper.generated.Arg;
import com.igsl.configmigration.workflow.mapper.generated.Meta;
import com.igsl.configmigration.workflow.mapper.generated.Workflow;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigWrapper;
import com.opensymphony.workflow.loader.StepDescriptor;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class WorkflowDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(WorkflowDTO.class);
	
	private String name;
	private String description;
	private String mode;
	private String displayName;
	private String updateAuthorName;
	private ApplicationUserDTO updateAuthor;
	private Date updatedDate;
	private String xml;
	private Map<String, String> layoutXml = new HashMap<>();
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		JiraWorkflow wf = (JiraWorkflow) obj;
		this.name = wf.getName();
		this.description = wf.getDescription();
		this.displayName = wf.getDisplayName();
		this.mode = wf.getMode();
		this.updateAuthor = new ApplicationUserDTO();
		this.updateAuthor.setJiraObject(wf.getUpdateAuthor());
		this.updateAuthorName = wf.getUpdateAuthorName();
		this.updatedDate = wf.getUpdatedDate();
		this.xml = com.atlassian.jira.workflow.WorkflowUtil.convertDescriptorToXML(wf.getDescriptor());
		this.uniqueKey = this.name;
		WorkflowUtil workflowUtil = (WorkflowUtil) JiraConfigTypeRegistry.getConfigUtil(WorkflowUtil.class);
		this.layoutXml = workflowUtil.getLayoutXML(this);
	}
	
	@Override
	protected void setupRelatedObjects() throws Exception {
		// Locate workflow related objects by using the mappings
		WorkflowUtil workflowUtil = (WorkflowUtil) JiraConfigTypeRegistry.getConfigUtil(WorkflowUtil.class);
		LOGGER.debug("Parsing workflow XML: " + this.name);
		Workflow jaxbWf = WorkflowMapper.parseWorkflow(this.xml);
		if (jaxbWf != null) {
			LOGGER.debug("Workflow XML parsed: " + this.name);
			for (MapperConfigWrapper wrapper : MapperConfigUtil.getMapperConfigs(workflowUtil.getActiveObjects()).values()) {
				LOGGER.debug("Processing mapping: " + wrapper.getDescription());
				if (wrapper.isDisabled()) {
					// Mapper disabled
					LOGGER.debug("Mapping disabled");
					continue;
				}
				if (wrapper.getWorkflowName() != null && !wrapper.getWorkflowName().isEmpty() && !wrapper.getWorkflowName().equals(this.getName())) {
					// Not mapper's target workflow
					LOGGER.debug("Mapping not targeting workflow: " + this.getName());
					continue;
				}
				JXPathContext ctx = JXPathContext.newContext(jaxbWf);
				List<Integer> captureGroups = MapperConfigUtil.parseCaptureGroups(wrapper.getCaptureGroups());
				// For all XPath matches
				LOGGER.debug("XPath: " + wrapper.getxPath() + " in " + this.name);
				Iterator<?> it = ctx.iterate(wrapper.getxPath());
				if (!it.hasNext()) {
					LOGGER.debug("No match for: " + this.name);
					continue;
				}
				LOGGER.debug("Found matches for: " + this.name);
				while (it.hasNext()) {
					WorkflowPart part = (WorkflowPart) it.next();
					if (part == null) {
						LOGGER.debug("WorkflowPart is null");
						continue;
					}
					LOGGER.debug("WorkflowPart found: " + part.getPartDisplayName());
					String value = null;
					switch (part.getWorkflowPartType()) {
					case ARG:
						value = ((Arg) part).getValue();
						break;
					case META:
						value = ((Meta) part).getValue();
						break;
					default: 
						// Do nothikng
						 break;
					}
					if (value == null || value.isEmpty()) {
						LOGGER.debug("Value is null or empty");
						continue;
					}
					LOGGER.debug("Value: " + value);
					Pattern pattern = Pattern.compile(wrapper.getRegex());
					Matcher matcher = pattern.matcher(value);
					while (matcher.find()) {
						if (captureGroups == null) {
							// The whole expression 
							String id = matcher.group();
							LOGGER.debug("Whole value is ID: " + id);
							JiraConfigDTO mappedDTO = MapperConfigUtil
									.resolveMapping(id, wrapper.getObjectType(), wrapper.getSearchType(), workflowUtil.getExportStore());
							if (mappedDTO != null) {
								LOGGER.debug("DTO: " + mappedDTO.getUniqueKey());
								LOGGER.debug("Result: " + this.addRelatedObject(mappedDTO));
								mappedDTO.addReferencedObject(this);
							}
						} else {
							// Just the specified groups
							for (int i : captureGroups) {
								if (matcher.groupCount() >= i) {
									String id = matcher.group(i);
									LOGGER.debug("Value ID: " + id);
									JiraConfigDTO mappedDTO = MapperConfigUtil
											.resolveMapping(id, wrapper.getObjectType(), wrapper.getSearchType(), workflowUtil.getExportStore());
									if (mappedDTO != null) {
										LOGGER.debug("DTO: " + mappedDTO.getUniqueKey());
										LOGGER.debug("Result: " + this.addRelatedObject(mappedDTO));
										mappedDTO.addReferencedObject(this);
									}
								}
							}
						} // Capture group options
					} // For all regex matches
				} // For all XPath matches
			} // For all mappings
		} // If workflow can be parsed
		LOGGER.debug(this.name + " relatedObjects size: " + this.relatedObjects.size());
		for (JiraConfigRef ref : this.relatedObjects.values()) {
			LOGGER.debug(ref.getUtil() + " => " + ref.getUniqueKey());
		}
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Display Name", new JiraConfigProperty(this.displayName));
		r.put("Mode", new JiraConfigProperty(this.mode));
		r.put("Update Author", new JiraConfigProperty(ApplicationUserUtil.class, this.updateAuthor));
		r.put("Update Author Name", new JiraConfigProperty(this.updateAuthorName));
		r.put("Updated Date", new JiraConfigProperty(this.updatedDate));
		r.put("XML", new JiraConfigProperty(this.xml));
		r.put("Layout", new JiraConfigProperty(this.layoutXml));
		WorkflowUtil util = (WorkflowUtil) JiraConfigTypeRegistry.getConfigUtil(this.getUtilClass());
		if (util != null) {
			MergeResult mr = new MergeResult();
			String mappedXML = util.remapWorkflowMXML(this.name, this.xml, mr);
			r.put("Mapped XML", new JiraConfigProperty(mappedXML));
		}		
		return r;
	}
	
	@Override
	public String getInternalId() {
		return this.getName();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getXml",
				"getName");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return com.igsl.configmigration.workflow.WorkflowUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return JiraWorkflow.class;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getUpdateAuthorName() {
		return updateAuthorName;
	}

	public void setUpdateAuthorName(String updateAuthorName) {
		this.updateAuthorName = updateAuthorName;
	}

	public ApplicationUserDTO getUpdateAuthor() {
		return updateAuthor;
	}

	public void setUpdateAuthor(ApplicationUserDTO updateAuthor) {
		this.updateAuthor = updateAuthor;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public Map<String, String> getLayoutXml() {
		return layoutXml;
	}

	public void setLayoutXml(Map<String, String> layoutXml) {
		this.layoutXml = layoutXml;
	}

}
