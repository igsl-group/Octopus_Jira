package com.igsl.configmigration.workflow.mapper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Logger;

import com.igsl.configmigration.workflow.mapper.generated.Action;
import com.igsl.configmigration.workflow.mapper.generated.Actions;
import com.igsl.configmigration.workflow.mapper.generated.Arg;
import com.igsl.configmigration.workflow.mapper.generated.CommonAction;
import com.igsl.configmigration.workflow.mapper.generated.CommonActions;
import com.igsl.configmigration.workflow.mapper.generated.Condition;
import com.igsl.configmigration.workflow.mapper.generated.Conditions;
import com.igsl.configmigration.workflow.mapper.generated.ExternalPermissions;
import com.igsl.configmigration.workflow.mapper.generated.Function;
import com.igsl.configmigration.workflow.mapper.generated.GlobalActions;
import com.igsl.configmigration.workflow.mapper.generated.GlobalConditions;
import com.igsl.configmigration.workflow.mapper.generated.InitialActions;
import com.igsl.configmigration.workflow.mapper.generated.Join;
import com.igsl.configmigration.workflow.mapper.generated.Joins;
import com.igsl.configmigration.workflow.mapper.generated.Meta;
import com.igsl.configmigration.workflow.mapper.generated.ObjectFactory;
import com.igsl.configmigration.workflow.mapper.generated.Permission;
import com.igsl.configmigration.workflow.mapper.generated.PostFunctions;
import com.igsl.configmigration.workflow.mapper.generated.PreFunctions;
import com.igsl.configmigration.workflow.mapper.generated.Register;
import com.igsl.configmigration.workflow.mapper.generated.Registers;
import com.igsl.configmigration.workflow.mapper.generated.RestrictTo;
import com.igsl.configmigration.workflow.mapper.generated.Result;
import com.igsl.configmigration.workflow.mapper.generated.Results;
import com.igsl.configmigration.workflow.mapper.generated.Split;
import com.igsl.configmigration.workflow.mapper.generated.Splits;
import com.igsl.configmigration.workflow.mapper.generated.Step;
import com.igsl.configmigration.workflow.mapper.generated.Steps;
import com.igsl.configmigration.workflow.mapper.generated.TriggerFunction;
import com.igsl.configmigration.workflow.mapper.generated.TriggerFunctions;
import com.igsl.configmigration.workflow.mapper.generated.UnconditionalResult;
import com.igsl.configmigration.workflow.mapper.generated.Validator;
import com.igsl.configmigration.workflow.mapper.generated.Validators;
import com.igsl.configmigration.workflow.mapper.generated.Workflow;

public interface WorkflowPart {

	public static final Logger LOGGER = Logger.getLogger(WorkflowPart.class);
	
	public static final String ATTRIBUTE_ID = "id";
	public static final String TYPE_CLASS = "class";
	public static final String ARG_CLASS_NAME = "class.name";
	public static final String ATTRIBUTE_ARG_CLASS_NAME = "NestedArgClassName";

