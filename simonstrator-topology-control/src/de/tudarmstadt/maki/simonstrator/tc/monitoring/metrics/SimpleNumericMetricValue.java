package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;

/**
 * A metric value that stores a single value.
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class SimpleNumericMetricValue<T extends Number> implements MetricValue<T> {

	private final T value;

	public SimpleNumericMetricValue(final T value) {
		this.value = value;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public boolean isValid() {
		return true;
	}
	
	@Override
	public String toString()
	{
	   return this.value.toString();
	}
}