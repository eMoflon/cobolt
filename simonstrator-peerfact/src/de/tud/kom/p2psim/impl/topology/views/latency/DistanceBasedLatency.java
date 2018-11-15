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

package de.tud.kom.p2psim.impl.topology.views.latency;

import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.topology.views.LatencyDeterminator;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.impl.topology.views.AbstractTopologyView;
import de.tud.kom.p2psim.impl.topology.views.RangedLink;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * This is the equivalent to the distance based latency strategy in the modular
 * net layer.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 01.06.2012
 */
public class DistanceBasedLatency implements LatencyDeterminator {

	private long baseLatency = 31 * Time.MILLISECOND;

	private long latencyPerKilometer = 10 * Time.MICROSECOND;

	/**
	 * Default parameters
	 */
	public DistanceBasedLatency() {
		//
	}

	@Override
	public void onMacAdded(MacLayer host, TopologyView viewParent) {
		//
	}

	@Override
	public long getLatency(TopologyView view, MacAddress source,
			MacAddress destination, Link link) {
		double distance = -1;
		if (link != null && link instanceof RangedLink) {
			distance = ((RangedLink) link).getNodeDistance();
		}
		if (distance == -1) {
			AbstractTopologyView<?> abstractView = (AbstractTopologyView<?>) view;
			distance = abstractView.getPosition(source)
					.distanceTo(abstractView.getPosition(destination));
		}
		return baseLatency + (long) ((distance / 1000) * latencyPerKilometer);
	}

}
