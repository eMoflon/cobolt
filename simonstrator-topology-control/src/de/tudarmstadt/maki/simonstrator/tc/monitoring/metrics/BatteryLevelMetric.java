package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.Host;

/**
 * Records the absolute energy level per host
 * 
 * @author Roland Kluge - Initial implementation
 */
public class BatteryLevelMetric extends PreinitializedMetric {

	public BatteryLevelMetric(final Map<Host, MetricValue<?>> data) {
		super("BatteryEnergyLevel", MetricUnit.ENERGY, data);
	}
}
