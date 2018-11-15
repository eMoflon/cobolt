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
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * Derive the propagation latency for a constant propagation speed. The default
 * value for the propagation speed is the light speed.
 * 
 * @author Christoph Muenker
 * @version 1.0, 18.08.2012
 */
public class ConstantSpeedPropagationLatency implements LatencyDeterminator {

	/**
	 * The propagation speed in micro seconds. Default value is the light speed.
	 */
	private double speed = 299.792458;

	/**
	 * The constant speed for the propagation delay is the light speed.
	 */
	public ConstantSpeedPropagationLatency() {
		setSpeed(299792458l);
	}

	/**
	 * Take the given speed as propagation speed.
	 * 
	 * @param speed
	 *            Propagation speed in meter per second
	 */
	@XMLConfigurableConstructor({ "speed" })
	ConstantSpeedPropagationLatency(double speed) {
		setSpeed(speed);
	}

	@Override
	public void onMacAdded(MacLayer mac, TopologyView viewParent) {
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
		double time = distance / speed;
		return Math.round(time);
	}

	/**
	 * 
	 * @param speed
	 *            Propagation speed in meter per second
	 */
	public void setSpeed(double speed) {
		// Change from m/s to m/µs
		this.speed = speed / (1000 * 1000);
	}
}
