package de.tudarmstadt.maki.tc.cbctc.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import de.tudarmstadt.maki.tc.cbctc.model.constraints.ConstraintViolationReport;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.ConstraintsFactory;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.EdgeStateBasedConnectivityConstraint;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.GraphConstraint;

public class TopologyModelTestHelper {
	private TopologyModelTestHelper() {
		throw new UnsupportedOperationException();
	}

	public static void assertHasState(final Topology topology, final EdgeState state, final boolean checkSymmetry,
			final String... edgeIds) {
		for (final String edgeId : edgeIds) {
			final Edge edge = topology.getEdgeById(edgeId);
			TopologyModelTestHelper.assertHasState(edge, state);
			if (checkSymmetry) {
				TopologyModelTestHelper.assertHasState(edge.getReverseEdge(), state);
			}
		}
	}

	public static void assertHasState(final Edge edge, final EdgeState state) {
		Assert.assertNotNull(edge);
		final EdgeState actualState = edge.getState();
		Assert.assertSame("Expected edge '" + edge.getId() + "' to be '" + state + "' but was '" + actualState + "'",
				state, actualState);
	}

	/**
	 * Asserts that the given edge is in state {@link EdgeState#ACTIVE}
	 */
	public static void assertIsActive(final Edge edge) {
		TopologyModelTestHelper.assertHasState(edge, EdgeState.ACTIVE);
	}

	/**
	 * Asserts that the given edge is in state {@link EdgeState#INACTIVE}
	 */
	public static void assertIsInactive(final Edge edge) {
		TopologyModelTestHelper.assertHasState(edge, EdgeState.INACTIVE);
	}

	/**
	 * Asserts that the given edge is in state {@link EdgeState#UNCLASSIFIED}
	 */
	public static void assertIsUnclassified(final Edge edge) {
		TopologyModelTestHelper.assertHasState(edge, EdgeState.UNCLASSIFIED);
	}

	/**
	 * Asserts that all edges in the given topology that have one of the edgeIds
	 * are in state {@link EdgeState#ACTIVE}
	 */
	public static void assertIsActive(final Topology topology, final String... edgeIds) {
		assertHasState(topology, EdgeState.ACTIVE, false, edgeIds);
	}

	/**
	 * Asserts that all edges in the given topology that have one of the edgeIds
	 * are in state {@link EdgeState#INACTIVE}
	 */
	public static void assertIsInactive(final Topology topology, final String... edgeIds) {
		assertHasState(topology, EdgeState.INACTIVE, false, edgeIds);
	}

	/**
	 * Asserts that all edges in the given topology that have one of the edgeIds
	 * are in state {@link EdgeState#UNCLASSIFIED}
	 */
	public static void assertIsUnclassified(final Topology topology, final String... edgeIds) {
		assertHasState(topology, EdgeState.UNCLASSIFIED, false, edgeIds);
	}

	public static void assertIsActiveSymmetric(final Topology topology, final String... edgeIds) {
		assertHasState(topology, EdgeState.ACTIVE, true, edgeIds);
	}

	public static void assertIsInactiveSymmetric(final Topology topology, final String... edgeIds) {
		assertHasState(topology, EdgeState.INACTIVE, true, edgeIds);
	}

	public static void assertIsUnclassifiedSymmetric(final Topology topology, final String... edgeIds) {
		assertHasState(topology, EdgeState.UNCLASSIFIED, true, edgeIds);
	}

	public static void assertHasNoUnclassifiedLinks(final Topology topology) {
		for (final Edge edge : topology.getEdges()) {
			Assert.assertNotSame(EdgeState.UNCLASSIFIED, edge.getState());
		}
	}

	/**
	 * Asserts that the graph contains for each edge its reverse edge and that
	 * the state of forward and reverse edge are the same.
	 */
	public static void assertIsSymmetricWithRespectToStates(final Topology graph) {
		for (final Edge edge : graph.getEdges()) {
			Assert.assertNotNull("Link '" + edge.getId() + "' has no reverse edge", edge.getReverseEdge());
			Assert.assertSame(
					"Reverse edge of reverse edge of '" + edge.getId() + "' is '"
							+ edge.getReverseEdge().getReverseEdge() + "'.",
					edge, edge.getReverseEdge().getReverseEdge());

			Assert.assertEquals(edge.getState(), edge.getReverseEdge().getState());
		}
	}

