package de.tudarmstadt.maki.modeling.jvlc;

import java.util.Map;

import org.junit.Assert;

import de.tudarmstadt.maki.modeling.graphmodel.Edge;

public final class JvlcTestHelper {
	private JvlcTestHelper() {
		throw new UnsupportedOperationException();
	}

	public static final double EPS_0 = 0.0;
	public static final double EPS_6 = 1e-6;

	public static void assertIsUnclassified(final Edge link) {
		JvlcTestHelper.assertHasState(link, LinkState.UNCLASSIFIED);
	}

	public static void assertIsInactive(final Edge link) {
		JvlcTestHelper.assertHasState(link, LinkState.INACTIVE);
	}

	public static void assertIsActive(final Edge link) {
		JvlcTestHelper.assertHasState(link, LinkState.ACTIVE);
	}

	public static void assertIsActive(final Topology topology, final String... edgeIds) {
		for (final String edgeId : edgeIds) {
			JvlcTestHelper.assertHasState(topology.getEdgeById(edgeId), LinkState.ACTIVE);
		}
	}

	public static void assertIsInactive(final Topology topology, final String... edgeIds) {
		for (final String edgeId : edgeIds) {
			JvlcTestHelper.assertHasState(topology.getEdgeById(edgeId), LinkState.INACTIVE);
		}
	}

	public static void assertHasState(final Edge link, final LinkState state) {
		final LinkState actualState = ((KTCLink) link).getState();
		Assert.assertSame("Expected link '" + link.getId() + "' to be '" + state + "' but was '" + actualState + "'", state, actualState);
	}

	public static void assertIsSymmetric(final Topology graph) {
		for (final Edge edge : graph.getEdges()) {
			Assert.assertNotNull("Link '" + edge.getId() + "' has no reverse edge", edge.getReverseEdge());
			Assert.assertSame("Reverse edge of reverse edge of '" + edge.getId() + "' is '" + edge.getReverseEdge().getReverseEdge() + "'.", edge,
					edge.getReverseEdge().getReverseEdge());

			final KTCLink link = (KTCLink) edge;
			final KTCLink reverseLink = (KTCLink) link.getReverseEdge();
			Assert.assertEquals(link.getState(), reverseLink.getState());
			Assert.assertEquals(link.getRequiredTransmissionPower(), reverseLink.getRequiredTransmissionPower(), 0.0);
			Assert.assertEquals(link.getDistance(), reverseLink.getDistance(), 0.0);
		}
	}

	public static void assertHasState(final Map<KTCLink, LinkState> expectedStateMap) {
		for (final KTCLink link : expectedStateMap.keySet()) {
			assertHasState(link, expectedStateMap.get(link));
		}
	}

	public static String getPathToDistanceTestGraph(final int i) {
		return "instances/testgraph_D" + i + ".grapht";
	}

	public static String getPathToEnergyTestGraph(final int i) {
		return "instances/testgraph_E" + i + ".grapht";
	}
}
