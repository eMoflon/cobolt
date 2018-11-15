package de.tudarmstadt.maki.simonstrator.tc.weighting;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

public class DistanceEdgeWeightProvider implements EdgeWeightProvider {

	private static final DistanceEdgeWeightProvider INSTANCE = new DistanceEdgeWeightProvider();

	@Override
	public double calculateWeight(final IEdge edge, final Graph graph) {
	    GraphElementProperties.validateThatPropertyIsPresent(edge, UnderlayTopologyProperties.DISTANCE);
		final Double property = edge.getProperty(UnderlayTopologyProperties.DISTANCE);
		return property;
	}

	public static DistanceEdgeWeightProvider getInstance() {
		return INSTANCE;
	}

}
