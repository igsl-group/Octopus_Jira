package com.igsl.configmigration.workflow.mapper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
		CONDITION(Condition.class, "negate", "type"),
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
		private WorkflowPartType(Class<?> cls) {
			this.cls = cls;
			this.identifyingAttributes = new ArrayList<>();
		}
		private WorkflowPartType(Class<?> cls, String... attributes) {
			this.cls = cls;
			this.identifyingAttributes = new ArrayList<>();
			this.identifyingAttributes.addAll(Arrays.asList(attributes));
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
			switch (partType) {
			case FUNCTION:
				JXPathContext ctx = JXPathContext.newContext(part);
				return "Function: " + WorkflowMapper.getFunctionDisplayName(part.getPartArgClassName(ctx));
			default:
				String name = partType.cls.getSimpleName().replaceAll("([A-Z])", " $1");
				return name.trim();
			}
		}
	}
	
	public default String getPartAttribute(JXPathContext ctx, String attr) {
		String result = (String) ctx.getValue(attr, String.class);
		if (result != null) {
			return result;
		}
		return "";
	}
	
	public default String getPartArgClassName(JXPathContext ctx) {
		return getPartAttribute(ctx, "arg[name='" + ARG_CLASS_NAME + "']/@value");
	}
	
	public default String getPartFilter(JXPathContext ctx) {
		String argFilter = "";
		String type = getPartAttribute(ctx, "type");
		String className = getPartArgClassName(ctx);
		if (TYPE_CLASS.equals(type) && className != null) {
			argFilter = "[arg[name='" + ARG_CLASS_NAME + "'][value='" + className + "']";
		}
		return argFilter;
	}
	
	public default String makeFilter() {
		String filter = "";
		WorkflowPartType partType = WorkflowPartType.parse(this);
		List<String> attrList = partType.getIdentifyingAttributes();
		JXPathContext ctx = JXPathContext.newContext(this);
		ctx.setLenient(true);
		for (String attr : attrList) {
			if (ATTRIBUTE_ARG_CLASS_NAME.equals(attr)) {
				filter += getPartFilter(ctx);
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
	 * Return relative XPath of this WorkflowPart, with selector to uniquely identify it.
	 * @return String
	 */
	public default String getPartXPath() {
		WorkflowPartType partType = getWorkflowPartType();
		return partType.getNodeName() + makeFilter();
	}

	/**
	 * Check if this WorkflowPart contains mappable attributes
	 * @return boolean
	 */
	public default boolean isPartMappable() {
		WorkflowPartType partType = getWorkflowPartType();
		switch (partType) {
		case ARG: // Fall
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
		return result;
	}
}
