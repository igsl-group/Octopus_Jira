package com.igsl.configmigration.workflow.mapper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
	
	/**
	 * Get a human readable name for this WorkflowPart, that can identify what it is.
	 * @return String
	 */
	public default String getWorkflowPartDisplayName() {
		Class<?> cls = this.getClass();
		if (Action.class.isAssignableFrom(cls)) {
			Action a = (Action) this;
			return "Action " + a.getName();
		} else if (Actions.class.isAssignableFrom(cls)) {
			return "Actions";
		} else if (Arg.class.isAssignableFrom(cls)) {
			Arg arg = (Arg) this;
			return "Argument " + arg.getName() + " = " + arg.getValue();
		} else if (CommonAction.class.isAssignableFrom(cls)) {
			CommonAction ca = (CommonAction) this;
			return "Common Action " + ca.getId();
		} else if (CommonActions.class.isAssignableFrom(cls)) {
			return "Common Actions";
		} else if (Condition.class.isAssignableFrom(cls)) {
			Condition con = (Condition) this;
			return "Condition " + con.getNegate() + " " + con.getType();
		} else if (Conditions.class.isAssignableFrom(cls)) {
			Conditions cons = (Conditions) this;
			return "Conditions " + cons.getType();
		} else if (ExternalPermissions.class.isAssignableFrom(cls)) {
			return "External Permissions";
		} else if (Function.class.isAssignableFrom(cls)) {
			Function func = (Function) this;
			return "Function " + func.getId();	// TODO Extract class name from Args?
		} else if (GlobalActions.class.isAssignableFrom(cls)) {
			return "Global Actions";
		} else if (GlobalConditions.class.isAssignableFrom(cls)) {
			return "Global Conditions";
		} else if (InitialActions.class.isAssignableFrom(cls)) {
			return "Initial Actions"; 
		} else if (Join.class.isAssignableFrom(cls)) {
			return "Join";
		} else if (Joins.class.isAssignableFrom(cls)) {
			return "Joins";
		} else if (Meta.class.isAssignableFrom(cls)) {
			Meta m = (Meta) this;
			return "Property " + m.getName() + " = " + m.getValue();
		} else if (ObjectFactory.class.isAssignableFrom(cls)) {
			return "Object Factory";
		} else if (Permission.class.isAssignableFrom(cls)) {
			Permission p = (Permission) this;
			return "Permission " + p.getId();
		} else if (PostFunctions.class.isAssignableFrom(cls)) {
			return "Post Functions"; 
		} else if (PreFunctions.class.isAssignableFrom(cls)) {	
			return "Pre Functions"; 
		} else if (Register.class.isAssignableFrom(cls)) {			
			Register r = (Register) this;
			return "Register " + r.getVariableName(); 
		} else if (Registers.class.isAssignableFrom(cls)) {	
			return "Registers";
		} else if (RestrictTo.class.isAssignableFrom(cls)) {
			return "Restriction";
		} else if (Result.class.isAssignableFrom(cls)) {			
			return "Result";
		} else if (Results.class.isAssignableFrom(cls)) {
			return "Results";
		} else if (Split.class.isAssignableFrom(cls)) {	
			return "Split";
		} else if (Splits.class.isAssignableFrom(cls)) {	
			return "Splits";
		} else if (Step.class.isAssignableFrom(cls)) {		
			Step s = (Step) this;
			return "Step " + s.getName();
		} else if (Steps.class.isAssignableFrom(cls)) {			
			return "Steps";
		} else if (TriggerFunction.class.isAssignableFrom(cls)) {		
			return "Trigger Function";
		} else if (TriggerFunctions.class.isAssignableFrom(cls)) {	
			return "Trigger Functions";
		} else if (UnconditionalResult.class.isAssignableFrom(cls)) {
			return "Unconditional Result";
		} else if (Validator.class.isAssignableFrom(cls)) {		
			Validator v = (Validator) this;
			return "Validator " + v.getId();	// TODO Extract name from class.name?
		} else if (Validators.class.isAssignableFrom(cls)) {			
			return "Validators";
		} else if (Workflow.class.isAssignableFrom(cls)) {			
			Workflow wf = (Workflow) this;
			return "Workflow";
		} 
		// Return class name as default
		return this.getClass().getSimpleName();
	}

	/**
	 * Get a list of children WorkflowPart that contains Meta or Arg to be mapped.
	 * 
	 * For Arg and Meta, ignore specific names.
	 * 
	 * @return
	 */
	public default List<WorkflowPart> getMappableChildren() {
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
