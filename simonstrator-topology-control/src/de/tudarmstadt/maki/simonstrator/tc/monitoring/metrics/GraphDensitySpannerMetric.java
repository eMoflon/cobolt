package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;

/**
 * Stores the density spanner, resulting from the comparison of the average node degrees of an input and output {@link Graph} 
 *  
 * @author Roland Kluge - Initial implementation
 */
public class GraphDensitySpannerMetric extends PreinitializedMetric
{
   public GraphDensitySpannerMetric(final Graph inputTopology, final Graph outputTopology)
   {
      super("GraphDensitySpannerMetric", MetricUnit.NONE, calculateMean(inputTopology, outputTopology));
   }

   private static MetricValue<Double> calculateMean(final Graph inputTopology, final Graph outputTopology)
   {
      final double inputTopologyDensity = 1.0 * inputTopology.getEdgeCount() / inputTopology.getNodeCount();
      final double outputTopologyDensity = 1.0 * outputTopology.getEdgeCount() / outputTopology.getNodeCount();
      final double densitySpanner = outputTopologyDensity / inputTopologyDensity;
      return new SimpleNumericMetricValue<Double>(densitySpanner);
   }

}