	/**
	 * Asserts that all edges in the given graph are of state
	 * {@edges EdgeState#ACTIVE}, except for those edges that have an ID in the
	 * specified list of inactiveEdgeIds.
	 */
	public static void assertThatAllLinksAreActiveWithExceptions(final Topology topology,
			final boolean assumeSymmetricEdges, final String... inactiveEdgeIds) {
		final List<String> sortedInactiveEdgeIds = new ArrayList<>(Arrays.asList(inactiveEdgeIds));
		Collections.sort(sortedInactiveEdgeIds);
		for (final Edge edge : topology.getEdges()) {
			if (Collections.binarySearch(sortedInactiveEdgeIds, edge.getId()) >= 0) {
				assertIsInactive(edge);
			} else if (assumeSymmetricEdges
					&& Collections.binarySearch(sortedInactiveEdgeIds, edge.getReverseEdge().getId()) >= 0) {
				assertIsInactive(edge);
			} else {
				assertIsActive(edge);
			}
		}
	}

	/**
	 * Asserts that all edges in the given graph are of state
	 * {@edges EdgeState#UNCLASSIFIED}
	 */
	public static void assertThatAllLinksAreUnclassified(final Topology graph) {
		for (final Edge edge : graph.getEdges()) {
			assertIsUnclassified(edge);
		}
	}

	public static String format(final Topology graph) {
		final StringBuilder builder = new StringBuilder();
		final List<String> edgeIds = new ArrayList<>();
		final Set<String> processedIds = new HashSet<>();
		for (final Edge edge : graph.getEdges()) {
			edgeIds.add(edge.getId());
		}
		final Map<EdgeState, Integer> stateCounts = new HashMap<>();
		stateCounts.put(EdgeState.ACTIVE, 0);
		stateCounts.put(EdgeState.INACTIVE, 0);
		stateCounts.put(EdgeState.UNCLASSIFIED, 0);
		Collections.sort(edgeIds);
		for (final String id : edgeIds) {
			if (!processedIds.contains(id)) {
				final Edge edge = graph.getEdgeById(id);
				final Edge reverseEdge = edge.getReverseEdge();
				Assert.assertNotNull("Null reverse edge of edge " + edge, reverseEdge);
				Assert.assertNotNull(reverseEdge.getId());
				builder.append(String.format("%6s", edge.getId()) + " : " + edge.getState().toString().substring(0, 1)
						+ " || " + String.format("%6s", reverseEdge.getId()) + " : "
						+ reverseEdge.getState().toString().substring(0, 1) + "\n");
				processedIds.add(edge.getId());
				processedIds.add(reverseEdge.getId());
				stateCounts.put(edge.getState(), stateCounts.get(edge.getState()) + 1);
				stateCounts.put(reverseEdge.getState(), stateCounts.get(reverseEdge.getState()) + 1);
			}
		}
		builder.insert(0, String.format("#A : %d || #I : %d || #U : %d\n", stateCounts.get(EdgeState.ACTIVE),
				stateCounts.get(EdgeState.INACTIVE), stateCounts.get(EdgeState.UNCLASSIFIED)));
		return builder.toString();

	}

	public static void assertThatAllLinksAreActiveWithExceptionsSymmetric(final Topology graph, final String... edgeIds) {
		assertThatAllLinksAreActiveWithExceptions(graph, true, edgeIds);
	}

	/**
	 * Asserts that the active-edges-induced subgraph of the given graph is
	 * connected.
	 */
	public static void assertConnectivityViaActvieEdges(Topology graph) {
		assertConnectivity(graph, EdgeState.ACTIVE);
	}

	/**
	 * Asserts that the subgraph induced by active and unclassified edges of the
	 * given graph is connected.
	 */
	public static void assertConnectivityViaActiveOrUnclassifiedEdges(Topology graph) {
		assertConnectivity(graph, EdgeState.ACTIVE, EdgeState.UNCLASSIFIED);
	}

	/**
	 * Asserts that the subgraph induced by the edges of the given states of the
	 * given graph is connected.
	 */
	public static void assertConnectivity(Topology graph, EdgeState... states) {
		final EdgeStateBasedConnectivityConstraint constraint = ConstraintsFactory.eINSTANCE
				.createEdgeStateBasedConnectivityConstraint();
		constraint.getStates().addAll(Arrays.asList(states));
		assertGraphConstraints(graph, Arrays.asList(constraint));
	}

	/**
	 * Asserts that the given graph fulfills all given constraints
	 */
	public static void assertGraphConstraints(Topology graph, List<? extends GraphConstraint> constraints) {
		final ConstraintViolationReport report = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();
		for (final GraphConstraint constraint : constraints) {
			constraint.checkOnTopology(graph, report);
			Assert.assertEquals("Constraint checker report contains violations", 0, report.getViolations().size());
		}

	}

	public static void assertGraphConstraint(Topology graph, GraphConstraint constraint) {
		final ConstraintViolationReport report = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();
		constraint.checkOnTopology(graph, report);
		Assert.assertEquals("Constraint checker report contains violations", 0, report.getViolations().size());
	}
}
