package org.cobolt.model.utils;

import org.cobolt.model.Edge;
import org.cobolt.model.EdgeState;
import org.cobolt.model.Node;
import org.cobolt.model.Topology;
import org.moflon.core.utilities.UtilityClassNotInstantiableException;

/**
 * Utility class for manipulating {@link Topology}'s
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public class TopologyUtils {
	private TopologyUtils() {
		throw new UtilityClassNotInstantiableException();
	}

	/**
	 * Adds a node with the given ID and energy level to the given topology.
	 */
	public static Node addNode(final Topology topology, final String id, final double remainingEnergy) {
		final Node node = topology.addNode(id);
		node.setEnergyLevel(remainingEnergy);
		return node;
	}

	/**
	 * Adds a node with the given ID to the given topology.
	 *
	 * No information about the energy level is stored
	 */
	public static Node addNode(final Topology topology, final String id) {
		return addNode(topology, id, Double.NaN);
	}

	public static Edge addEdge(final Topology topology, final String id, final String sourceId, final String targetId,
			final double distance, final double requiredTransmissionPower, final EdgeState state) {
		final Node source = topology.getNodeById(sourceId);
		final Node target = topology.getNodeById(targetId);
		final Edge edge = topology.addDirectedEdge(id, source, target);
		edge.setWeight(distance);
		edge.setExpectedLifetime(edge.getSource().getEnergyLevel() / requiredTransmissionPower);
		edge.setState(state);
		return edge;
	}

	public static Edge addEdge(final Topology topology, final String id, final String source, final String target,
			final double distance, final double requiredTransmissionPower) {
		return addEdge(topology, id, source, target, distance, requiredTransmissionPower, EdgeState.UNCLASSIFIED);
	}

	public static Edge addEdge(final Topology topology, final String id, final String source, final String target,
			final double distance) {
		return addEdge(topology, id, source, target, distance, Double.NaN);
	}

	public static Edge addUndirectedEdge(final Topology topology, final String idFwd, final String idBwd,
			final Node node1, final Node node2, final double distance, final double requiredTransmissionPower) {
		final Edge fwdEdge = topology.addUndirectedEdge(idFwd, idBwd, node1, node2);
		fwdEdge.setExpectedLifetime(fwdEdge.getSource().getEnergyLevel() / requiredTransmissionPower);
		fwdEdge.setDistance(distance);

		final Edge bwdEdge = fwdEdge.getReverseEdge();
		bwdEdge.setExpectedLifetime(bwdEdge.getSource().getEnergyLevel() / requiredTransmissionPower);
		bwdEdge.setDistance(distance);

		return fwdEdge;
	}

	/**
	 * Adds an undirected edge to the given topology.
	 *
	 * No information about the required transmission power of the edge is provided
	 */
	public static Edge addUndirectedEdge(final Topology topology, final String idFwd, final String idBwd,
			final Node node1, final Node node2, final double distance) {
		return addUndirectedEdge(topology, idFwd, idBwd, node1, node2, distance, Double.NaN);
	}

	/**
	 * Returns true if the topology contains at least one unclassified edge
	 *
	 * @param topology
	 *                     the topology to check
	 * @return whether the topology contains at least one unclassified edge
	 */
	public static boolean containsUnclassifiedEdges(final Topology topology) {
		return topology.getEdges().stream().anyMatch(e -> e.getState() == EdgeState.UNCLASSIFIED);
	}
}
