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

package de.tud.kom.p2psim.impl.topology.views;

import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;


/**
 * A Link that sets isConnected() and some other parameters based on the current
 * distance of source and destination. It supports obstacles in that a link is
 * not connected as soon as it intersects an obstacle
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 07.03.2012
 */
public class RangedLink extends DefaultLink {

	private double maxDistance = 0;

	private double currentDistance = 0;

	/**
	 * 
	 * @param source
	 * @param destination
	 * @param isConnected
	 * @param dropProbability
	 * @param bandwidth
	 * @param latency
	 * @param mtu
	 * @param maxDistance
	 */
	public RangedLink(MacAddress source, MacAddress destination,
			boolean isConnected, double dropProbability, long bandwidth,
			long latency, int mtu, double maxDistance) {
		super(source, destination, isConnected, dropProbability, bandwidth,
				latency, mtu);
		this.maxDistance = maxDistance;
	}

	/**
	 * A topology may add some kind of dynamic behavior to the maximum distance
	 * of a link
	 * 
	 * @param maxDistance
	 */
	public void updateMaxDistance(double maxDistance) {
		this.maxDistance = maxDistance;
	}

	public double getMaxDistance() {
		return maxDistance;
	}

	public double getNodeDistance() {
		return currentDistance;
	}

	/**
	 * Update the distance between source and destination after movement
	 * occurred.
	 * 
	 * @param distance
	 */
	public void updateNodeDistance(double distance) {
		this.currentDistance = distance;
		setConnected(distance < maxDistance);
	}

	@Override
	public String toString() {
		return super.toString() + " distance: " + currentDistance;
	}

}
