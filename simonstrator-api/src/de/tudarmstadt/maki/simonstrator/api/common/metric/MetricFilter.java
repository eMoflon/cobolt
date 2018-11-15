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

package de.tudarmstadt.maki.simonstrator.api.common.metric;

import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Host;

/**
 * A metric filter takes one or more Metrics, performs some kind of
 * computation/aggregation on top of them and provides the results as a new,
 * derived metric.
 * 
 * Filters support black- and white-listing of Metrics to define their input
 * channels. They only support N:N and N:1 relationships, but not N:M or 1:M
 * relationships. This is enforced to provide an automatic naming scheme for
 * derived metrics to ease creation of chains. Derived metrics are named after
 * the filter and their input in the following manner:
 * 
 * N:N: input: metric, output: filter_metric, for example "nodes_online" becomes
 * "average_nodes_online".
 * 
 * N:1: inputs: metric1, metric2, output: filter_metric1_metric2.
 * 
 * Filters can become more advanced than this basic scheme by defining
 * additional config parameters, for example metrics that are used as normalizer
 * (number of hosts online, for example). The Filter can retrieve the metric
 * identified by its name via the MetricAnalyzer. Furthermore, those filters may
 * decide to overwrite the default naming scheme to prevent collisions on
 * multiple instances.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 08.08.2012
 */
public interface MetricFilter {

	/**
	 * Called on initialization with all metrics
	 * 
	 * @param metrics
	 * @param hosts
	 */
	public void initialize(List<Metric<?>> metrics, List<Host> hosts);

	/**
	 * Called by the analyzer, as soon as the monitoring interval is finished
	 * (most often, this will be right at the end of the simulation)
	 */
	public void onStop();

	/**
	 * Has to return the List of metrics that are used by this filter to compute
	 * the derived metric(s).
	 * 
	 * @return
	 */
	public List<Metric<?>> getInputMetrics();
	
	/**
	 * Has to return the List of derived metrics.
	 * 
	 * @return
	 */
	public List<Metric<?>> getOutputMetrics();
	
	/**
	 * Name of the filter
	 * 
	 * @return
	 */
	public String getName();

}
