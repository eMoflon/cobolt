package de.tudarmstadt.maki.simonstrator.tc.events.dto;

import de.tudarmstadt.maki.simonstrator.api.Time;

public class MetricValueEvent extends Event
{
   public static final String TYPE_ID = "new-metric-value";
   public final String metric;
   public final double value;

   public MetricValueEvent(final String metricName, final double metricValue, final long timestamp)
   {
      super(TYPE_ID);
      this.metric = metricName;
      this.value = metricValue;
      this.timestamp = timestamp;
   }

   public String getMetric()
   {
      return metric;
   }

   public Object getValue() {
      return this.value;
   }

   @Override
   public String toString()
   {
      return String.format("#%d:%s %s=%.2f (at %s)", this.getNumber(), this.getType(), this.getMetric(), this.getValue(), Time.getFormattedTime(this.getTimestamp()));
   }

}
