package de.tudarmstadt.maki.simonstrator.tc.democles.integrated;

import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.common.OperationRuntime;
import org.gervarro.democles.common.runtime.SearchPlanOperation;
import org.gervarro.democles.constraint.PatternInvocationConstraintType;
import org.gervarro.democles.plan.WeightedOperation;
import org.gervarro.democles.plan.common.SearchPlanOperationBuilder;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.impl.Constraint;
import org.gervarro.democles.specification.impl.Variable;

import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.SimonstratorConstraintConstraintType;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.SimonstratorConstraintTypeModule;

/**
 * Gives every operation a weight that the Pattern Matcher can find the most effective way trough the graph
 * @author lneumann
 */
public class SimonstratorWeightedOperationBuilder<SPO extends SearchPlanOperation<O>, O extends OperationRuntime>
implements SearchPlanOperationBuilder<WeightedOperation<SPO,Integer>,SPO> {

	@Override
	public WeightedOperation<SPO, Integer> createSearchPlanOperation(SPO operation) {
		Adornment adornment = operation.getPrecondition();
		Object object = operation.getOrigin();
		if(object instanceof Constraint){
			Constraint constraint = (Constraint) object;
			ConstraintType cType = constraint.getType();
			
			if(adornment.numberOfFrees() == 0) {
				if (cType instanceof PatternInvocationConstraintType && !((PatternInvocationConstraintType) cType).isPositive()) {
					return WeightedOperation.createOperation(operation, 5);
				}
				return WeightedOperation.createOperation(operation, -10);
			}
			
			if(cType == SimonstratorConstraintTypeModule.GRAPH_NODES){
				return WeightedOperation.createOperation(operation, 50);
			} else if (cType == SimonstratorConstraintTypeModule.GRAPH_EDGES) {
				return WeightedOperation.createOperation(operation, 80);
			} else if (cType == SimonstratorConstraintTypeModule.INCOMING_EDGE || cType == SimonstratorConstraintTypeModule.OUTGOING_EDGE) {
				if (adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.BOUND && adornment.get(1) == Adornment.FREE) {
					return WeightedOperation.createOperation(operation, 30);
				}
				return WeightedOperation.createOperation(operation, 3);
			} else if (cType instanceof SimonstratorConstraintConstraintType) {
				return WeightedOperation.createOperation(operation, 5);
			}
		} else if (object instanceof Variable) {
			return WeightedOperation.createOperation(operation, -5);
		}
		return null;
	}

}
