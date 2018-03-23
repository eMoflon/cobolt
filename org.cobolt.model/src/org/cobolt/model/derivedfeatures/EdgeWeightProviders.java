package org.cobolt.model.derivedfeatures;

import org.cobolt.model.Edge;
import org.cobolt.model.ModelPackage;
import org.cobolt.model.Topology;

/**
 * Collection of several pre-configured {@link EdgeWeightProvider}s and utility
 * methods
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public final class EdgeWeightProviders {
	public static final EdgeWeightProvider DISTANCE_PROVIDER = new EAttributeBasedEdgeWeightProvider(
			ModelPackage.eINSTANCE.getEdge_Distance());

	public static final EdgeWeightProvider SQUARED_DISTANCE_PROVIDER = new EAttributeBasedEdgeWeightProvider(
			ModelPackage.eINSTANCE.getEdge_Distance(), x -> x * x);

	public static final EdgeWeightProvider EXPECTED_REMAINING_LIFETIME_PROVIDER = new EAttributeBasedEdgeWeightProvider(
			ModelPackage.eINSTANCE.getEdge_ExpectedLifetime());

	public static void apply(final Edge edge, EdgeWeightProvider weightProvider) {
		edge.setWeight(weightProvider.getEdgeWeight(edge));
	}

	public static void apply(final Topology topology, final EdgeWeightProvider weightProvider) {
		topology.getEdges().forEach(edge -> apply(edge, weightProvider));
	}
}
