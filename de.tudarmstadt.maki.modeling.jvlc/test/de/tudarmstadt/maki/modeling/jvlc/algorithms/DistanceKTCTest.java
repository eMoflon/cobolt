package de.tudarmstadt.maki.modeling.jvlc.algorithms;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.getPathToDistanceTestGraph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.graphmodel.Graph;
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
	
	@Before
	public void setUp() {
		this.graph = JvlcFactory.eINSTANCE.createTopology();
	}
	
	@Test
	public void testAlgorithmWithTestgraph1() throws Exception {
		GraphTFileReader.readTopology(graph, getPathToDistanceTestGraph(5));
		final DistanceKTC distanceKTC = JvlcFactory.eINSTANCE.createDistanceKTC();
		distanceKTC.setK(1.1);

		distanceKTC.runOnTopology(graph);

		constraintChecker.checkPredicate(graph, distanceKTC);
		Assert.assertTrue(constraintChecker.checkConnectivityViaActiveLinks(graph));
		Assert.assertTrue(constraintChecker.checkThatNoUnclassifiedLinksExist(graph));

	}

	@Test
	public void testAlgorithmWithTestgraph1NodeBased() throws Exception {
		GraphTFileReader.readTopology(graph, getPathToDistanceTestGraph(5));
		final DistanceKTC distanceKTC = JvlcFactory.eINSTANCE.createDistanceKTC();
		distanceKTC.setK(1.1);

		for (final Node node : graph.getNodes()) {
			distanceKTC.runOnNode((KTCNode) node);
		}

		AssertConstraintViolationEnumerator.getInstance().checkPredicate(graph, distanceKTC);
		Assert.assertTrue(
				AssertConstraintViolationEnumerator.getInstance().checkConnectivityViaActiveLinks(graph));
		Assert.assertTrue(
				AssertConstraintViolationEnumerator.getInstance().checkThatNoUnclassifiedLinksExist(graph));

	}

	@Ignore // TODO@rkluge: Check me
	@Test
	public void testPredicateWithTestgraph1() throws Exception {
		GraphTFileReader.readTopology(graph, getPathToDistanceTestGraph(5));
		final DistanceKTC distanceKTC = JvlcFactory.eINSTANCE.createDistanceKTC();

		final KTCLink e13 = getEdgeById(graph, "n_1->n_3");
		final KTCLink e12 = getEdgeById(graph, "n_1->n_2");
		final KTCLink e23 = getEdgeById(graph, "n_2->n_3");

		distanceKTC.setK(1.3);
		Assert.assertTrue(distanceKTC.checkPredicate(e13, e12, e23));
		Assert.assertTrue(distanceKTC.checkPredicate(e13, e23, e12));
		Assert.assertFalse(distanceKTC.checkPredicate(e23, e13, e12));

		distanceKTC.setK(1.5);
		Assert.assertFalse(distanceKTC.checkPredicate(e13, e12, e23));
		Assert.assertFalse(distanceKTC.checkPredicate(e13, e23, e12));
	}

	private KTCLink getEdgeById(final Graph testGraph, final String edgeId) {
		return (KTCLink) testGraph.getEdgeById(edgeId);
	}

}
