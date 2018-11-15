package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric;

import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import de.tud.kom.p2psim.impl.topology.views.visualization.ui.PlottingView;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.XYChart;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MetricAggregationOperator;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MetricUtils;

public class MetricPlottingAdapter {

	public static final String NO_AXIS_LABEL = null;
	public static final double NO_RANGE_LIMIT = Double.NaN;
	private XYChart chart;
	private YIntervalSeries series;
	private final Metric<MetricValue<?>> metric;
	private final PlottingView view;
	private final double lowerBound;
	private final double upperBound;
	private String xAxisLabel;
	private String yAxisLabel;

	public MetricPlottingAdapter(final Metric<MetricValue<?>> metric, final PlottingView view, double lowerBound,
			double upperBound) {
		this(metric, view, lowerBound, upperBound, null, null);
	}

	public MetricPlottingAdapter(final Metric<MetricValue<?>> metric, final PlottingView view, double lowerBound,
			double upperBound, final String xAxisLabel, final String yAxisLabel) {
		this.metric = metric;
		this.view = view;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
	}

	public void initialize() {
		chart = view.createPlot(metric.getName() + " -- " + metric.getDescription());

		final YIntervalSeriesCollection dataset = chart.getDataset();
		this.series = dataset.getSeries(dataset.getSeriesCount() - 1);
		if (Double.isNaN(lowerBound) || Double.isNaN(upperBound)) {
			this.chart.getRangeAxis().setAutoRange(true);
		} else {
			this.chart.getRangeAxis().setAutoRange(false);
			this.chart.getRangeAxis().setRange(lowerBound, upperBound);
		}
		if (this.xAxisLabel != null) {
			this.chart.getDomainAxis().setLabel(this.xAxisLabel);
		}
		if (this.yAxisLabel != null) {
			this.chart.getRangeAxis().setLabel(this.yAxisLabel);
		}
		this.chart.setGridVisible(true);
	}

	public void newMetricValue(final long timestamp, final Metric<MetricValue<?>> metric,
			final Object markerForUnavailableDatum, final MetricAggregationOperator operator,
			final double scalingFactor) {
		final double aggregated = MetricUtils.aggregateAccordingToMetricTypeAndOperator(metric, markerForUnavailableDatum,
				operator);

		final double timeInMinutes = 1.0 * timestamp / Time.MINUTE;
		final double scaledValue = aggregated * scalingFactor;
		this.series.add(timeInMinutes, scaledValue, scaledValue, scaledValue);
	}

	public void newMetricValue(final long timestamp, final Metric<MetricValue<?>> metric,
			final Object markerForUnavailableDatum) {
		this.newMetricValue(timestamp, metric, markerForUnavailableDatum, MetricAggregationOperator.MEAN, 1.0);
	}

	public void newMetricValue(final long timestamp, Metric<MetricValue<?>> metric, int markerForUnavailableDatum,
			double scalingFactor) {
		this.newMetricValue(timestamp, metric, markerForUnavailableDatum, MetricAggregationOperator.MEAN,
				scalingFactor);
	}

}