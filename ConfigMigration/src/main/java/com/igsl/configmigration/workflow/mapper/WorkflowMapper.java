package com.igsl.configmigration.workflow.mapper;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowModuleDescriptor;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.PluginAccessorHelper;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigSearchType;
import com.igsl.configmigration.JiraConfigTypeRegistry;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.workflow.mapper.generated.Arg;
import com.igsl.configmigration.workflow.mapper.generated.Meta;
import com.igsl.configmigration.workflow.mapper.generated.Workflow;
import com.igsl.configmigration.workflow.mapper.v1.MapperConfigWrapper;

public class WorkflowMapper extends JiraWebActionSupport {

	public static final PluginAccessor PLUGIN_ACCESSOR = ComponentAccessor.getPluginAccessor();
	public static final ModuleDescriptorFactory MODULE_DESCRIPTOR_FACTORY = ComponentAccessor.getComponent(ModuleDescriptorFactory.class);
	public static final String WORKFLOW_FUNCTION_DESCRIPTOR_TYPE = "workflow-function";

	private static final String SESSION_DATA = WorkflowMapper.class.getCanonicalName() + ".SessionData";
	
	static class SessionData {
		public String selectedWorkflow;
		public String xml;
		public Workflow workflow;
		public Map<String, WorkflowPartWrapper> workflowPartsRegistry = new HashMap<>();	// Key is hashCode
		public WorkflowPartWrapper part;	// Editing part
		public MapperConfigWrapper mapping;	// Mapping being edited
		public Iterator<?> partIterator;	// Matches for mapping
		public String partIteratorXPath;	// XPath used for search
		public Map<String, Map<String, JiraConfigProperty>> lookupResult;
		public List<JiraConfigSearchType> searchTypes = new ArrayList<>();
	}
	private SessionData sessionData;
	
	private void getSessionData(HttpServletRequest req) {
		Object data = req.getSession().getAttribute(SESSION_DATA);
		if (data != null && data instanceof SessionData) {
			this.sessionData = (SessionData) data;
		} else {
			// Create new
			this.sessionData = new SessionData();
			saveSessionData(req);
		}
	}
	
	private void saveSessionData(HttpServletRequest req) {
		req.getSession().setAttribute(SESSION_DATA, this.sessionData);
	}
	
	private static final Logger LOGGER = Logger.getLogger(WorkflowMapper.class);
	private static final WorkflowManager MANAGER = ComponentAccessor.getWorkflowManager();
	
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_MAPPING_UPDATED = "mappingUpdated";
	
	// Load workflow action
	private static final String ACTION_LOAD_WORKFLOW = "loadWorkflow";
	private static final String PARAM_WORKFLOW = "workflow";
	
	// Edit part action
	private static final String ACTION_LOAD_PART = "loadPart";
	private static final String PARAM_PART = "part";
	
	// Edit mapping
	private static final String ACTION_LOAD_MAPPING = "loadMapping";
	private static final String ACTION_CREATE_MAPPING = "createMapping";
	private static final String ACTION_SAVE_MAPPING = "saveMapping";
	private static final String ACTION_DELETE_MAPPING = "deleteMapping";
	private static final String PARAM_MAPPING = "mapping";
	private static final String PARAM_MAPPING_DESCRIPTION = "mappingDesc";
	private static final String PARAM_MAPPING_OBJECT_TYPE = "mappingObjectType";
	private static final String PARAM_MAPPING_SEARCH_TYPE = "mappingSearchType";
	private static final String PARAM_MAPPING_REGEX = "mappingRegex";
	private static final String PARAM_MAPPING_CAPTURE_GROUPS = "mappingCaptureGroups";
	private static final String PARAM_MAPPING_REPLACEMENT = "mappingReplacement";
	private static final String PARAM_MAPPING_DISABLED = "mappingDisabled";
	private static final String PARAM_MAPPING_XPATH = "mappingXPath";
	private static final String PARAM_MAPPING_WORKFLOW_NAME = "mappingWorkflowName";
	
