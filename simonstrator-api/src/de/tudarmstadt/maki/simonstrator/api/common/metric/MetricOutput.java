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

/**
 * Output-channels for the Metric-Analyzer.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 08.08.2012
 */
public interface MetricOutput {

	/**
	 * Called as soon as the monitor starts with all available metrics (i.e.
	 * metrics that resulted from a filter as well as global metrics defined
	 * directly in the analyzer)
	 * 
	 * @param metrics
	 */
	public void initialize(List<Metric<?>> metrics);

	/**
	 * Called as soon as the monitor stops, may be used to finalize the output
	 * (close sockets etc.)
	 */
	public void onStop();
}
