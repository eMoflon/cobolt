package de.tudarmstadt.maki.modeling.jvlc.algorithms;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.getPathToDistanceTestGraph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.graphmodel.Graph;
import de.tudarmstadt.maki.modeling.graphmodel.GraphModelTestHelper;
import de.tudarmstadt.maki.modeling.graphmodel.Node;
import de.tudarmstadt.maki.modeling.jvlc.DistanceKTC;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.Topology;
import de.tudarmstadt.maki.modeling.jvlc.constraints.AssertConstraintViolationEnumerator;
import de.tudarmstadt.maki.modeling.jvlc.io.GraphTFileReader;

public class DistanceKTCTest {

	private static final AssertConstraintViolationEnumerator constraintChecker = AssertConstraintViolationEnumerator
			.getInstance();

	private Topology graph;

	private DistanceKTC algorithm;

	@Before
	public void setUp() {
		this.graph = JvlcFactory.eINSTANCE.createTopology();
		this.algorithm = JvlcFactory.eINSTANCE.createDistanceKTC();
	}

	@Test
	public void testAlgorithmWithTestgraph1TopologyBased() throws Exception {
		GraphTFileReader.readTopology(graph, getPathToDistanceTestGraph(5));
		algorithm.setK(1.1);

		algorithm.runOnTopology(graph);

		constraintChecker.checkPredicate(graph, algorithm);
		GraphModelTestHelper.assertHasNoUnclassifiedLinks(graph);
		GraphModelTestHelper.assertConnectivityViaActiveOrUnclassifiedEdges(graph);
	}

	@Test
	public void testAlgorithmWithTestgraph1NodeBased() throws Exception {
		GraphTFileReader.readTopology(graph, getPathToDistanceTestGraph(5));
		algorithm.setK(1.1);

		for (final Node node : graph.getNodes()) {
			algorithm.runOnNode((KTCNode) node);
		}

		constraintChecker.checkPredicate(graph, algorithm);
		GraphModelTestHelper.assertHasNoUnclassifiedLinks(graph);
		GraphModelTestHelper.assertConnectivityViaActiveOrUnclassifiedEdges(graph);

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
