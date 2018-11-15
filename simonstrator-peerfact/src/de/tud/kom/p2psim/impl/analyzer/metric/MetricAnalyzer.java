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

package de.tud.kom.p2psim.impl.analyzer.metric;

import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Oracle;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.MetricFilter;
import de.tudarmstadt.maki.simonstrator.api.common.metric.MetricOutput;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;

/**
 * The base analyzer that is to be configured in the Monitor-part if
 * {@link Metric}s are to be used. The Metrics themselves are configured as children via setMetric.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 07.08.2012
 */
public class MetricAnalyzer implements Analyzer {

	private static boolean initialized = false;

	private static final List<Metric<?>> metrics = new LinkedList<Metric<?>>();

	private static final List<MetricOutput> outputs = new LinkedList<MetricOutput>();

	private static final List<MetricFilter> filters = new LinkedList<MetricFilter>();

	public MetricAnalyzer() {
		if (initialized) {
			throw new ConfigurationException(
					"Only one Metric-Analyzer is allowed!");
		}
		initialized = true;
	}

	@Override
	public void start() {
		assert initialized;
		List<Host> hosts = Oracle.getAllHosts();
		if (hosts.isEmpty()) {
			throw new ConfigurationException(
					"The MetricAnalyzer needs the GlobalOracle!");
		}

		for (Metric<?> metric : metrics) {
			metric.initialize(hosts);
		}

		for (MetricFilter filter : filters) {
			filter.initialize(metrics, hosts);
			metrics.addAll(filter.getOutputMetrics());
		}

		for (MetricOutput output : outputs) {
			output.initialize(metrics);
		}
	}

	@Override
	public void stop(Writer output) {
		assert initialized;
		for (MetricFilter filter : filters) {
			filter.onStop();
		}
		for (MetricOutput outputChannel : outputs) {
			outputChannel.onStop();
		}
	}
	
	/**
	 * True, if the analyzer is configured and ready
	 * 
	 * @return
	 */
	public static boolean isInitialized() {
		return initialized;
	}

	/**
	 * Adds a metric
	 * 
	 * @param metric
	 */
	public static void setMetric(Metric metric) {
		if (!initialized) {
			throw new ConfigurationException("Analyzer not configured!");
		}
		metrics.add(metric);
	}

	/**
	 * Retrieve a metric, or null if the metric does not exist
	 * 
	 * @param name
	 * @return
	 */
	public static Metric getMetric(String name) {
		if (!initialized) {
			throw new ConfigurationException("Analyzer not configured!");
		}
		for (Metric metric : metrics) {
			if (metric.getName().equals(name)) {
				return metric;
			}
		}
		return null;
	}

	/**
	 * Adds an ouput channel
	 * 
	 * @param output
	 */
	public static void setOutput(MetricOutput output) {
		if (!initialized) {
			throw new ConfigurationException("Analyzer not configured!");
		}
		outputs.add(output);
	}

	/**
	 * Adds a filter
	 * 
	 * @param filter
	 */
	public static void setFilter(MetricFilter filter) {
		if (!initialized) {
			throw new ConfigurationException("Analyzer not configured!");
		}
		filters.add(filter);
	}

}
