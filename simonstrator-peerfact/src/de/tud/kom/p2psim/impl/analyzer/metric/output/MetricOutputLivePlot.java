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

package de.tud.kom.p2psim.impl.analyzer.metric.output;

import java.util.LinkedList;
import java.util.List;

import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.MetricCDFAdapter;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.MetricChartAdapter;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.MetricPlotAdapter;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.PlottingView;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 10.08.2012
 */
public class MetricOutputLivePlot extends AbstractOutput implements
		EventHandler {

	private List<MetricPlotAdapter> charts = new LinkedList<MetricPlotAdapter>();

	protected int upperPercentile = -1;

	protected int lowerPercentile = -1;

	private boolean enableCDF = false;

	private final long samplingInterval;

	private int maxItemCount = -1;

	@XMLConfigurableConstructor({ "samplingInterval" })
	public MetricOutputLivePlot(long samplingInterval) {
		this.samplingInterval = samplingInterval;
	}

	public void setMaxItemCount(int maxItemCount) {
		this.maxItemCount = maxItemCount;
	}

	@Override
	public void onInitialize(List<Metric> metrics) {
		PlottingView pView = null;
		PlottingView pViewCdf = null;
		for (Metric metric : metrics) {
			if (pView == null) {
				pView = VisualizationInjector.createPlottingView("Metrics");
			}
			MetricChartAdapter mca = new MetricChartAdapter(metric, pView,
					lowerPercentile, upperPercentile, maxItemCount);
			charts.add(mca);
			if (enableCDF && !metric.isOverallMetric()) {
				if (pViewCdf == null) {
					pViewCdf = VisualizationInjector
							.createPlottingView("CDFs of Metrics");
				}
				charts.add(new MetricCDFAdapter(metric, pViewCdf));
			}
		}
		scheduleNext();
	}

	@Override
	public void onStop() {
		// not interested
	}

	public void setUpperPercentile(int percentile) {
		this.upperPercentile = percentile;
	}

	public void setLowerPercentile(int percentile) {
		this.lowerPercentile = percentile;
	}

	private void scheduleNext() {
		Event.scheduleWithDelay(samplingInterval, this, null, 0);
		for (MetricPlotAdapter chart : charts) {
			chart.refresh();
		}
	}

	@Override
	public void eventOccurred(Object se, int type) {
		scheduleNext();
	}

	public void setEnableCDF(boolean enableCDF) {
		this.enableCDF = enableCDF;
	}

}
