package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric;

import de.tud.kom.p2psim.impl.linklayer.ModularLinkLayer;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.PreinitializedMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.SimpleNumericMetricValue;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.NonfunctionalProperties;

/**
 * Average per-link drop rate (extracted from {@link ModularLinkLayer}
 *
 * @author Roland Kluge - Initial implementation
 */
public class TotalLinkDropRateMetric extends PreinitializedMetric {

	public static final double NOT_AVAILABLE = Double.NaN;

	public TotalLinkDropRateMetric() {
		super(NonfunctionalProperties.LINK_DROPRATE.getName(), MetricUnit.NONE, getMeanLinkDropRate());
	}

	private static MetricValue<Double> getMeanLinkDropRate() {
		final long dropCount = ModularLinkLayer._linkDropped;
		final long sentCount = ModularLinkLayer._linkUnicastSent;
		if (sentCount == 0) {
			return new SimpleNumericMetricValue<Double>(NOT_AVAILABLE);
		} else {
			final double dropFraction = 1.0 * dropCount / sentCount;
			return new SimpleNumericMetricValue<Double>(dropFraction);
		}
	}

}
