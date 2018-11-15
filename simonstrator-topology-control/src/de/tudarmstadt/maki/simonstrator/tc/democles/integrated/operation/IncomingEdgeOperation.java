package de.tudarmstadt.maki.simonstrator.tc.democles.integrated.operation;

import java.util.Set;

import org.gervarro.democles.common.Adornment;
import org.gervarro.democles.runtime.InternalDataFrameProvider;
import org.gervarro.democles.runtime.IteratorBasedSingleFreeVariableHandler;
import org.gervarro.democles.runtime.NativeOperation;
import org.gervarro.democles.runtime.RemappedDataFrame;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;

public class IncomingEdgeOperation extends NativeOperation {

	@Override
	public InternalDataFrameProvider getDataFrame(RemappedDataFrame frame, Adornment adornment) {
		if (adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.BOUND && adornment.get(2) == Adornment.BOUND) {
			final Graph graph =  (Graph) frame.getValue(0);
			final INode node = (INode) frame.getValue(1);
			final IEdge edge = (IEdge) frame.getValue(2);
			
			if(graph.getIncomingEdges(node.getId()).contains(edge)) {
				return frame;
			}
		} else if (adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.BOUND && adornment.get(2) == Adornment.FREE) {
			final Graph graph =  (Graph) frame.getValue(0);
			final INode node = (INode) frame.getValue(1);
			
			final Set<IEdge> edges = graph.getIncomingEdges(node.getId());
			if(edges != null && !edges.isEmpty()){
				return new IteratorBasedSingleFreeVariableHandler<IEdge>(frame, edges.iterator(), 2);
			}
		} else if (adornment.get(0) == Adornment.BOUND && adornment.get(1) == Adornment.FREE && adornment.get(2) == Adornment.BOUND) {
			final Graph graph =  (Graph) frame.getValue(0);
			final IEdge edge = (IEdge) frame.getValue(2);
			
			final INode node = graph.getNode(edge.toId());
			if(node != null){
				frame = createDataFrame(frame);
				frame.setValue(1, node);
				return frame;
			}
		} else {
		   throw new IllegalArgumentException(adornment.toString());
		}
		return null;
	}

}
