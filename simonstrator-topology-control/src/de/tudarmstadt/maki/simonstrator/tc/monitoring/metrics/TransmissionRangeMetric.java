package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

/**
 * Stores the transmission range of the wireless nodes
 * 
 * @author Roland Kluge - Initial implementation
 */
public class TransmissionRangeMetric extends PreinitializedMetric {

	public TransmissionRangeMetric(final double transmissionRange) {
		super("UniformTransmissionRange", MetricUnit.LENGTH, new SimpleNumericMetricValue<Double>(transmissionRange));
	}

}
