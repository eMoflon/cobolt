package de.tudarmstadt.maki.simonstrator.tc.democles.integrated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gervarro.democles.common.runtime.OperationBuilder;
import org.gervarro.democles.common.runtime.SpecificationExtendedVariableRuntime;
import org.gervarro.democles.runtime.NativeOperation;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.VariableType;

import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation.GraphEdgesOperation;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation.GraphNodesOperation;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation.IncomingEdgeOperation;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation.OutgoingEdgeOperation;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation.SimonstratorConstraintOperation;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation.SimonstratorTypeCheckOperation;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.SimonstratorConstraintConstraintType;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.SimonstratorConstraintTypeModule;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;

/**
 * Creates for every constraint one corresponding operation
 * @author lneumann
 */
public class SimonstratorOperationBuilder
		implements OperationBuilder<NativeOperation, NativeOperation, SpecificationExtendedVariableRuntime> {

	// Caching maps
		private final Map<ConstraintType, NativeOperation> constraintTypeMapping =
				new HashMap<ConstraintType, NativeOperation>();
		private final Map<VariableType, NativeOperation> variableTypeMapping =
				new HashMap<VariableType, NativeOperation>();
	
	@Override
	public NativeOperation getConstraintOperation(ConstraintType constraint,
			List<? extends SpecificationExtendedVariableRuntime> parameters) {
		NativeOperation nativeOperation = null;
		if (constraint == SimonstratorConstraintTypeModule.OUTGOING_EDGE) {
			nativeOperation = constraintTypeMapping.get(constraint);
			if (nativeOperation == null) {
				nativeOperation = new OutgoingEdgeOperation();
				constraintTypeMapping.put(constraint, nativeOperation);
			}
		} else if (constraint == SimonstratorConstraintTypeModule.INCOMING_EDGE) {
			nativeOperation = constraintTypeMapping.get(constraint);
			if (nativeOperation == null) {
				nativeOperation = new IncomingEdgeOperation();
				constraintTypeMapping.put(constraint, nativeOperation);
			}
		} else if (constraint == SimonstratorConstraintTypeModule.GRAPH_NODES) {
			nativeOperation = constraintTypeMapping.get(constraint);
			if (nativeOperation == null) {
				nativeOperation = new GraphNodesOperation();
				constraintTypeMapping.put(constraint, nativeOperation);
			}
		} else  if(constraint == SimonstratorConstraintTypeModule.GRAPH_EDGES){
			nativeOperation = constraintTypeMapping.get(constraint);
			if (nativeOperation == null) {
				nativeOperation = new GraphEdgesOperation();
				constraintTypeMapping.put(constraint, nativeOperation);
			}
		} else if (constraint instanceof SimonstratorConstraintConstraintType) {
			nativeOperation = constraintTypeMapping.get(constraint);
			if(nativeOperation == null) {
				SimonstratorConstraintConstraintType simonstratorConstraintConstraintType = (SimonstratorConstraintConstraintType) constraint;
				GraphElementConstraint constraintConstraint = simonstratorConstraintConstraintType.getSimonstratorConstraint();
				nativeOperation = new SimonstratorConstraintOperation(constraintConstraint);
				constraintTypeMapping.put(constraint, nativeOperation);			}
		}
		return nativeOperation;
	}

	@Override
	public NativeOperation getVariableOperation(VariableType variable,
			SpecificationExtendedVariableRuntime runtimeVariable) {
		NativeOperation nativeOperation = null;
		nativeOperation = variableTypeMapping.get(variable);
		if(nativeOperation == null){
			nativeOperation = new SimonstratorTypeCheckOperation(variable);
			variableTypeMapping.put(variable, nativeOperation);
		}
		return nativeOperation;
	}

}
