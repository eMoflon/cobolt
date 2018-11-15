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


package de.tud.kom.p2psim.impl.network.bandwidthDetermination;

import java.util.Random;

import de.tud.kom.p2psim.api.network.BandwidthDetermination;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * This class implements a randomized determination of bandwidth, that follows
 * no certain distribution, but is used to omit a static assignment of
 * bandwidth-capacities.
 * 
 * @author Dominik Stingl
 * 
 */
public class RandomizedBandwidthDetermination implements
		BandwidthDetermination<Integer> {

	private long maxDownBandwidth = 10000;
	
	private long minDownBandwidth = 5000;
	
	private double upstreamBWQuota = 0.1;

	private Random rnd = Randoms
			.getRandom(RandomizedBandwidthDetermination.class);

	@Override
	public BandwidthImpl getBandwidthByObject(Integer object) {
		// not needed
		return null;
	}

	@Override
	public BandwidthImpl getRandomBandwidth() {
		double downBandwidth = minDownBandwidth + rnd.nextDouble()
				* (maxDownBandwidth - minDownBandwidth);
		double upBandwidth = downBandwidth * upstreamBWQuota;
		return new BandwidthImpl((long)Math.floor(downBandwidth), (long)Math.floor(upBandwidth));
	}

	public void setMaxDownBandwidth(long maxDownBandwidth) {
		this.maxDownBandwidth = maxDownBandwidth;
	}
	
	public void setMinDownBandwidth(long minDownBandwidth) {
		this.minDownBandwidth = minDownBandwidth;
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		bw.writeSimpleType("maxDownBandwidth", maxDownBandwidth);
		bw.writeSimpleType("minDownBandwidth", minDownBandwidth);
	}

}
