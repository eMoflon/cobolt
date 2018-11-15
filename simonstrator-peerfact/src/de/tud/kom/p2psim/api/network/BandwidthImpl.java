/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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


package de.tud.kom.p2psim.api.network;

import de.tudarmstadt.maki.simonstrator.api.component.network.Bandwidth;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

public class BandwidthImpl implements Bandwidth, Cloneable {

	private long downBW;

	private long upBW;

	@XMLConfigurableConstructor({ "downUpBW" })
	public BandwidthImpl(long downUpBW) {
		this(downUpBW, downUpBW);
	}

	@XMLConfigurableConstructor({ "downBW", "upBW" })
	public BandwidthImpl(long downBW, long upBW) {
		super();
		this.downBW = downBW;
		this.upBW = upBW;
	}

	/**
	 * The downstream bandwidth in bit/s
	 * 
	 * @return
	 */
	public long getDownBW() {
		return downBW;
	}

	/**
	 * The upstream bandwidth in bit/s
	 * 
	 * @return
	 */
	public long getUpBW() {
		return upBW;
	}
	
	/**
	 * Sets the downstream bandwidth in bit/s
	 * 
	 * @param downBW
	 */
	public void setDownBW(long downBW) {
		this.downBW = downBW;
	}

	/**
	 * Sets the upstream bandwidth in bit/s
	 * 
	 * @param upBW
	 */
	public void setUpBW(long upBW) {
		this.upBW = upBW;
	}

	public String toString() {
		return "(Down: " + downBW + " bit/s, Up: " + upBW + " bit/s)";
	}
	
	public BandwidthImpl clone() {
		return new BandwidthImpl(downBW, upBW);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = downBW;
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = upBW;
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BandwidthImpl other = (BandwidthImpl) obj;
		if (downBW != other.downBW)
			return false;
		if (upBW != other.upBW)
			return false;
		return true;
	}

}
