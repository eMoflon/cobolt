package de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics;

import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;

public interface ICheckpointedLatencyMetric extends Metric<MetricValue<?>> {

	String NAME = "MeanLatencySinceCheckpoint";
	double NOT_AVAILABLE = -1.0;

}
