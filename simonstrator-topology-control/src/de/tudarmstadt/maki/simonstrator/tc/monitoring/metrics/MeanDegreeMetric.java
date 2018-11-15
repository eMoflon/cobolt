package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;

/**
 * Evaluates the mean degree of nodes in the given topology 
 * The average degree is equal to 2 * numEdges / numNodes (see https://en.wikipedia.org/wiki/Degree_(graph_theory))
 * 
 * @author Roland Kluge - Initial implementation
 */
public class MeanDegreeMetric extends PreinitializedMetric {

	public MeanDegreeMetric(final Graph inputTopology) {
		super("TopologyMeanDegree", new SimpleNumericMetricValue<Double>(calculateAverageDegree(inputTopology)));
	}

	private static double calculateAverageDegree(final Graph topology) {
		final int nodeCount = topology.getNodeCount();
      return nodeCount != 0 ? 2.0 * topology.getEdgeCount() / nodeCount : 0;
	}
}