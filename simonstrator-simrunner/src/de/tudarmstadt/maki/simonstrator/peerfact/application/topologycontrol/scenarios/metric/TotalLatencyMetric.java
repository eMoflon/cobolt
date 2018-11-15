package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric;

import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.metric.AbstractMetric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplication_ImplBase;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.SimpleNumericMetricValue;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.NonfunctionalProperties;

public class TotalLatencyMetric extends AbstractMetric<MetricValue<?>> {

	public static final double NOT_AVAILABLE = -1;

	public TotalLatencyMetric() {
		super(NonfunctionalProperties.LATENCY.getName(), MetricUnit.TIME);
	}

	@Override
	public void initialize(final List<Host> hosts) {
		hosts.forEach(host -> {
			final double totalAverageLatency = TopologyControlEvaluationApplication_ImplBase.find(host)
					.getTotalAverageLatency();
			final double safeValue = Double.isNaN(totalAverageLatency) ? TotalLatencyMetric.NOT_AVAILABLE
					: totalAverageLatency;
			addHost(host, new SimpleNumericMetricValue<Double>(safeValue));
		});
	}

}
