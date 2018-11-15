package de.tudarmstadt.maki.simonstrator.tc.democles.integrated;

import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.common.OperationRuntime;
import org.gervarro.democles.common.runtime.SearchPlanOperation;
import org.gervarro.democles.constraint.PatternInvocationConstraintType;
import org.gervarro.democles.plan.common.SearchPlanOperationBuilder;
import org.gervarro.democles.specification.Constraint;
import org.gervarro.democles.specification.ConstraintType;
import org.gervarro.democles.specification.Variable;

import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.SimonstratorConstraintConstraintType;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.SimonstratorConstraintTypeModule;

/**
 * Defines that all parameters have to be bound after the pattern matching process
 * @author lneumann
 */
public class SimonstratorSearchPlanOperationBuilder<O extends OperationRuntime> implements SearchPlanOperationBuilder<SearchPlanOperation<O>,O> {

	@Override
	public SearchPlanOperation<O> createSearchPlanOperation(O operation) {
		Object origin = operation.getOrigin();
		if (origin instanceof Constraint) {
			ConstraintType constraintType = ((Constraint) origin).getType();
			if (constraintType instanceof PatternInvocationConstraintType) {
				final Constraint constraint = (Constraint) origin;
				final int adornmentLength = constraint.getParameters().size();
				Adornment adornment = new Adornment(adornmentLength);
				for(int i = 0; i < adornment.size(); i++){
					adornment.set(i, Adornment.BOUND);
				}
				return new SearchPlanOperation<O>(operation, adornment);
			} else {
				Adornment adornment = operation.getPrecondition();
				if((constraintType == SimonstratorConstraintTypeModule.INCOMING_EDGE || constraintType == SimonstratorConstraintTypeModule.OUTGOING_EDGE) && (adornment.size() == 3 )){
					if(adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.BOUND && adornment.get(2) == Adornment.BOUND){
						return new SearchPlanOperation<O>(operation, Adornment.create(Adornment.BOUND, Adornment.BOUND, Adornment.BOUND));
					} else if (adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.BOUND && adornment.get(2) == Adornment.FREE) {
						return new SearchPlanOperation<O>(operation, Adornment.create(Adornment.BOUND, Adornment.BOUND, Adornment.BOUND));
					} else if (adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.FREE && adornment.get(2) == Adornment.BOUND) {
						return new SearchPlanOperation<O>(operation, Adornment.create(Adornment.BOUND, Adornment.BOUND, Adornment.BOUND));
					}
				} else if ((constraintType == SimonstratorConstraintTypeModule.GRAPH_NODES || constraintType == SimonstratorConstraintTypeModule.GRAPH_EDGES) && (adornment.size() == 2)) {
					if(adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.BOUND){
						return new SearchPlanOperation<O>(operation, Adornment.create(Adornment.BOUND, Adornment.BOUND));
					} else if (adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.FREE) {
						return new SearchPlanOperation<O>(operation, Adornment.create(Adornment.BOUND, Adornment.BOUND));
					}
				} else if(constraintType instanceof SimonstratorConstraintConstraintType){
					if(adornment.numberOfBound() == adornment.size()){
						Adornment allBound = new Adornment(adornment.size());
						for(int i = 0; i<adornment.size(); i++){
							adornment.set(i, Adornment.BOUND);
						}
						return new SearchPlanOperation<O>(operation, allBound);
					}
				}
			}
		} else if (origin instanceof Variable) {
			Adornment adornment = operation.getPrecondition();
			if (adornment.size() == 1 && adornment.get(0) == Adornment.NOT_TYPECHECKED) {
				return new SearchPlanOperation<>(operation, Adornment.create(Adornment.BOUND));
			}
		}
		return null;
	}

}
