package com.igsl.configmigration.workflow;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
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
import com.igsl.configmigration.status.StatusDTO;
import com.igsl.configmigration.status.StatusUtil;
import com.igsl.configmigration.workflow.mapper.MapperConfigUtil;
import com.igsl.configmigration.workflow.mapper.WorkflowMapper;
import com.igsl.configmigration.workflow.mapper.WorkflowPart;
import com.igsl.configmigration.workflow.mapper.generated.Arg;
import com.igsl.configmigration.workflow.mapper.generated.Meta;
import com.igsl.configmigration.workflow.mapper.generated.Workflow;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfig;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigWrapper;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class WorkflowUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(WorkflowUtil.class);
	private static final WorkflowManager WORKFLOW_MANAGER = ComponentAccessor.getWorkflowManager();
	private static final String DEFAULT_WORKFLOW = "jira";
	
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
						if (value == null) {
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
