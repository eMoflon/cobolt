package de.tudarmstadt.maki.modeling.jvlc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import de.tudarmstadt.maki.modeling.graphmodel.Edge;
import de.tudarmstadt.maki.modeling.graphmodel.Graph;

public final class JvlcTestHelper {
	private JvlcTestHelper() {
		throw new UnsupportedOperationException();
	}

	public static final double EPS_0 = 0.0;
	public static final double EPS_6 = 1e-6;

	public static void assertHasState(final Topology topology, final LinkState state, final boolean checkSymmetry, final String... edgeIds) {
		for (final String edgeId : edgeIds) {
			final Edge link = topology.getEdgeById(edgeId);
			JvlcTestHelper.assertHasState(link, state);
			if (checkSymmetry) {
				JvlcTestHelper.assertHasState(link.getReverseEdge(), state);
			}
		}
	}

	public static void assertHasState(final Edge link, final LinkState state) {
		Assert.assertNotNull(link);
		final LinkState actualState = ((KTCLink) link).getState();
		Assert.assertSame("Expected link '" + link.getId() + "' to be '" + state + "' but was '" + actualState + "'", state, actualState);
	}

	public static void assertIsActive(final Edge link) {
		JvlcTestHelper.assertHasState(link, LinkState.ACTIVE);
	}

	public static void assertIsInactive(final Edge link) {
		JvlcTestHelper.assertHasState(link, LinkState.INACTIVE);
	}

	public static void assertIsUnclassified(final Edge link) {
		JvlcTestHelper.assertHasState(link, LinkState.UNCLASSIFIED);
	}

	public static void assertIsActive(final Topology topology, final String... edgeIds) {
		assertHasState(topology, LinkState.ACTIVE, false, edgeIds);
	}

	public static void assertIsInactive(final Topology topology, final String... edgeIds) {
		assertHasState(topology, LinkState.INACTIVE, false, edgeIds);
	}

	public static void assertIsUnclassified(final Topology topology, final String... edgeIds) {
		assertHasState(topology, LinkState.UNCLASSIFIED, false, edgeIds);
	}

	public static void assertIsActiveSymmetric(final Topology topology, final String... edgeIds) {
		assertHasState(topology, LinkState.ACTIVE, true, edgeIds);
	}

	public static void assertIsInactiveSymmetric(final Topology topology, final String... edgeIds) {
		assertHasState(topology, LinkState.INACTIVE, true, edgeIds);
	}

	public static void assertIsUnclassifiedSymmetric(final Topology topology, final String... edgeIds) {
		assertHasState(topology, LinkState.UNCLASSIFIED, true, edgeIds);
	}

	public static void assertHasNoUnclassifiedLinks(final Topology topology) {
		for (final Edge link : topology.getEdges()) {
			Assert.assertNotSame(LinkState.UNCLASSIFIED, ((KTCLink) link).getState());
		}
	}

	/**
	 * Asserts that the graph contains for each link its reverse link and that the attributes that should be symmetric (state, required transmission power, distance) are the same.
	 */
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

	public static void assertHasDistance(final Topology topology, final String id, final double distance) {
		Assert.assertEquals(distance, topology.getKTCLinkById(id).getDistance(), EPS_0);
	}

	public static void assertAllActiveWithExceptionsSymmetric(final Topology topology, final String... edgeIds) {
		assertAllActiveWithExceptions(topology, true, edgeIds);
	}

	public static void assertAllActiveWithExceptions(final Topology topology, final boolean assumeSymmetricEdges, final String... edgeIds) {
		final List<String> sortedEdgeIds = new ArrayList<>(Arrays.asList(edgeIds));
		Collections.sort(sortedEdgeIds);
		for (final Edge edge : topology.getEdges()) {
			if (Collections.binarySearch(sortedEdgeIds, edge.getId()) >= 0) {
				assertIsInactive(edge);
			} else if (assumeSymmetricEdges && Collections.binarySearch(sortedEdgeIds, edge.getReverseEdge().getId()) >= 0) {
				assertIsInactive(edge);
			} else {
				assertIsActive(edge);
			}
		}
	}

	public static String formatEdgeStates(final Graph graph) {
		final StringBuilder builder = new StringBuilder();
		final List<String> edgeIds = new ArrayList<>();
		final Set<String> processedIds = new HashSet<>();
		for (final Edge edge : graph.getEdges()) {
			edgeIds.add(edge.getId());
		}
		final Map<LinkState, Integer> stateCounts = new HashMap<>();
		stateCounts.put(LinkState.ACTIVE, 0);
		stateCounts.put(LinkState.INACTIVE, 0);
		stateCounts.put(LinkState.UNCLASSIFIED, 0);
		Collections.sort(edgeIds);
		for (final String id : edgeIds) {
			if (!processedIds.contains(id)) {
				final KTCLink link = (KTCLink) graph.getEdgeById(id);
				final KTCLink revLink = (KTCLink) link.getReverseEdge();
				Assert.assertNotNull("Null reverse link of link " + link, revLink);
				Assert.assertNotNull(revLink.getId());
				builder.append(String.format("%6s", link.getId()) + " : " + link.getState().toString().substring(0, 1) + " || "
						+ String.format("%6s", revLink.getId()) + " : " + revLink.getState().toString().substring(0, 1) + "\n");
				processedIds.add(link.getId());
				processedIds.add(revLink.getId());
				stateCounts.put(link.getState(), stateCounts.get(link.getState()) + 1);
				stateCounts.put(revLink.getState(), stateCounts.get(revLink.getState()) + 1);
			}
		}
		builder.insert(0, String.format("#A : %d || #I : %d || #U : %d\n", stateCounts.get(LinkState.ACTIVE), stateCounts.get(LinkState.INACTIVE),
				stateCounts.get(LinkState.UNCLASSIFIED)));
		return builder.toString();

	}

}