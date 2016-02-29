package de.tudarmstadt.maki.modeling.jvlc.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.moflon.core.utilities.eMoflonEMFUtil;

import de.tudarmstadt.maki.modeling.graphmodel.DoubleAttribute;
import de.tudarmstadt.maki.modeling.graphmodel.Graph;
import de.tudarmstadt.maki.modeling.graphmodel.GraphmodelFactory;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.Topology;

public class TestGraphGenerator {
	public static void saveGraph(final Graph graph, final String filename) throws IOException {
		final ResourceSet set = eMoflonEMFUtil.createDefaultResourceSet();
		final Resource resource = set.createResource(eMoflonEMFUtil.createFileURI(filename, false));
		resource.getContents().add(graph);
		resource.save(null);
	}

	public static Topology produceTestTopology1() {
		final Topology graph = createEmptyGraph();
		final List<KTCNode> nodes = createNodes(graph, 5);
		createSymmetricLinksWithDistances(nodes,                             //
				1, 2, 15.0,                              // 
				1, 3, 20.0,                              //
				2, 3, 17.0,                             //
				1, 4, 25.0,                             //
				3, 4, 22.0,                              //
				1, 5, 30.0,                             //
				4, 5, 27.0//
		);

		return graph;
	}

	private static List<KTCLink> createSymmetricLinksWithDistances(final List<KTCNode> nodes, final Object... edgeSpecification) {
		if (edgeSpecification.length % 3 != 0) {
			throw new IllegalArgumentException("Invalid spec.");
		}

		final List<KTCLink> edges = new ArrayList<>();
		for (int e = 0; e < edgeSpecification.length; e += 3) {
			final KTCNode source = nodes.get((Integer) edgeSpecification[e] - 1);
			final KTCNode target = nodes.get((Integer) edgeSpecification[e + 1] - 1);
			final double distance = (Double) edgeSpecification[e + 2];
			final KTCLink edge = createKTCLink(source, target, distance);
			edges.add(edge);
			final KTCLink reverseEdge = createKTCLink(target, source, distance);
			edges.add(reverseEdge);
		}

		nodes.get(0).getGraph().getEdges().addAll(edges);
		return edges;
	}

	private static KTCLink createKTCLink(final KTCNode source, final KTCNode target, final double distance) {
		final KTCLink link = JvlcFactory.eINSTANCE.createKTCLink();
		source.getGraph().getEdges().add(link);
		link.setId(source.getId() + "->" + target.getId());
		link.setDistance(distance);
		link.setSource(source);
		link.setTarget(target);
		final DoubleAttribute distanceAttribute = GraphmodelFactory.eINSTANCE.createDoubleAttribute();
		link.getAttributes().add(distanceAttribute);
		distanceAttribute.setKey("distance");
		distanceAttribute.setValue(distance);
		return link;
	}

	private static List<KTCNode> createNodes(final Graph graph, final int nodeCount) {
		final List<KTCNode> nodes = new ArrayList<>();
		for (int n = 1; n <= nodeCount; ++n) {
			nodes.add(createKTCNode("n_" + n, graph));
		}
		return nodes;
	}

	private static KTCNode createKTCNode(final String id, final Graph graph) {
		final KTCNode node = JvlcFactory.eINSTANCE.createKTCNode();
		graph.getNodes().add(node);
		node.setId(id);
		return node;
	}

	private static Topology createEmptyGraph() {
		return JvlcFactory.eINSTANCE.createTopology();
	}
}
