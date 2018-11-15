package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric;

import java.util.HashMap;
import java.util.Map;

import de.tud.kom.p2psim.impl.topology.views.visualization.ui.PlottingView;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MetricUtils;

public class MetricPlottingsAdapterManager {

	private final Map<String, MetricPlottingAdapter> metricAdapters;

	public MetricPlottingsAdapterManager() {
		this.metricAdapters = new HashMap<>();
	}

	public boolean hasAdapter(final String metricName) {
		return this.metricAdapters.containsKey(metricName);
	}

	public void addAdapter(String name, MetricPlottingAdapter adapter) {
		this.metricAdapters.put(name, adapter);
	}

	public MetricPlottingAdapter getAdapter(final String metricName) {
		return this.metricAdapters.get(metricName);
	}

	public MetricPlottingAdapter registerMetricAdapterIfNecessary(final PlottingView plottingView,
			final Metric<MetricValue<?>> metric, final double lowerBound, final double upperBound,
			final String xAxisLabel, final String yAxisLabel) {
		MetricUtils.checkNotNull(metric);

		if (!hasAdapter(metric.getName())) {
			final MetricPlottingAdapter adapter = new MetricPlottingAdapter(metric, plottingView, lowerBound,
					upperBound, xAxisLabel, yAxisLabel);
			addAdapter(metric.getName(), adapter);
			adapter.initialize();
			return adapter;
		} else
			return this.getAdapter(metric.getName());
	}

	public MetricPlottingAdapter registerMetricAdapterIfNecessary(final PlottingView plottingView,
			final Metric<MetricValue<?>> metric, final String xAxisLabel, final String yAxisLabel) {
		return this.registerMetricAdapterIfNecessary(plottingView, metric, MetricPlottingAdapter.NO_RANGE_LIMIT,
				MetricPlottingAdapter.NO_RANGE_LIMIT, xAxisLabel, yAxisLabel);
	}

	public MetricPlottingAdapter registerMetricAdapterIfNecessary(final PlottingView plottingView,
			final Metric<MetricValue<?>> metric) {
		return this.registerMetricAdapterIfNecessary(plottingView, metric, MetricPlottingAdapter.NO_RANGE_LIMIT,
				MetricPlottingAdapter.NO_RANGE_LIMIT, MetricPlottingAdapter.NO_AXIS_LABEL,
				MetricPlottingAdapter.NO_AXIS_LABEL);
	}
}
