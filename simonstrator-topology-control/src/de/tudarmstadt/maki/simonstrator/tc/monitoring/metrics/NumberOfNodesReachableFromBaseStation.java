package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

/**
 * Contains the number of nodes that can reached from the base station (if
 * applicable).
 * 
 * If there is no base station, the metric is set to {@link #NOT_AVAILABLE}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class NumberOfNodesReachableFromBaseStation extends PreinitializedMetric {

	public static final int NOT_AVAILABLE = -1;

	public NumberOfNodesReachableFromBaseStation(final int numberOfReachableNodes) {
		super("NumberOfNodesReachableFromBaseStation", 
				new SimpleNumericMetricValue<Integer>(numberOfReachableNodes));
	}

}