	/**
	 * Settings related to JAXB generated classes
	 */
	public enum WorkflowPartType {
		ACTION(Action.class, "name"),
		ACTIONS(Actions.class),
		ARG(Arg.class, "name"),
		COMMON_ACTION(CommonAction.class),
		COMMON_ACTIONS(CommonActions.class),
		CONDITION(Condition.class, "negate", "type", ATTRIBUTE_ARG_CLASS_NAME),
		CONDITIONS(Conditions.class, "type"),
		EXTERNAL_PERMISSIONS(ExternalPermissions.class),
		FUNCTION(Function.class, "type", ATTRIBUTE_ARG_CLASS_NAME),
		GLOBAL_ACTIONS(GlobalActions.class),
		GLOBAL_CONDITIONS(GlobalConditions.class),
		INITIAL_ACTIONS(InitialActions.class),
		JOIN(Join.class),
		JOINS(Joins.class),
		META(Meta.class, "name"),
		OBJECT_FACTORY(ObjectFactory.class),
		PERMISSION(Permission.class, "id", "name"),
		POST_FUNCTIONS(PostFunctions.class),
		PRE_FUNCTIONS(PreFunctions.class),
		REGISTER(Register.class, "variableName"),
		REGISTERS(Registers.class),
		RESTRICT_TO(RestrictTo.class),
		RESULT(Result.class),
		RESULTS(Results.class),
		SPLIT(Split.class),
		SPLITS(Splits.class),
		STEP(Step.class, "name"),
		STEPS(Steps.class),
		TRIGGER_FUNCTION(TriggerFunction.class),
		TRIGGER_FUNCTIONS(TriggerFunctions.class),
		UNCONDITIONAL_RESULT(UnconditionalResult.class),
		VALIDATOR(Validator.class, "type", ATTRIBUTE_ARG_CLASS_NAME),
		VALIDATORS(Validators.class),
		WORKFLOW(Workflow.class);
		private Class<?> cls;
		private List<String> identifyingAttributes;
		private WorkflowPartType(Class<?> cls, String... attributes) {
			this.cls = cls;
			this.identifyingAttributes = new ArrayList<>();
			if (attributes != null) {
				this.identifyingAttributes.addAll(Arrays.asList(attributes));
			}
		}
		public static Comparator<WorkflowPart> getComparator() {
			return new Comparator<WorkflowPart>() {
				private int getOrder(WorkflowPart part) {
					if (part == null) {
						return 0;
					} else if (part instanceof Meta) {
						return 1;
					} else if (part instanceof Arg) {
						return 2;
					} else {
						return 3;
					}
				}				
				@Override
				public int compare(WorkflowPart o1, WorkflowPart o2) {
					int order1 = getOrder(o1);
					int order2 = getOrder(o2);
					if (order1 == order2) {
						if (o1 == null && o2 == null) {
							return 0;
						} else if (o1 == null && o2 != null) {
							return -1;
						} else if (o1 != null && o2 == null) {
							return 1;
						} else if (o1 != null && o2 != null) {
							return o1.getPartDisplayName().compareTo(o2.getPartDisplayName());
						}
					} else {
						return Integer.compare(order1, order2);
					}
					return 0;
				}
			};
		}
		public static WorkflowPartType parse(WorkflowPart part) {
			if (part != null) {
				for (WorkflowPartType type : WorkflowPartType.values()) {
					if (type.cls.equals(part.getClass())) {
						return type;
					}
				}
			}
			return null;
		}
		public Class<?> getWorkflowPartClass() {
			return this.cls;
		}
		public List<String> getIdentifyingAttributes() {
			return this.identifyingAttributes;
		}
		public String getNodeName() {
			switch (this) {
			case WORKFLOW:
				return "";
			default:
				String name = this.cls.getSimpleName();
				return name.substring(0, 1).toLowerCase() + name.substring(1);
			}
		}
		public static String getDisplayName(WorkflowPart part) {
			WorkflowPartType partType = WorkflowPartType.parse(part);
			JXPathContext ctx = JXPathContext.newContext(part);
			ctx.setLenient(true);
			switch (partType) {
			case ACTION:
				Action action = (Action) part;
				return "Action: " + action.getName();
			case ARG: 
				Arg arg = (Arg) part;
				return "Arg: " + arg.getName();
			case FUNCTION:
				return "Function: " + WorkflowMapper.getFunctionDisplayName(part.getPartArgClassName());
			case META:
				Meta meta = (Meta) part;
				return "Meta: " + meta.getName();
			case STEP:
				Step step = (Step) part;
				return "Step: " + step.getName();
			case VALIDATOR:
				Validator validator = (Validator) part;
				return "Validator: " + validator.getPartArgSimpleClassName();
			default:
				// Split camel class name, then capitalize first letter
				String name = partType.cls.getSimpleName().replaceAll("([A-Z])", " $1");
				return name.substring(0, 1).toUpperCase() + name.substring(1);
			}
		}
	}
	
	/**
	 * Create JXPathContext of this WorkflowPart. 
	 * Preferrably cache this and reuse?
	 * @return JXPathContext
	 */
	public default JXPathContext getJXPathContext() {
		JXPathContext ctx = JXPathContext.newContext(this);
		ctx.setLenient(true);
		return ctx;
	}

	/**
	 * Get XPath value relative to this WorkflowPart
	 * @param ctx JXPathContext of this WorkflowPart
	 * @param xPath XPath
	 * @return String
	 */
	public default String getPartAttribute(String xPath) {
		JXPathContext ctx = this.getJXPathContext();
		String result = (String) ctx.getValue(xPath, String.class);
		if (result != null) {
			return result;
		}
		return "";
	}
	
