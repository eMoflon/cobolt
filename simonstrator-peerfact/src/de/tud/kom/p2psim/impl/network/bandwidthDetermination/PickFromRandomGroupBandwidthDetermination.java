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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.tud.kom.p2psim.api.network.BandwidthDetermination;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * Picks a random bandwidth (weighted) from a list of bandwidth groups assigned.
 * Overriding classes should add specific bandwidth groups.
 * 
 * @author Leo Nobach (additional changes: Dominik Stingl)
 * 
 */
public abstract class PickFromRandomGroupBandwidthDetermination implements
		BandwidthDetermination<Integer> {

	Random rand = Randoms
			.getRandom(PickFromRandomGroupBandwidthDetermination.class);

	int distributionMax = 0;

	List<Integer> distribution = new ArrayList<Integer>();

	List<BandwidthImpl> bandwidths = new ArrayList<BandwidthImpl>();

	public PickFromRandomGroupBandwidthDetermination() {
		addBandwidthGroups();
	}
	
	public abstract void addBandwidthGroups();

	/**
	 * Adds a new type of a network interface to the collection of different
	 * network interfaces. The specification for a new type consists of the
	 * upload- and download-bandwidth as well as of the relative usage-amount of
	 * the network interface
	 * 
	 * @param upBW
	 *            describes the maximum upload-bandwidth in bit per second
	 * @param downBW
	 *            describes the maximum download-bandwidth in bit per second
	 * @param part
	 *            describes the proportion of network interfaces of the given
	 *            type
	 */
	protected void addNewBandwidth(long upBW, long downBW, int part) {
		distributionMax += part;
		distribution.add(distributionMax);
		bandwidths.add(new BandwidthImpl(downBW, upBW));
	}

	public BandwidthImpl getRandomBandwidth() {
		int random = rand.nextInt(distributionMax);

		for (int i = 0; i < bandwidths.size(); i++) {
			if (distribution.get(i) > random) {
				return bandwidths.get(i);
			}
		}
		return bandwidths.get(bandwidths.size() - 1);

	}

	@Override
	public BandwidthImpl getBandwidthByObject(Integer object) {
		// nothing to do
		return null;
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		//No simple/complex types to write back
	}

}
