package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.PreinitializedMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.SimpleNumericMetricValue;

public class CheckpointedEnergyConsumptionMetric extends PreinitializedMetric {

	public CheckpointedEnergyConsumptionMetric(Map<Host, Double> energyConsumptionSinceLastCheckpoint) {
		super("Checkpointed energy consumption", MetricUnit.ENERGY,
				createMetricValue(energyConsumptionSinceLastCheckpoint));
	}

	private static Map<Host, MetricValue<?>> createMetricValue(Map<Host, Double> energyConsumptionSinceLastCheckpoint) {
		final Map<Host, MetricValue<?>> result = new HashMap<>();
		for (final Entry<Host, Double> entry : energyConsumptionSinceLastCheckpoint.entrySet()) {
			result.put(entry.getKey(), new SimpleNumericMetricValue<Double>(entry.getValue()));
		}
		return result;
	}

}
