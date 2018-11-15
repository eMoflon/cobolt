package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.Host;

/**
 * Records the relative battery level (percentage) per host
 * 
 * @author Roland Kluge - Initial implementation
 */
public class BatteryPercentageMetric extends PreinitializedMetric {

	public BatteryPercentageMetric(final Map<Host, MetricValue<?>> batteryPercentage) {
		super("BatteryPercentage", MetricUnit.PERCENT, batteryPercentage);
	}
}
