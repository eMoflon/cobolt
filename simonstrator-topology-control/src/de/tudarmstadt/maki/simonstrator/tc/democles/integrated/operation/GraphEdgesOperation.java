package de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation;

import java.util.Set;

import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.runtime.InternalDataFrameProvider;
import org.gervarro.democles.runtime.IteratorBasedSingleFreeVariableHandler;
import org.gervarro.democles.runtime.NativeOperation;
import org.gervarro.democles.runtime.RemappedDataFrame;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;

public class GraphEdgesOperation extends NativeOperation {

	@Override
	public InternalDataFrameProvider getDataFrame(RemappedDataFrame frame, Adornment adornment) {
		if (adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.BOUND) {
			final Graph graph = (Graph) frame.getValue(0);
			final IEdge edge = (IEdge) frame.getValue(1);
			
			if(graph.getEdges().contains(edge)){
				return frame;
			}	
		} else if (adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.FREE){
			final Graph graph = (Graph) frame.getValue(0);
			@SuppressWarnings("unchecked")
         final Set<IEdge> edges = (Set<IEdge>) graph.getEdges();
			
			if(edges != null && !edges.isEmpty()){
				return new IteratorBasedSingleFreeVariableHandler<IEdge>(frame, edges.iterator(), 1);
			}
		} else {
			throw new IllegalArgumentException(adornment.toString());
		}
		return null;
	}

}
