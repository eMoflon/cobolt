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

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.impl.linklayer.mac.EnqueuingMac;
import de.tud.kom.p2psim.impl.linklayer.mac.SimpleMacLayer;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * A very basic MAC - it does not perform scheduling but instead uses a local
 * queue if enabled to provide traffic control. This MAC is best used when
 * ETHERNET-like structures are simulated and there is no need for complex
 * MAC-scheduling.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 27.02.2012
 */
public class SimpleMac extends AbstractMacConfig {

	/**
	 * number of link-layer retransmits (message is at max transmitted 1 +
	 * maxRetransmissions times)
	 */
	private int maxRetransmissions = 0;

	private boolean enableTrafficQueue = false;

	/**
	 * 0 means unlimited.
	 */
	private int maxQueueLength = 0;

	private long maxTimeInQueue = 3 * Time.SECOND;


	/**
	 * String for warnings and errors
	 */
	private String errorInfo = "";

	private boolean error = false;

	public SimpleMac() {
		//
	}

	@Override
	public MacLayer getConfiguredMacLayer(SimHost host, MacAddress address) {
		/*
		 * The bandwidth, has to be a new object!
		 */
		BandwidthImpl maxBandwidth = getBandwidth().clone();

		if (enableTrafficQueue) {
			return new EnqueuingMac(host, address, getPhy(), maxQueueLength,
					maxTimeInQueue, maxRetransmissions, maxBandwidth);
		} else {
			return new SimpleMacLayer(host, address, getPhy(),
					maxRetransmissions, maxBandwidth);
		}
	}

	@Override
	public String getHelp() {
		if (error) {
			return errorInfo;
		} else {
			return "A very simple MAC-Layer."
					+ errorInfo;
		}
	}

	@Override
	public boolean isWellConfigured() {
		if (getPhy() == null) {
			error = true;
			errorInfo += "\n\tYou have to specify a PHY-Type for the MAC. Add phy=\"TYPE\" to your config, where TYPE is one of "
					+ PhyType.printTypes() + ".";
		}
		if (getPhy() == PhyType.ETHERNET && maxRetransmissions != 0) {
			errorInfo += "\n\tYou configured an ETHERNET-MAC to provide retransmissions (error control). ETHERNET does not support such a feature, consider disabling it in your configuration as well.";
		}
		if (enableTrafficQueue) {
			errorInfo += "\n\tYou enabled the TrafficQueue for outgoing traffic in the LinkLayer. The maximum queue size is: "
					+ (maxQueueLength == 0 ? "unlimited" : maxQueueLength)
					+ " and entries are deleted "
					+ (maxTimeInQueue == 0 ? "never" : "after "
							+ Time.getFormattedTime(maxTimeInQueue));
		} else {
			errorInfo += "\n\tThe TrafficQueue is disabled - messages are sent directly, even if the previous message is not yet fully transmitted.";
		}
		if (getBandwidth() == null) {
			setBandwidth(new BandwidthImpl(getPhy().getDefaultBW()));
			errorInfo += "\n\tYour Components will get the bandwidth by the PHY: "
					+ getPhy().getDefaultBW() + " byte/s";
		} else {
			errorInfo += "\n\tYour host will be created with the "
					+ getBandwidth().toString() + " as Bandwidth.";
		}
		return !error;
	}

	/*
	 * Configuration parameters
	 */
	/**
	 * Maximum transmissions for each Message on the LinkLayer
	 * 
	 * @param maxRetransmissions
	 */
	public void setMaxRetransmissions(int maxRetransmissions) {
		this.maxRetransmissions = maxRetransmissions;
		if (maxRetransmissions < 0) {
			error = true;
			errorInfo += "\n\tA value < 0 for maxRetransmissions is not allowed!";
		} else if (maxRetransmissions > 10) {
			errorInfo += "\n\tYou configured maxRetransmission to be > 10, which will slow down simulation. In most cases, you might want to keep the number of retransmits lower. You might need to reconsider the DropRate on the PHY you used.";
		} 
		errorInfo += "\n\tYou configured your MAC to retransmit a message "
				+ maxRetransmissions + " times.";
	}



	/**
	 * 
	 * 
	 * @param trafficQueueSize
	 */
	public void setTrafficQueueSize(int trafficQueueSize) {
		this.maxQueueLength = trafficQueueSize;
		this.enableTrafficQueue = true;
	}

	/**
	 * Entries in the TrafficQueue are deleted after this time
	 * 
	 * @param timeout
	 */
	public void setTrafficQueueTimeout(long timeout) {
		this.maxTimeInQueue = timeout;
		this.enableTrafficQueue = true;
	}

}