	// Part matching
	private static final String ACTION_NEXT_PART = "nextPart";
	
	// Lookup test
	private static final String ACTION_LOOKUP = "lookup";
	
	/**
	 * Notes:
	 * Function to load a workflow as example, extracting list of items that can be mapped.
	 * Has an internal list of mapping items.
	 * Has an user-customizable list of mapping items.
	 * 
	 * For each item, check the Arg items and provide function to test map them.
	 */
	
	private static final String XML_HEADER = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"<!DOCTYPE workflow PUBLIC \"-//OpenSymphony Group//DTD OSWorkflow 2.8//EN\" \"http://www.opensymphony.com/osworkflow/workflow_2_8.dtd\">\r\n";
	
	private static SAXParserFactory saxParserFactory; 
	
	static {
		saxParserFactory = SAXParserFactory.newInstance();
		// Disable DTD validation
		try {
			saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (SAXNotRecognizedException | SAXNotSupportedException | ParserConfigurationException e) {
			LOGGER.error("Error configuring SAX Parser", e);
		}
	}
	
	public List<JiraConfigSearchType> getSearchTypes() {
		return this.sessionData.searchTypes;
	}
	
	public String getSelectedWorkflow() {
		return this.sessionData.selectedWorkflow;
	}
	
	public String getXml() {
		return this.sessionData.xml;
	}
	
	public Workflow getWorkflow() {
		return this.sessionData.workflow;
	}
	
	private String searchResult;
	public String getSearchResult() {
		return this.searchResult;
	}
	
	@ComponentImport
	private final ActiveObjects ao;

	private static final long serialVersionUID = 1L;

	public WorkflowMapper(@ComponentImport ActiveObjects ao) {
		LOGGER.debug("Inject ActiveObjects: " + ao);
		this.ao = ao;
	}
	
	/**
	 * Get list of published workflows
	 * @return Collection of JiraWorkflow
	 */
	public Collection<JiraWorkflow> getWorkflows() {
		return MANAGER.getWorkflows();
	}
	
	public static Workflow parseWorkflow(String xml) throws Exception {
		Source xmlSource = new SAXSource(saxParserFactory.newSAXParser().getXMLReader(),
                new InputSource(new StringReader(xml)));
		JAXBContext ctx = JAXBContext.newInstance(Workflow.class);
		Unmarshaller parser = ctx.createUnmarshaller();
		Workflow result = (Workflow) parser.unmarshal(xmlSource);
		return result;
	}
	
	public static Workflow parseWorkflow(JiraWorkflow wf) throws Exception {
		String xml = WorkflowUtil.convertDescriptorToXML(wf.getDescriptor());
		return parseWorkflow(xml);
	}

	public Map<String, WorkflowPartWrapper> getMappableWorkflowParts() {
		return this.sessionData.workflowPartsRegistry;
	}
	
	private void registerWorkflowParts(Workflow wf) {
		this.sessionData.workflowPartsRegistry.clear();
		registerWorkflowParts((WorkflowPart) wf, "");
	}
	private void registerWorkflowParts(WorkflowPart part, String parentXPath) {
		this.sessionData.workflowPartsRegistry.put(Integer.toString(part.hashCode()), new WorkflowPartWrapper(part, parentXPath));
		for (WorkflowPart child : part.getChildren()) {
			String path;
			if (parentXPath != null && parentXPath.length() != 0) {
				path = parentXPath + "/" + part.getPartXPath();
			} else {
				path = part.getPartXPath();
			}
			registerWorkflowParts(child, path);
		}
	}
	
	public WorkflowPartWrapper getEditingWorkflowPartWrapper() {
		return this.sessionData.part;
	}
	
	public Map<String, String> getObjectTypes() {
		Map<String, String> result = new TreeMap<>();
		for (JiraConfigUtil util : JiraConfigTypeRegistry.getConfigUtilList(false)) {
			result.put(util.getName(), util.getClass().getCanonicalName());
		}
		return result;
	}
	
	public Map<String, MapperConfigWrapper> getMappings() {
		return MapperConfigUtil.getMapperConfigs(this.ao);
	}
	
	public MapperConfigWrapper getMapping() {
		return this.sessionData.mapping;
	}
	
	public Map<String, Map<String, JiraConfigProperty>> getLookupResult() {
		return this.sessionData.lookupResult;
	}
	
	public static String serializeWorkflow(Workflow wf) throws Exception {
		JAXBContext ctx = JAXBContext.newInstance(Workflow.class);
		Marshaller marshaller = ctx.createMarshaller();
		// Set XML header
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		sw.write(XML_HEADER);
		marshaller.marshal(wf, sw);
		return sw.toString();
	}
	
	/**
	 * Jira workflow stores a workflow function with its class name.
	 * 
	 * But display name and description are actually stored in the workflow function's factory.
	 * The mapping of workflow function to its factory is stored in plugin manifest only.
	 * 
	 * This method takes workflow function class name and try to find a matching display name.
	 * @param className
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getFunctionDisplayName(String className) {
		Collection<ModuleDescriptor> moduleDescriptors = 
				PluginAccessorHelper.getEnabledModuleDescriptorsByType(PLUGIN_ACCESSOR, MODULE_DESCRIPTOR_FACTORY, WORKFLOW_FUNCTION_DESCRIPTOR_TYPE);
		for (ModuleDescriptor desc : moduleDescriptors) {
			/*
			 * Note: 
			 * Jira's API did NOT expose the function class anywhere from the ModuleDescriptor.
			 * The function class is stored in a protected field AbstractWorkflowModuleDescriptor.implementationClass (type Class).
			 * 
			 * So our only solution is to either read it with reflection, or parse the plugin manifest.
			 * We choose the former.
			 */
			if (desc instanceof AbstractWorkflowModuleDescriptor) {
				try {
					Field field = AbstractWorkflowModuleDescriptor.class.getDeclaredField("implementationClass");
					if (field != null) {
						field.setAccessible(true);
						Class<?> functionClass = (Class<?>) field.get(desc);
						field.setAccessible(false);
						if (functionClass.getCanonicalName().equals(className)) {
							return desc.getDisplayName();
						}
					}
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					LOGGER.error("Failed to read function-class", e);
				}
			}
		}
		return className;
	}
	
	private void refreshLookupResult() {
		this.sessionData.lookupResult = null;
		if (this.sessionData.part != null && this.sessionData.mapping != null) {
			WorkflowPart part = this.sessionData.part.getWorkflowPart();
			String value = null;
			switch (part.getWorkflowPartType()) {
			case ARG: 
				value = ((Arg) part).getValue();
				break;
			case META:
				value = ((Meta) part).getValue();
				break;
			default:
				// Not supported
				break;
			}
			if (value != null) {
				List<String> valueList = new ArrayList<>();
				Pattern pattern = Pattern.compile(this.sessionData.mapping.getRegex());
				Matcher matcher = pattern.matcher(value);
				List<Integer> captureGroups = MapperConfigUtil.parseCaptureGroups(this.sessionData.mapping.getCaptureGroups());
				while (matcher.find()) {
					if (captureGroups == null) {
						valueList.add(matcher.group());
					} else {
						for (int i : captureGroups) {
							if (matcher.groupCount() >= i) {
								valueList.add(matcher.group(i));
							}
						}
					}
				} 
				if (valueList.size() != 0) {
					this.sessionData.lookupResult = new TreeMap<String, Map<String, JiraConfigProperty>>();
					for (String v : valueList) {
						if (!this.sessionData.lookupResult.containsKey(v)) {
							if (v != null && v.length() != 0) {
								JiraConfigDTO dto = MapperConfigUtil.lookupDTO(v, this.sessionData.mapping.getObjectType(), this.sessionData.mapping.getSearchType());
								if (dto != null) {
									this.sessionData.lookupResult.put(v, dto.getConfigProperties());
								} else {
									this.sessionData.lookupResult.put(v, null);
								}
							} else {
								this.sessionData.lookupResult.put(v, null);
							}
						}
					}
				}
			} 
		}
	}
	
	@Override
	protected void doValidation() {
		LOGGER.debug("doValidation");
	}

	@Override
	protected String doExecute() throws Exception {
		LOGGER.debug("doExecute");
		HttpServletRequest req = this.getHttpRequest();
		getSessionData(req);
		
//		QueryDslAccessor test = ComponentAccessor.getComponent(QueryDslAccessor.class);
//		LOGGER.debug("Test: " + test);
//		test.executeQuery(new QueryCallback<String>() {
//			@Override
//			public String runQuery(DbConnection con) {
//				return null;
//			}
//		});
		
		String action = req.getParameter(PARAM_ACTION);
		LOGGER.debug("action: " + action);
		
		// Check mapping data
		if (this.sessionData.mapping != null) {
			// Double check mapping still exists
			if (this.sessionData.mapping.getId() != null) {
				// Not a new ID, check if it still exists, if not, wipe it from memory
				if (MapperConfigUtil.getMapperConfigById(this.ao, this.sessionData.mapping.getId()) == null) {
					this.sessionData.mapping = null;
				} 
			}
			if (this.sessionData.mapping != null) {
				Boolean mappingUpdated = Boolean.parseBoolean(req.getParameter(PARAM_MAPPING_UPDATED));
				if (mappingUpdated) {
					// Update fields
					this.sessionData.mapping.setUpdated(true);
					this.sessionData.mapping.setRegex(req.getParameter(PARAM_MAPPING_REGEX));
					this.sessionData.mapping.setCaptureGroups(req.getParameter(PARAM_MAPPING_CAPTURE_GROUPS));
					this.sessionData.mapping.setReplacement(req.getParameter(PARAM_MAPPING_REPLACEMENT));
					Boolean disabled = Boolean.parseBoolean(req.getParameter(PARAM_MAPPING_DISABLED));
					this.sessionData.mapping.setDisabled(disabled);
					this.sessionData.mapping.setDescription(req.getParameter(PARAM_MAPPING_DESCRIPTION));
					this.sessionData.mapping.setObjectType(req.getParameter(PARAM_MAPPING_OBJECT_TYPE));
					this.sessionData.mapping.setSearchType(req.getParameter(PARAM_MAPPING_SEARCH_TYPE));
					this.sessionData.mapping.setxPath(req.getParameter(PARAM_MAPPING_XPATH));
					this.sessionData.mapping.setWorkflowName(req.getParameter(PARAM_MAPPING_WORKFLOW_NAME));
				}
			}
		}
		
		if (ACTION_LOAD_WORKFLOW.equals(action)) {
			this.sessionData.selectedWorkflow = req.getParameter(PARAM_WORKFLOW);
			LOGGER.debug("Selecting workflow: " + this.sessionData.selectedWorkflow);
			this.sessionData.workflow = null;
			this.sessionData.xml = null;
			this.sessionData.part = null;
			this.sessionData.workflowPartsRegistry.clear();
			this.sessionData.partIterator = null;
			this.sessionData.partIteratorXPath = null;
			if (this.sessionData.selectedWorkflow != null && this.sessionData.selectedWorkflow.length() != 0) {
				JiraWorkflow workflow = MANAGER.getWorkflow(this.sessionData.selectedWorkflow);
				if (workflow != null) {
					this.sessionData.workflow = parseWorkflow(workflow);
					this.sessionData.xml = serializeWorkflow(this.sessionData.workflow);
					registerWorkflowParts(this.sessionData.workflow);
				}
			}
		} else if (ACTION_LOAD_PART.equals(action)) {
			WorkflowPartWrapper part = this.sessionData.workflowPartsRegistry.get(req.getParameter(PARAM_PART));
			if (part != null) {
				this.sessionData.part = part;
				this.sessionData.mapping = MapperConfigUtil.getMapperConfigByXPath(this.ao, part.getXPath());
				if (this.sessionData.mapping == null) {
					// No existing mapping, create a blank one with prefilled XPath
					this.sessionData.mapping = new MapperConfigWrapper();
					this.sessionData.mapping.setxPath(this.sessionData.part.getXPath());
				}
			}
		} else if (ACTION_LOAD_MAPPING.equals(action)) {
			String mappingId = req.getParameter(PARAM_MAPPING);
			if (mappingId != null && mappingId.length() != 0) {
				this.sessionData.mapping = MapperConfigUtil.getMapperConfigById(this.ao, mappingId);
			} else {
				this.sessionData.mapping = null;
			}
			this.sessionData.part = null;
			this.sessionData.partIterator = null;
			this.sessionData.partIteratorXPath = null;
		} else if (ACTION_CREATE_MAPPING.equals(action)) {
			this.sessionData.mapping = new MapperConfigWrapper();
			this.sessionData.partIterator = null;
			this.sessionData.partIteratorXPath = null;
		} else if (ACTION_SAVE_MAPPING.equals(action)) {
			if (this.sessionData.mapping != null) {
				MapperConfigUtil.saveMapperConfig(this.ao, this.sessionData.mapping);
				this.sessionData.mapping.setUpdated(false);
			}
		} else if (ACTION_DELETE_MAPPING.equals(action)) {
			if (this.sessionData.mapping != null) {
				MapperConfigUtil.deleteMapperConfig(this.ao, this.sessionData.mapping);
				this.sessionData.mapping = null;
				this.sessionData.partIterator = null;
				this.sessionData.partIteratorXPath = null;
			}
		} else if (ACTION_NEXT_PART.equals(action)) {
			this.searchResult = "";
			String xPath = req.getParameter(PARAM_MAPPING_XPATH);
			if (this.sessionData.workflow != null && this.sessionData.mapping != null) {
				if (this.sessionData.partIteratorXPath == null ||
					!this.sessionData.partIteratorXPath.equals(xPath)) {	
					// Need a new search
					this.sessionData.partIterator = null;
				}
				this.sessionData.partIteratorXPath = xPath;
				if (this.sessionData.partIteratorXPath != null && 
					this.sessionData.partIteratorXPath.length() != 0) {
					if (this.sessionData.partIterator == null) {
						LOGGER.debug("starting new search: " + this.sessionData.partIteratorXPath);
						// Start new search
						JXPathContext ctx = JXPathContext.newContext(this.sessionData.workflow);
						try { 
							this.sessionData.partIterator = ctx.iterate(this.sessionData.partIteratorXPath);
						} catch (Exception ex) {
							this.sessionData.part = null;
							this.sessionData.partIterator = null;
							this.searchResult = "XPath is invalid";
						}
					} else {
						LOGGER.debug("continuing search: " + this.sessionData.partIteratorXPath);
					}
					// Find next result
					if (this.sessionData.partIterator.hasNext()) {
						WorkflowPart part = (WorkflowPart) this.sessionData.partIterator.next();
						this.sessionData.part = this.sessionData.workflowPartsRegistry.get(Integer.toString(part.hashCode()));
						this.searchResult = "Next match located";
					} else {
						// No more result
						this.sessionData.part = null;
						this.sessionData.partIterator = null;
						this.searchResult = "No match found";
					}
				} else {
					this.searchResult = "Please enter a XPath";
				}
			} else {
				this.searchResult = "Please select a workflow";
			}
		} else if (ACTION_LOOKUP.equals(action)) {
			// Do nothing
		}
		refreshLookupResult(); // Lookup is always refreshed, all actions impact it
		
		// Refresh search type list
		if (this.sessionData.mapping != null) {
			JiraConfigUtil util = JiraConfigTypeRegistry.getConfigUtil(this.sessionData.mapping.getObjectType());
			if (util != null) {
				this.sessionData.searchTypes = util.getSearchTypes();
			} else {
				this.sessionData.searchTypes = null;
			}
		} else {
			this.sessionData.searchTypes = null;
		}

		return JiraWebActionSupport.INPUT;
	}

}
