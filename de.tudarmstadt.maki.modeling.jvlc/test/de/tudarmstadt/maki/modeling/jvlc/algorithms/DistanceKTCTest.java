package de.tudarmstadt.maki.modeling.jvlc.algorithms;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.getPathToDistanceTestGraph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.graphmodel.EdgeState;
import de.tudarmstadt.maki.modeling.graphmodel.Graph;
import de.tudarmstadt.maki.modeling.graphmodel.GraphModelTestHelper;
import de.tudarmstadt.maki.modeling.graphmodel.Node;
import de.tudarmstadt.maki.modeling.graphmodel.constraints.ConstraintsFactory;
import de.tudarmstadt.maki.modeling.graphmodel.constraints.EdgeStateBasedConnectivityConstraint;
import de.tudarmstadt.maki.modeling.graphmodel.constraints.NoUnclassifiedLinksConstraint;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.PlainKTC;
import de.tudarmstadt.maki.modeling.jvlc.Topology;
import de.tudarmstadt.maki.modeling.jvlc.TopologyControlOperationMode;
import de.tudarmstadt.maki.modeling.jvlc.io.GraphTFileReader;
import de.tudarmstadt.maki.modeling.jvlc.io.TopologyUtils;

public class DistanceKTCTest {

	private Topology graph;

	private PlainKTC algorithm;

	private NoUnclassifiedLinksConstraint noUnclasifiedLinksConstraint;

	private EdgeStateBasedConnectivityConstraint strongConnectivityConstraint;

	@Before
	public void setUp() {
		this.graph = JvlcFactory.eINSTANCE.createTopology();
		this.algorithm = JvlcFactory.eINSTANCE.createPlainKTC();
		this.algorithm.setOperationMode(TopologyControlOperationMode.INCREMENTAL);
		this.noUnclasifiedLinksConstraint = ConstraintsFactory.eINSTANCE.createNoUnclassifiedLinksConstraint();
		this.strongConnectivityConstraint = ConstraintsFactory.eINSTANCE.createEdgeStateBasedConnectivityConstraint();
		this.strongConnectivityConstraint.getStates().add(EdgeState.ACTIVE);
	}

	@Test
	public void testAlgorithmWithTestgraph1_RunOnTopology() throws Exception {
		GraphTFileReader.readTopology(graph, getPathToDistanceTestGraph(5));
		algorithm.setK(1.1);

		algorithm.runOnTopology(graph);

		GraphModelTestHelper.assertGraphConstraints(graph, algorithm.getAlgorithmSpecificConstraints());
		GraphModelTestHelper.assertGraphConstraint(graph, noUnclasifiedLinksConstraint);
		GraphModelTestHelper.assertGraphConstraint(graph, strongConnectivityConstraint);
	}

	@Test
	public void testAlgorithmWithTestgraph1_RunOnNode() throws Exception {
		GraphTFileReader.readTopology(graph, getPathToDistanceTestGraph(5));
		algorithm.setK(1.1);
		algorithm.initializeConstraints();

		for (final Node node : graph.getNodes()) {
			algorithm.runOnNode((KTCNode) node);
			System.out.println(TopologyUtils.formatEdgeStateReport(graph));
		}


		GraphModelTestHelper.assertGraphConstraints(graph, algorithm.getAlgorithmSpecificConstraints());
		GraphModelTestHelper.assertGraphConstraint(graph, noUnclasifiedLinksConstraint);
		GraphModelTestHelper.assertGraphConstraint(graph, strongConnectivityConstraint);

	}

	@Test
	public void testPredicateWithTestgraph1() throws Exception {
		GraphTFileReader.readTopology(graph, getPathToDistanceTestGraph(5));

		final KTCLink e13 = getKTCLinkById(graph, "e1-3");
		final KTCLink e12 = getKTCLinkById(graph, "e1-2");
		final KTCLink e23 = getKTCLinkById(graph, "e2-3");

		algorithm.setK(1.3);
		Assert.assertTrue(algorithm.checkPredicate(e13, e12, e23));
		Assert.assertTrue(algorithm.checkPredicate(e13, e23, e12));
		Assert.assertFalse(algorithm.checkPredicate(e23, e13, e12));

		algorithm.setK(1.5);
		Assert.assertFalse(algorithm.checkPredicate(e13, e12, e23));
		Assert.assertFalse(algorithm.checkPredicate(e13, e23, e12));
	}

	private KTCLink getKTCLinkById(final Graph testGraph, final String edgeId) {
		return (KTCLink) testGraph.getEdgeById(edgeId);
	}

}
