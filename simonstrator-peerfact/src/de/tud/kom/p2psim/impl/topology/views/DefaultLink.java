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

import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tudarmstadt.maki.simonstrator.api.Rate;

/**
 * A very basic Link-Implementation. The idea behind a link is that the Link
 * itself decides, if it is connected or not (for example based on a distance).
 * This should be the base class for all links and its connectivity is set on
 * creation time. It can be altered by extending classes.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 29.02.2012
 */
public class DefaultLink implements Link {

	private MacAddress source;

	private MacAddress destination;

	protected double dropProb;
	
	protected long bandwidth;

	protected long latency;

	protected int mtu;

	private boolean isConnected = true;

	private boolean isSourceDifferentToDestination = true;

	private boolean isOutdated = true;

	/**
	 * Fully configurable Implementation of the Link
	 * 
	 * @param source
	 * @param destination
	 * @param isConnected
	 * @param dropProbability
	 * @param bandwidth
	 *            in bit/s, see {@link Rate}
	 * @param latency
	 * @param mtu
	 */
	public DefaultLink(MacAddress source, MacAddress destination,
			boolean isConnected, double dropProbability, long bandwidth,
			long latency, int mtu) {
		this.source = source;
		this.destination = destination;
		if (source.equals(destination)) {
			isSourceDifferentToDestination = false;
			this.isConnected = false;
		} else {
			this.isConnected = isConnected;
		}

		this.dropProb = dropProbability;
		this.bandwidth = bandwidth;
		this.latency = latency;
		this.mtu = mtu;
	}

	@Override
	public MacAddress getSource() {
		return source;
	}

	@Override
	public MacAddress getDestination() {
		return destination;
	}

	@Override
	public double getDropProbability() {
		return dropProb;
	}

	@Override
	public long getBandwidth(boolean isBroadcast) {
		return bandwidth;
	}

	@Override
	public long getLatency() {
		return latency;
	}

	@Override
	public int getMTU() {
		return mtu;
	}

	/**
	 * Use setConnected() in your more advanced links extending this class to
	 * alter connectivity based on distance etc. It is good practice to
	 * calculate the boolean only once after each update (ie. in one of the
	 * update...-Methods).
	 * 
	 * The DefaultLink is always connected.
	 * 
	 * @return
	 */
	@Override
	public final boolean isConnected() {
		return isConnected;
	}

	/**
	 * Use this to alter the connectivity of the link in an extending class. If
	 * source == destination this will have no effect, as such a link always has
	 * to return false when isConnected() is called.
	 * 
	 * @param isConnected
	 */
	protected final void setConnected(boolean isConnected) {
		this.isConnected = isSourceDifferentToDestination && isConnected;
	}
	
	@Override
	public String toString() {
		return "Link from " + source.toString() + " to "
				+ destination.toString();
	}

	public boolean isOutdated() {
		return isOutdated;
	}

	public void setOutdated(boolean outdated) {
		isOutdated = outdated;
	}

	/**
	 * Enables a topology view to update the link latency, e.g., based on the
	 * node distance
	 * 
	 * @param latency
	 */
	public void updateLatency(long latency) {
		this.latency = latency;
	}

	/**
	 * Update the probability of a packet drop on this link
	 * 
	 * @param dropProb
	 */
	public void updateDropProbability(double dropProb) {
		this.dropProb = dropProb;
	}

}
