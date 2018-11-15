package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;

public class NodeCountMetric extends PreinitializedMetric
{
   public static final String DECRIPTION = "TopologyNodeCount";

   public NodeCountMetric(final Graph inputTopology)
   {
      super(DECRIPTION);
      this.setOverallMetric(new SimpleNumericMetricValue<Integer>(inputTopology.getNodeCount()));
   }
}
