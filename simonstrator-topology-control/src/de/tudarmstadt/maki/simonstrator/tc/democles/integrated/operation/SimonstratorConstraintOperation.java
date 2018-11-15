package de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation;

import java.util.ArrayList;

import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.runtime.InternalDataFrameProvider;
import org.gervarro.democles.runtime.NativeOperation;
import org.gervarro.democles.runtime.RemappedDataFrame;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;

public class SimonstratorConstraintOperation extends NativeOperation {


	private GraphElementConstraint simonstratorConstraint;

	public SimonstratorConstraintOperation(GraphElementConstraint simonstratorConstraint) {
		this.simonstratorConstraint = simonstratorConstraint;
	}
	
	public int getNumberOfVariables(){
		return simonstratorConstraint.getVariables().size();
	}

	@Override
	public InternalDataFrameProvider getDataFrame(RemappedDataFrame frame, Adornment adornment) {
		if(adornment.numberOfBound() == simonstratorConstraint.getVariables().size()) {
			ArrayList<IElement> variables = new ArrayList<IElement>();
			
			for(int i=0; i < frame.size() ; i++){
				variables.add((IElement) frame.getValue(i));
			}
			if (simonstratorConstraint.isFulfilled(variables)) {
				return frame;
			}
		}
		else {
		   throw new IllegalArgumentException(adornment.toString());
		}
		return null;
	}
}
