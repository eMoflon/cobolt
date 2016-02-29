package de.tudarmstadt.maki.modeling.jvlc.graph;

import org.junit.Assert;
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

public class DistanceKTCTest {

	@Test
	public void testAlgorithmWithTestgraph1() throws Exception {

		final Topology testGraph1 = TestGraphGenerator.produceTestTopology1();
		final DistanceKTC distanceKTC = JvlcFactory.eINSTANCE.createDistanceKTC();
		distanceKTC.setK(1.1);

		distanceKTC.runOnTopology(testGraph1);

		new AssertConstraintViolationEnumerator().checkPredicate(testGraph1, distanceKTC);
		Assert.assertTrue(AssertConstraintViolationEnumerator.getInstance().checkConnectivityViaActiveLinks(testGraph1));
		Assert.assertTrue(AssertConstraintViolationEnumerator.getInstance().checkThatNoUnclassifiedLinksExist(testGraph1));

	}

	@Test
	public void testAlgorithmWithTestgraph1NodeBased() throws Exception {

		final Topology testGraph1 = TestGraphGenerator.produceTestTopology1();
		final DistanceKTC distanceKTC = JvlcFactory.eINSTANCE.createDistanceKTC();
		distanceKTC.setK(1.1);

		for (final Node node : testGraph1.getNodes())
		{
			distanceKTC.runOnNode((KTCNode) node);
		}

		new AssertConstraintViolationEnumerator().checkPredicate(testGraph1, distanceKTC);
		Assert.assertTrue(
				AssertConstraintViolationEnumerator.getInstance().checkConnectivityViaActiveLinks(testGraph1));
		Assert.assertTrue(
				AssertConstraintViolationEnumerator.getInstance().checkThatNoUnclassifiedLinksExist(testGraph1));

	}

	@Ignore //TODO@rkluge: Check me
	@Test
	public void testPredicateWithTestgraph1() throws Exception {
		final Graph testGraph1 = TestGraphGenerator.produceTestTopology1();
		final DistanceKTC distanceKTC = JvlcFactory.eINSTANCE.createDistanceKTC();

		final KTCLink e13 = getEdgeById(testGraph1, "n_1->n_3");
		final KTCLink e12 = getEdgeById(testGraph1, "n_1->n_2");
		final KTCLink e23 = getEdgeById(testGraph1, "n_2->n_3");

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
