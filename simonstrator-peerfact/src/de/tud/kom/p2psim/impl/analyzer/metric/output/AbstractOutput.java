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

import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.MetricOutput;

/**
 * Basic implementation of an output, allows black- and whitelisting of metrics.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 08.08.2012
 */
public abstract class AbstractOutput implements MetricOutput {

	private String[] blacklist = null;

	private String[] whitelist = null;

	private final List<Metric> metrics = new LinkedList<Metric>();

	protected boolean inFilter(Metric metric, String[] filter) {
		for (int i = 0; i < filter.length; i++) {
			String string = filter[i];
			if (metric.getName().equals(string)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final void initialize(List<Metric<?>> metrics) {
		for (Metric metric : metrics) {
			if (blacklist != null && inFilter(metric, blacklist)) {
				continue;
			}
			if (whitelist != null && !inFilter(metric, whitelist)) {
				continue;
			}
			this.metrics.add(metric);
		}
		onInitialize(this.metrics);
	}

	/**
	 * Called with the filtered list of metrics as soon as initialize is called.
	 * 
	 * @param metrics
	 */
	public abstract void onInitialize(List<Metric> metrics);

	/**
	 * List of (filtered) metrics for this output
	 * 
	 * @return
	 */
	public List<Metric> getMetrics() {
		return metrics;
	}

	/**
	 * List of Metrics
	 * 
	 * @param blacklist
	 */
	public void setBlacklist(String blacklist) {
		this.blacklist = blacklist.split(";");
	}

	public void setWhitelist(String whitelist) {
		this.whitelist = whitelist.split(";");
	}

}
