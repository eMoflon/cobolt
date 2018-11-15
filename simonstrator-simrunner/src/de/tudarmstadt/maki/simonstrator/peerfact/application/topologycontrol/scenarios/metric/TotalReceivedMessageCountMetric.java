package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric;

import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.metric.AbstractMetric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplication_ImplBase;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.SimpleNumericMetricValue;

public class TotalReceivedMessageCountMetric extends AbstractMetric<MetricValue<?>> {

	public TotalReceivedMessageCountMetric() {
		super("TotalReceivedMessageCount", MetricUnit.NONE);
	}

	@Override
	public void initialize(final List<Host> hosts) {
		hosts.forEach(host -> addHost(host, new SimpleNumericMetricValue<Integer>(
				TopologyControlEvaluationApplication_ImplBase.find(host).getTotalMessageReceivedCount())));
	}

}
