package com.igsl.configmigration.workflow;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Logger;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.database.ConnectionFunction;
import com.atlassian.jira.database.DatabaseAccessor;
import com.atlassian.jira.database.DatabaseConnection;
import com.atlassian.jira.event.WorkflowImportedFromXmlEvent;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraDraftWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;
import com.igsl.configmigration.status.StatusUtil;
import com.igsl.configmigration.workflow.mapper.MapperConfigUtil;
import com.igsl.configmigration.workflow.mapper.WorkflowMapper;
import com.igsl.configmigration.workflow.mapper.WorkflowPart;
import com.igsl.configmigration.workflow.mapper.generated.Arg;
import com.igsl.configmigration.workflow.mapper.generated.Meta;
import com.igsl.configmigration.workflow.mapper.generated.Workflow;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigWrapper;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class WorkflowUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(WorkflowUtil.class);
	private static final WorkflowManager WORKFLOW_MANAGER = ComponentAccessor.getWorkflowManager();
	private static final String DEFAULT_WORKFLOW = "jira";
	private static final DatabaseAccessor DATABASE_ACCESSOR = ComponentAccessor.getComponent(DatabaseAccessor.class);
	
	@Override
	public boolean isDefaultObject(JiraConfigDTO dto) {
		if (dto != null && 
			(
				JiraConfigDTO.NULL_KEY.equals(dto.getUniqueKey()) || 
				DEFAULT_WORKFLOW.equals(dto.getUniqueKey())
			)) {
			return true;
		}
		return false;
	}
	
	public static JiraConfigDTO getWorkflowDetails(Object item) throws Exception {
		JiraConfigDTO result = null;
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		StackTraceElement e = ste[2];
		LOGGER.debug("Invoked from: " + 
				e.getClassName() + "." + e.getMethodName() + 
				" (" + e.getFileName() + "@" + e.getLineNumber() + ")");		
		LOGGER.debug("getWorkflowDetails encountered: " + item.getClass().getCanonicalName());
		if (item instanceof AbstractDescriptor) {
			Class<? extends JiraConfigDTO> c = JiraConfigTypeRegistry.getDTOClass(item.getClass());
			if (c != null) {
				JiraConfigDTO dto = c.newInstance();
				dto.setJiraObject(item);
				result = dto;
			}
		}
		return result;
	}
	
	@Override
	public String getName() {
		return "Workflow";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		JiraWorkflow wf = WORKFLOW_MANAGER.getWorkflow(id);
		if (wf != null) {
			WorkflowDTO item = new WorkflowDTO();
			item.setJiraObject(wf);
			return item;
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		for (JiraWorkflow wf : WORKFLOW_MANAGER.getWorkflows()) {
			if (uniqueKey.equals(wf.getName())) {
				WorkflowDTO item = new WorkflowDTO();
				item.setJiraObject(wf);
				return item;
			}
		}
		return null;
	}
	
	public String remapWorkflowMXML(String originalWorkflowName, String xml, MergeResult result) {
		String mappedXML = xml;
		try {
			Workflow jaxbWf = WorkflowMapper.parseWorkflow(xml);
			if (jaxbWf != null) {
				JXPathContext ctx = JXPathContext.newContext(jaxbWf);
				for (MapperConfigWrapper wrapper : MapperConfigUtil.getMapperConfigs(this.ao).values()) {
					if (wrapper.isDisabled()) {
						continue;
					}
					if (wrapper.getWorkflowName() != null && 
						!wrapper.getWorkflowName().isEmpty() && 
						originalWorkflowName != null) {
						if (!wrapper.getWorkflowName().equals(originalWorkflowName)) {
							continue;
						}
					}
					// Execute mapping
					JiraConfigUtil util = MapperConfigUtil.lookupUtil(wrapper.getObjectType());
					if (util == null) {
						result.addWarning("Unrecognized object type [" + wrapper.getObjectType() + "]");
						continue;
					}
					List<Integer> captureGroups = MapperConfigUtil.parseCaptureGroups(wrapper.getCaptureGroups());
					// For all XPath matches
					Iterator<?> it = ctx.iterate(wrapper.getxPath());
					while (it.hasNext()) {
						WorkflowPart part = (WorkflowPart) it.next();
						if (part == null) {
							continue;
						}
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
							continue;
						}
						Pattern pattern = Pattern.compile(wrapper.getRegex());
						Matcher matcher = pattern.matcher(value);
						StringBuffer newValue = new StringBuffer();
						while (matcher.find()) {
							if (captureGroups == null) {
								String id = matcher.group();
								JiraConfigDTO mappedDTO = MapperConfigUtil.resolveMapping(id, wrapper.getObjectType(), this.importStore);
								if (mappedDTO != null) {
									matcher.appendReplacement(newValue, Matcher.quoteReplacement(mappedDTO.getInternalId()));
								} else {
									result.addWarning("Unable to map " + util.getName() + " id " + id);
								}
							} else {
								Map<Integer, String> replacementMap = new HashMap<>();
								for (int i : captureGroups) {
									if (matcher.groupCount() >= i) {
										String id = matcher.group(i);
										JiraConfigDTO mappedDTO = MapperConfigUtil.resolveMapping(id, wrapper.getObjectType(), this.importStore);
										if (mappedDTO != null) {
											replacementMap.put(i, Matcher.quoteReplacement(mappedDTO.getInternalId()));
										} else {
											result.addWarning("Unable to map " + util.getName() + " id " + id);
										}
									}
								}
								String replacementString = MapperConfigUtil.constructReplacement(wrapper.getReplacement(), matcher.groupCount(), replacementMap);
								matcher.appendReplacement(newValue, replacementString);
							}
						}
						matcher.appendTail(newValue);
						value = newValue.toString();
						// Relace value
						switch (part.getWorkflowPartType()) {
						case ARG:
							((Arg) part).setValue(value);
							break;
						case META:
							((Meta) part).setValue(value);
							break;
						default: 
							// Do nothikng
							 break;
						}						
					}	// For all XPath matches
				} // For all mappings
				// Serialize Workflow
				mappedXML = WorkflowMapper.serializeWorkflow(jaxbWf);
				if (mappedXML == null) {
					result.addWarning("Failed to serialize mapped workflow XML, XML remains unchanged");
					mappedXML = xml;
				}
			}
		} catch (Exception ex) {
			result.addWarning("Failed to serialize mapped workflow XML, XML remains unchanged");
			mappedXML = xml;
		}
		return mappedXML;
	}
	
	private String generateWorkflowHash(WorkflowDTO workflow) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] hash = md.digest(workflow.getName().getBytes("UTF8"));
		StringBuilder tag = new StringBuilder();
		for (byte b : hash) {
			tag.append(String.format("%02x", b));
		}
		LOGGER.debug("WorkflowName: " + workflow.getName() + " => " + tag);
		return tag.toString();
	}
	
	private static final String WORKFLOW_DESIGNER_PROPERTY_SET = "com.atlassian.jira.plugins.jira-workflow-designer";
	private static final long WORKFLOW_DESIGNER_PROPERTY_SET_ID = 1L;
	
	public void setLayoutXML(WorkflowDTO workflow) throws Exception {
		PropertySet ps = OFBizPropertyUtils.getPropertySet(WORKFLOW_DESIGNER_PROPERTY_SET, WORKFLOW_DESIGNER_PROPERTY_SET_ID);
		if (ps != null) {
			LOGGER.debug("Setting layout for workflow: " + workflow.getName());
			LOGGER.debug("Layout size: " + workflow.getLayoutXml().size());
			for (Map.Entry<String, String> entry : workflow.getLayoutXml().entrySet()) {
				ps.setText(entry.getKey(), entry.getValue());
				LOGGER.debug("Set " + entry.getKey() + " = " + entry.getValue());
			}
		}
		// PropertySet is cached... how to flush it? Just update it too?
		PropertySet cachedPS = OFBizPropertyUtils.getCachingPropertySet(WORKFLOW_DESIGNER_PROPERTY_SET, WORKFLOW_DESIGNER_PROPERTY_SET_ID);
		if (cachedPS != null) {
			for (Map.Entry<String, String> entry : workflow.getLayoutXml().entrySet()) {
				cachedPS.setText(entry.getKey(), entry.getValue());
				LOGGER.debug("Cache Set " + entry.getKey() + " = " + entry.getValue());
			}
		}
	}
	
	public Map<String, String> getLayoutXML(WorkflowDTO workflow) {
		Map<String, String> result = new HashMap<>();
		try {
			String hash = generateWorkflowHash(workflow);
			PropertySet ps = OFBizPropertyUtils.getPropertySet(WORKFLOW_DESIGNER_PROPERTY_SET, WORKFLOW_DESIGNER_PROPERTY_SET_ID);
			if (ps != null) {
				LOGGER.debug("PropertySet found");
				for (Object item : ps.getKeys(PropertySet.TEXT)) {
					String key = String.valueOf(item);
					if (key.endsWith(hash)) {
						String value = ps.getText(key);
						result.put(key, value);
					}
				}
			} else {
				LOGGER.debug("PropertySet not found");
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to lookup workflow layout XML", ex);
		}
		return result;
	}

	@Override
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		LOGGER.debug("merge starts");
		StatusUtil statusUtil = (StatusUtil) JiraConfigTypeRegistry.getConfigUtil(StatusUtil.class);
		ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		WorkflowDTO original = null;
		if (oldItem != null) {
			original = (WorkflowDTO) oldItem;
		} else {
			original = (WorkflowDTO) findByUniqueKey(newItem.getUniqueKey(), newItem.getObjectParameters());
		}
		WorkflowDTO src = (WorkflowDTO) newItem;
		// Remap data in XML
		LOGGER.debug("Workflow XML: " + src.getXml());
		src.setXml(remapWorkflowMXML((original == null? null : original.getName()), src.getXml(), result));
		if (original != null) {
			// Update
			WorkflowDescriptor wfDesc = 
					com.atlassian.jira.workflow.WorkflowUtil.convertXMLtoWorkflowDescriptor(src.getXml());
			ConfigurableJiraWorkflow wf = (ConfigurableJiraWorkflow) WORKFLOW_MANAGER.getWorkflow(src.getName());
			if (wf.isActive()) {
				JiraWorkflow draft = WORKFLOW_MANAGER.getDraftWorkflow(src.getName());
				if (draft == null) {
					// Create draft
					draft = WORKFLOW_MANAGER.createDraftWorkflow(currentUser, src.getName());
				}
				WorkflowDescriptor draftDesc = draft.getDescriptor();
				// Copy all data from wfDesc to draftDesc
				// Unlike ConfigurableJiraWorkflow class
				// JiraDraftWorkflow does NOT have .setDescriptor().
				// Instead of trying to figure out how to properly copy everything over
				// Modify it with reflection
				Field descriptorField = JiraDraftWorkflow.class.getSuperclass().getDeclaredField("descriptor");
				descriptorField.setAccessible(true);
				descriptorField.set(draft, wfDesc);
				descriptorField.setAccessible(false);
				WORKFLOW_MANAGER.updateWorkflow(currentUser, draft);
				// Move draft to original
				WORKFLOW_MANAGER.overwriteActiveWorkflow(currentUser, src.getName());
			} else {
				wf.setDescriptor(wfDesc);		
				WORKFLOW_MANAGER.updateWorkflow(currentUser, wf);
			}
			result.setNewDTO(findByUniqueKey(wf.getName()));
		} else {
			WorkflowDescriptor wfDesc = 
					com.atlassian.jira.workflow.WorkflowUtil.convertXMLtoWorkflowDescriptor(src.getXml());
			// Create ConfigurationWorkflow
			ConfigurableJiraWorkflow wf = new ConfigurableJiraWorkflow(src.getName(), WORKFLOW_MANAGER);
			wf.setDescriptor(wfDesc);
			wf.setDescription(src.getDescription());
			// Commit
			WORKFLOW_MANAGER.createWorkflow(currentUser, wf);
			WorkflowDTO created = new WorkflowDTO();
			created.setJiraObject(wf);
			result.setNewDTO(created);
		}
		// Trigger event that workflow layout has been updated
		EventPublisher publisher = ComponentAccessor.getComponent(EventPublisher.class);
		publisher.publish(new WorkflowImportedFromXmlEvent((JiraWorkflow) result.getNewDTO().getJiraObject()));
		// Apply layout
		setLayoutXML(src);
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return WorkflowDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (JiraWorkflow wf : WORKFLOW_MANAGER.getWorkflows()) {
			WorkflowDTO item = new WorkflowDTO();
			item.setJiraObject(wf);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);
		}
		return result;
	}

}
