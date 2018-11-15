package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric;

import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.metric.AbstractMetric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplication_ImplBase;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.ICheckpointedLatencyMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.SimpleNumericMetricValue;

public class CheckpointedLatencyMetric extends AbstractMetric<MetricValue<?>>
		implements ICheckpointedLatencyMetric {

	public CheckpointedLatencyMetric() {
		super(NAME, MetricUnit.TIME);
	}

	@Override
	public void initialize(final List<Host> hosts) {
		hosts.forEach(host -> {
			final double averageLatencySinceCheckpoint = TopologyControlEvaluationApplication_ImplBase.find(host)
					.getAverageLatencySinceCheckpoint();
			final double safeValue = Double.isNaN(averageLatencySinceCheckpoint)
					? ICheckpointedLatencyMetric.NOT_AVAILABLE
					: averageLatencySinceCheckpoint;
			addHost(host, new SimpleNumericMetricValue<Double>(safeValue));
		});
	}

}
