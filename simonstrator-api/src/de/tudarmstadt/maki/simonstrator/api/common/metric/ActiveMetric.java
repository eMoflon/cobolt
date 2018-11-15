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

import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;

/**
 * A metric that is actively updated (for example based on a sampling interval).
 * Normally, only derived metrics (i.e. metrics that passed through a filter)
 * should be active metrics, but in some cases overlays might want to provide
 * metrics that are updated on specific operations.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 13.08.2012
 */
public interface ActiveMetric<M extends MetricValue<?>> extends Metric<M> {

	/**
	 * Add a listener that is informed whenever the metric is updated
	 * 
	 * @param listener
	 */
	public void addActiveMetricListener(ActiveMetricListener listener);

	/**
	 * Listener that is informed whenever the {@link ActiveMetric} is updated.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 13.08.2012
	 */
	public interface ActiveMetricListener {

		/**
		 * The given metric was updated
		 * 
		 * @param metric
		 */
		public void onMetricUpdate(ActiveMetric<?> metric);

	}

}
