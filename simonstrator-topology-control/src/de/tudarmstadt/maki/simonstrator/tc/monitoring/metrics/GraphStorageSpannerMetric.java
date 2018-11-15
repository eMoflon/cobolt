package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;

/**
 * Stores the storage spanner, resulting from the comparison of the sizes of an input and output {@link Graph} 
 *  
 * @author Roland Kluge - Initial implementation
 */
public class GraphStorageSpannerMetric extends PreinitializedMetric
{
   public GraphStorageSpannerMetric(final Graph inputTopology, final Graph outputTopology)
   {
      super("GraphStorageSpannerMetric", MetricUnit.NONE, calculateMean(inputTopology, outputTopology));
   }

   private static MetricValue<Double> calculateMean(final Graph inputTopology, final Graph outputTopology)
   {
      final double inputTopologySize = inputTopology.getNodeCount() + inputTopology.getEdgeCount();
      final double outputTopologySize = outputTopology.getNodeCount() + outputTopology.getEdgeCount();
      final double storageSpanner = outputTopologySize / inputTopologySize;
      return new SimpleNumericMetricValue<Double>(storageSpanner);
   }

}
