package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

public class TimestampMetric extends PreinitializedMetric
{
   public static final String DECRIPTION = "Timestamp";

   public TimestampMetric(final long timestamp)
   {
      super(DECRIPTION, MetricUnit.TIME);
      this.setOverallMetric(new SimpleNumericMetricValue<Long>(timestamp));
   }

}
