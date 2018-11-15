package de.tudarmstadt.maki.simonstrator.tc.component;

import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.PreinitializedMetric;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;

public class GoalNonfunctionalPropertyMetric extends PreinitializedMetric
{

   public GoalNonfunctionalPropertyMetric(final TopologyControlComponentConfig configuration)
   {
      super("Goal Nonfunctional Property", MetricUnit.NONE, new MetricValue<String>() {

         @Override
         public String getValue()
         {
            return configuration.goalNonfunctionalProperty.getName();
         }

         @Override
         public boolean isValid()
         {
            return true;
         }
      });
   }

}
