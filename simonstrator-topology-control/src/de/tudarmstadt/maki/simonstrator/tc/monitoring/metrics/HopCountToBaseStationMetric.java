package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.Host;

/**
 * Captures the distance (in hops) to the base station (if applicable).
 * 
 * Otherwise, the distance is set to {@link #NOT_AVAILABLE}
 * 
 * @author Roland Kluge - Initial implementation
 */
public class HopCountToBaseStationMetric extends PreinitializedMetric {

	public static final int NOT_AVAILABLE = -1;

	public HopCountToBaseStationMetric(final Map<Host, MetricValue<?>> hopCount) {
		super("HopCountToBaseStation", hopCount);
	}

}
