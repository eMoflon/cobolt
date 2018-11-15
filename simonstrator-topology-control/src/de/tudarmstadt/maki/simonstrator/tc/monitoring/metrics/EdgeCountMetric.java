package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;

public class EdgeCountMetric extends PreinitializedMetric
{
   public static final String DECRIPTION = "TopologyEdgeCount";

   public EdgeCountMetric(final Graph inputTopology)
   {
      super(DECRIPTION);
      this.setOverallMetric(new SimpleNumericMetricValue<Integer>(inputTopology.getEdgeCount()));
   }
}
