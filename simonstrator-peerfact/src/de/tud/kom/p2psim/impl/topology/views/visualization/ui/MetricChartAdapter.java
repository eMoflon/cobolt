/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tud.kom.p2psim.impl.topology.views.visualization.ui;

import org.jfree.data.xy.YIntervalSeries;

import de.tud.kom.p2psim.impl.analyzer.metric.MetricAnalyzer;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricUnit;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;

/**
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 10.08.2012
 */
public class MetricChartAdapter implements MetricPlotAdapter {

	private MetricValue<Number> std = null;

	private MetricValue<Number> perc_lower = null;

	private MetricValue<Number> perc_upper = null;

	private MetricValue<Number> avg = null;

	private MetricValue<Number> min = null;

	private MetricValue<Number> max = null;

	private YIntervalSeries series = null;

	private YIntervalSeries minSeries = null;

	private YIntervalSeries maxSeries = null;

	private final Metric metric;

	/**
	 * 
	 * @param metric
	 * @param view
	 * @param upperPercentile
	 *            upper percentile to be drawn, or -1 (you need to specify a
	 *            matching percentile filter!)
	 * @param lowerPercentile
	 *            lower percentile to be drawn, or -1 (you need to specify a
	 *            matching percentile filter!)
	 * @param maxItems
	 *            number of items to consider for min/max
	 */
	public MetricChartAdapter(Metric metric, PlottingView view,
			int upperPercentile, int lowerPercentile, int maxItems) {
		this.metric = metric;
		if (metric.isOverallMetric()) {
			MetricValue<?> mv = metric.getOverallMetric();
			avg = (MetricValue<Number>) mv;
		} else {
			// Try to find Avg, Std, Min, Max
			Metric avgM = MetricAnalyzer.getMetric("Avg_" + metric.getName());
			Metric stdM = MetricAnalyzer.getMetric("Std_" + metric.getName());
			if (avgM != null && avgM.isOverallMetric()) {
				avg = avgM.getOverallMetric();
			}
			if (avg != null && stdM != null && stdM.isOverallMetric()) {
				std = stdM.getOverallMetric();
			}

			if (lowerPercentile > 0 && upperPercentile > 0) {
				Metric percUpperM = MetricAnalyzer.getMetric("P"
						+ upperPercentile + "_" + metric.getName());
				Metric percLowerM = MetricAnalyzer.getMetric("P"
						+ lowerPercentile + "_" + metric.getName());
				if (percLowerM != null && percLowerM.isOverallMetric()
						&& percUpperM != null && percUpperM.isOverallMetric()) {
					perc_lower = percLowerM.getOverallMetric();
					perc_upper = percUpperM.getOverallMetric();
				}
			}

			Metric minM = MetricAnalyzer.getMetric("Min_" + metric.getName());
			Metric maxM = MetricAnalyzer.getMetric("Max_" + metric.getName());
			if (minM != null && minM.isOverallMetric()) {
				min = minM.getOverallMetric();
			}
			if (maxM != null && maxM.isOverallMetric()) {
				max = maxM.getOverallMetric();
			}
		}

		if (avg != null) {
			XYChart chart = view.createPlot(metric.getName() + " -- "
					+ metric.getDescription());

			series = chart.getDataset().getSeries(0);
			if (maxItems > 0) {
				series.setMaximumItemCount(maxItems);
			}
			if (min != null) {
				chart.getDataset().addSeries(new YIntervalSeries("Min"));
				minSeries = chart.getDataset().getSeries(
						chart.getDataset().getSeriesCount() - 1);
				if (maxItems > 0) {
					minSeries.setMaximumItemCount(maxItems);
				}
			}
			if (max != null) {
				chart.getDataset().addSeries(new YIntervalSeries("Max"));
				maxSeries = chart.getDataset().getSeries(
						chart.getDataset().getSeriesCount() - 1);
				if (maxItems > 0) {
					maxSeries.setMaximumItemCount(maxItems);
				}
			}
		}
	}

	public void refresh() {
		long seconds = Time.getCurrentTime() / Time.SECOND;
		if (series != null) {
			double avgVal = scaledValue(avg);
			if (!avg.isValid()) {
				avgVal = 0;
			}
			double varUp = 0;
			double varDown = 0;
			if (std != null) {
				varUp = varDown = scaledValue(std);
				if (!std.isValid()) {
					varUp = varDown = 0;
				}
			}
			if (perc_upper != null && perc_lower != null) {
				varUp = scaledValue(perc_upper) - avgVal;
				varDown = avgVal - scaledValue(perc_lower);
				if (!perc_lower.isValid()) {
					varUp = 0;
				}
				if (!perc_upper.isValid()) {
					varDown = 0;
				}
			}
			series.add(seconds, avgVal, Math.max(0, avgVal - varDown), avgVal
					+ varUp);
		}
		if (minSeries != null) {
			double minV = scaledValue(min);
			if (min.isValid()) {
				minSeries.add(seconds, minV, minV, minV);
			}
		}
		if (maxSeries != null) {
			double maxV = scaledValue(max);
			if (max.isValid()) {
				maxSeries.add(seconds, maxV, maxV, maxV);
			}
		}
	}

	protected double scaledValue(MetricValue<Number> mv) {
		if (metric.getUnit() == MetricUnit.TIME) {
			return mv.getValue().doubleValue() / Time.SECOND;
		}
		return mv.getValue().doubleValue();
	}

}
