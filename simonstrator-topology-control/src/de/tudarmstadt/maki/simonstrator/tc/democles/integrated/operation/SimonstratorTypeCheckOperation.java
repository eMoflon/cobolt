package de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation;

import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.runtime.InternalDataFrameProvider;
import org.gervarro.democles.runtime.NativeOperation;
import org.gervarro.democles.runtime.RemappedDataFrame;
import org.gervarro.democles.specification.VariableType;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.EdgeVariableType;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.GraphVariableType;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.NodeVariableType;

public class SimonstratorTypeCheckOperation extends NativeOperation {
	private VariableType variableType;
	
	public SimonstratorTypeCheckOperation(final VariableType variableType) {
		this.variableType = variableType;
	}

	@Override
	public InternalDataFrameProvider getDataFrame(RemappedDataFrame frame, Adornment adornment) {
		if (adornment.get(0) == Adornment.NOT_TYPECHECKED) {
			final Object object = frame.getValue(0);
			if(variableType == GraphVariableType.getInstance()){
				if(object instanceof Graph){
					return frame;
				}
			} else if (variableType == NodeVariableType.getInstance()){
				if(object instanceof INode){
					return frame;
				}
			} else if (variableType == EdgeVariableType.getInstance()){
				if(object instanceof IEdge){
					return frame;
				}
			} 
		} else {
		   throw new IllegalArgumentException(adornment.toString());
		}
		return null;
	}

}
