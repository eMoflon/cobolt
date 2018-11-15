package de.tudarmstadt.maki.simonstrator.tc.filtering;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.BasicGraph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.util.UtilityClassNotInstantiableException;

public final class TopologyFilterUtils {
	private TopologyFilterUtils() {
	    throw new UtilityClassNotInstantiableException();
	}

	public static Graph filter(final Graph graph, final EdgeFilter filter) {
		final Graph newGraph = new BasicGraph();
		for (final INode node : graph.getNodes()) {
			final INode newNode = Graphs.createNode(node.getId());
			newNode.addPropertiesFrom(node);
			newGraph.addNode(newNode);
		}
		for (final IEdge edge : graph.getEdges()) {
			if (filter.ignoreEdge(edge)) {
				final IEdge newEdge = Graphs.createDirectedEdge(edge.fromId(), edge.toId());
				newEdge.addPropertiesFrom(edge);
				newGraph.addEdge(newEdge);
			}
		}
		return newGraph;
	}
}
