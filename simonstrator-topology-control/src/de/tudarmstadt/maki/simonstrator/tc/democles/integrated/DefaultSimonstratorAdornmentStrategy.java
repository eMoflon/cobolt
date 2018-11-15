package de.tudarmstadt.maki.simonstrator.tc.democles.integrated;

import java.util.Arrays;
import java.util.List;

import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.common.runtime.AdornmentAssignmentStrategy;
import org.gervarro.democles.runtime.NativeOperation;

import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation.GraphEdgesOperation;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation.GraphNodesOperation;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation.IncomingEdgeOperation;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation.OutgoingEdgeOperation;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation.SimonstratorConstraintOperation;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation.SimonstratorTypeCheckOperation;


/**
 * Defines which adornments are allowed for a specific operation
 * @author lneumann
 */
public enum DefaultSimonstratorAdornmentStrategy implements AdornmentAssignmentStrategy<List<Adornment>,NativeOperation> {
	INSTANCE;
	
	public final Adornment getAdornmentForNativeVariableOperation(final NativeOperation nativeOperation) {
		if (nativeOperation instanceof SimonstratorTypeCheckOperation) {
			return Adornment.create(Adornment.NOT_TYPECHECKED);
		} else {
			throw new RuntimeException("Unknown native operation");
		}
	}
	
	public final List<Adornment> getAdornmentForNativeConstraintOperation(final NativeOperation nativeOperation) {
		if (nativeOperation instanceof OutgoingEdgeOperation || nativeOperation instanceof IncomingEdgeOperation) {
			return Arrays.asList(
					Adornment.create(Adornment.BOUND, Adornment.BOUND, Adornment.BOUND),
					Adornment.create(Adornment.BOUND, Adornment.FREE, Adornment.BOUND),
					Adornment.create(Adornment.BOUND, Adornment.BOUND, Adornment.FREE));
		} else if (nativeOperation instanceof GraphNodesOperation || nativeOperation instanceof GraphEdgesOperation) {
			return Arrays.asList(
					Adornment.create(Adornment.BOUND, Adornment.BOUND),
					Adornment.create(Adornment.BOUND, Adornment.FREE));
		} else if (nativeOperation instanceof SimonstratorConstraintOperation){
			SimonstratorConstraintOperation simonnstratorConstraintOperation = SimonstratorConstraintOperation.class.cast(nativeOperation);
			int numberOfVariables = simonnstratorConstraintOperation.getNumberOfVariables();
			Adornment adornment = new Adornment(numberOfVariables);
			for(int i = 0; i<numberOfVariables; i++){
				adornment.set(i, Adornment.BOUND);
			}
			return Arrays.asList(adornment);
		}
		return null;
	}
}
