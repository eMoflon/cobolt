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

package de.tud.kom.p2psim.impl.linklayer.mac.configs;

import de.tud.kom.p2psim.api.linklayer.mac.MacConfiguration;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.BandwidthDetermination;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;

/**
 * Basic configuration methods such as PHY
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 03.03.2012
 * @param <M>
 */
public abstract class AbstractMacConfig implements MacConfiguration {

	private PhyType phy = null;

	/**
	 * If this is set, it determines the bandwidth of a link. Otherwise, the
	 * bandwidth is taken directly from the PHY-Type.
	 */
	private BandwidthImpl bandwidth = null;

	private BandwidthDetermination bandwidthDetermination = null;

	/**
	 * Configuration of the PHY for this MAC
	 * 
	 * @param phy
	 */
	public void setPhy(String phy) {
		phy = phy.toUpperCase();
		try {
			this.phy = PhyType.valueOf(phy);
		} catch (IllegalArgumentException e) {
			throw new ConfigurationException("The PHY " + phy
					+ " is unknown. Please select one of "
					+ PhyType.printTypes());
		}
		if (this.phy == null) {
			throw new ConfigurationException("The PHY " + phy
					+ " is unknown. Please select one of "
					+ PhyType.printTypes());
		}
	}

	/**
	 * If your mac does not predefine a PHY, use this. Do not forget to check
	 * for null!
	 * 
	 * @return
	 */
	public PhyType getPhy() {
		return phy;
	}

	/**
	 * Set a static upload bandwidth without changing the down-BW
	 * 
	 * @param upBw
	 */
	public void setUpBandwidth(long upBW) {
		if (bandwidth == null) {
			bandwidth = new BandwidthImpl(getPhy().getDefaultBW());
		}
		bandwidth.setUpBW(upBW);
	}

	/**
	 * Set a static download bandwidth without changing the down-BW
	 * 
	 * @param upBw
	 */
	public void setDownBandwidth(long downBW) {
		if (bandwidth == null) {
			bandwidth = new BandwidthImpl(getPhy().getDefaultBW());
		}
		bandwidth.setDownBW(downBW);
	}

	/**
	 * Allows configuration with a bandwidth-object
	 * 
	 * @param upDownBW
	 */
	public void setBandwidth(BandwidthImpl upDownBW) {
		bandwidth = upDownBW;
	}

	/**
	 * Allows the LinkLayer to use the {@link BandwidthDetermination}s provided
	 * in the "old" netLayers, for example OECD.
	 * 
	 * @param bandwidthDetermination
	 */
	public void setBandwidthDetermination(
			BandwidthDetermination bandwidthDetermination) {
		if (bandwidth != null) {
			throw new ConfigurationException(
					"You can specify the bandwidth using either a BW-Object or a BandwidthDetermination, but not both at once!");
		}
		this.bandwidthDetermination = bandwidthDetermination;
	}

	/**
	 * The configured bandwidth
	 * 
	 * @return
	 */
	public BandwidthImpl getBandwidth() {
		if (bandwidthDetermination != null) {
			return bandwidthDetermination.getRandomBandwidth();
		} else {
			return bandwidth;
		}
	}

}