	/**
	 * Get Simple class name of class.name value in Arg contained by this WorkflowPart
	 * @param ctx JXPathContext of this WorkflowPart
	 * @return String
	 */
	public default String getPartArgSimpleClassName() {
		String className = getPartArgClassName();
		String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
		return simpleClassName;
	}
	
	/**
	 * Get class.name value in Arg contained by this WorkflowPart
	 * @param ctx JXPathContext of this WorkflowPart
	 * @return String
	 */
	public default String getPartArgClassName() {
		return getPartAttribute("arg[name='" + ARG_CLASS_NAME + "']/@value");
	}
	
	/**
	 * Get XPath selector of this WorkflowPart's Arg containing class.name, if this WorkflowPart has type attribute equal to TYPE_CLASS.
	 * @param ctx JXPathContext of this WorkflowPart
	 * @return String
	 */
	public default String getPartClassNameFilter(JXPathContext ctx) {
		String argFilter = "";
		String type = getPartAttribute("type");
		String className = getPartArgClassName();
		if (TYPE_CLASS.equals(type) && className != null) {
			argFilter = "[arg[name='" + ARG_CLASS_NAME + "'][value='" + className + "']]";
		}
		return argFilter;
	}
	
	/**
	 * Create XPath selector for this WorkflowPart.
	 * The selectors used are based on WorkflowPartType.getIdentifyingAttributes().
	 * @return String
	 */
	public default String makeXPathSelector() {
		String filter = "";
		WorkflowPartType partType = WorkflowPartType.parse(this);
		List<String> attrList = partType.getIdentifyingAttributes();
		JXPathContext ctx = JXPathContext.newContext(this);
		ctx.setLenient(true);
		for (String attr : attrList) {
			if (ATTRIBUTE_ARG_CLASS_NAME.equals(attr)) {
				filter += getPartClassNameFilter(ctx);
			} else {
				String value = (String) ctx.getValue(attr, String.class);
				if (value != null) {
					filter += "[" + attr + "='" + value + "']";
				}
			}
		}
		return filter;
	}
	
	/**
	 * Return absolute XPath of this WorkflowPart.
	 * @param parentXPath Parent's XPath, empty or null if at top level
	 * @return String
	 */
	public default String getAbsoluteXPath(String parentXPath) {
		if (parentXPath != null && parentXPath.length() != 0) {
			return parentXPath + "/" + getPartXPath();
		} else {
			return getPartXPath();
		}
	}
	
	/**
	 * Return relative XPath of this WorkflowPart, with selector to uniquely identify it.
	 * @return String
	 */
	public default String getPartXPath() {
		WorkflowPartType partType = getWorkflowPartType();
		return partType.getNodeName() + makeXPathSelector();
	}

	/**
	 * Check if this WorkflowPart contains mappable attributes
	 * @return boolean
	 */
	public default boolean isPartMappable() {
		WorkflowPartType partType = getWorkflowPartType();
		switch (partType) {
		case ARG: 
			Arg arg = (Arg) this;
			if (ARG_CLASS_NAME.equals(arg.getName())) {
				return false;
			}
			return true;
		case META:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Get display name of this WorkflowPart
	 * @return String
	 */
	public default String getPartDisplayName() {
		return WorkflowPartType.getDisplayName(this);
	}
	
	public default WorkflowPartType getWorkflowPartType() {
		return WorkflowPartType.parse(this);
	}
	
	public default List<WorkflowPart> getChildren() {
		List<WorkflowPart> result = new ArrayList<>();
		try {
			BeanInfo info = Introspector.getBeanInfo(this.getClass());
			for (PropertyDescriptor desc : info.getPropertyDescriptors()) {
				Method getter = desc.getReadMethod();
				if (getter != null) {
					Class<?> returnType = getter.getReturnType();
					if (WorkflowPart.class.isAssignableFrom(returnType)) {
						WorkflowPart part = (WorkflowPart) getter.invoke(this);
						if (part != null) {
							result.add(part);
						}
					} else if (List.class.isAssignableFrom(returnType)) {
						List<?> list = (List<?>) getter.invoke(this);
						if (list != null) {
							for (Object item : list) {
								if (item != null &&  item instanceof WorkflowPart) {
									result.add((WorkflowPart) item);
								}
							}
						}
					}
				}
			}
		} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// Ignore
		}
		// Don't sort, leave items in natural order
		//result.sort(WorkflowPartType.getComparator()); 
		return result;
	}
}
