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

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math.stat.Frequency;
import org.jfree.data.xy.YIntervalSeries;

import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricUnit;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;

public class MetricCDFAdapter implements MetricPlotAdapter {

	private final Metric metric;

	private YIntervalSeries series;

	private Frequency freq;

	private double maxX = Double.MIN_VALUE;

	private final XYChart chart;

	public MetricCDFAdapter(Metric metric, PlottingView view) {
		this.metric = metric;
		assert !metric.isOverallMetric();

		chart = view.createPlot(metric.getName() + " -- "
				+ metric.getDescription());

		series = chart.getDataset().getSeries(0);
		chart.getRangeAxis().setAutoRange(false);
		chart.getRangeAxis().setRange(0.0, 1.0);
		freq = new Frequency(new Comparator<Double>() {
			@Override
			public int compare(Double o1, Double o2) {
				return o1.compareTo(o2);
			}
		});
	}

	@Override
	public void refresh() {
		series.clear();
		freq.clear();
		List<MetricValue> mvs = metric.getAllPerHostMetrics();

		for (MetricValue mv : mvs) {
			Object val = mv.getValue();

			if (mv.isValid()) {
				if (val instanceof Number) {
					double vd = ((Number) val).doubleValue();

					if (metric.getUnit() == MetricUnit.TIME) {
						vd = vd / Time.SECOND;
					}

					if (vd > maxX) {
						maxX = vd;
					}

					freq.addValue(new Double(vd));
				}
			}
		}

		Map<Double, Double> cdf = new TreeMap<Double, Double>();
		Iterator<Comparable<?>> it = freq.valuesIterator();
		while (it.hasNext()) {
			Double dval = (Double) it.next();
			cdf.put(freq.getCumPct(dval), dval);
		}

		if (!cdf.isEmpty()) {
			series.add(0, 0, 0, 0);
			for (Entry<Double, Double> entry : cdf.entrySet()) {
				series.add(entry.getValue(), entry.getKey(), entry.getKey(),
						entry.getKey());
			}
			chart.getDomainAxis().setRange(0, maxX);
		}

	}
}
