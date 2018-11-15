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

package de.tud.kom.p2psim.api.linklayer.mac;

import de.tud.kom.p2psim.api.linklayer.LinkLayer;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Rate;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;

/**
 * This is a physical medium (PHY-Layer). It is implemented as an enum, because
 * all parameter-related stuff has to be implemented in the {@link Link}. This
 * enum is used throughout the new {@link LinkLayer} and also on application
 * level if you want to explicitly specify which component to use.
 * 
 * It provides basic defaults for MTU and Drop rates in the medium, which might
 * be used inside the MacLayer or as part of a {@link Link}
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 25.02.2012
 */
public enum PhyType {

	/**
	 * Direct wireless connections between adjacent hosts or AP-based (decided
	 * by the MAC/TopologyView used). Based on
	 * "WiFi in Ad Hoc Mode: A Meaurement Study" by Anastasi et al. (2004),
	 * 
	 * Assuming the following defaults: 1% drop-rate of LinkLayer-Packets
	 * (LinkLayer might add retransmission behavior as in 802.11), 5MBit/s netto
	 * (802.11b) BW, 500us latency, 2,2kB MTU, Broadcast
	 */
	WIFI(NetInterfaceName.WIFI, 0.01, 5 * Rate.Mbit_s, 500 * Time.MICROSECOND,
			2334, true),
	/**
	 * A wired connection based on a TopologyView
	 * 
	 * Assuming the following defaults: 0.1% link-layer loss, 100 Mbit/s, 1ms
	 * Link-Latency, 1,5kB MTU
	 */
	ETHERNET(NetInterfaceName.ETHERNET, 0.001, 100 * Rate.Mbit_s,
			1 * Time.MILLISECOND, 1500, false),

	/**
	 * A centrally scheduled wireless connection connecting a host to at most
	 * one base-station. No direct communication between hosts.
	 *
	 * Assuming the following defaults: zero link-layer packet loss due to
	 * scheduling, 384kBit/s, 500us latency, 1,4 kByte MTU
	 */
	UMTS(NetInterfaceName.MOBILE, 0, 384 * Rate.kbit_s,
			500 * Simulator.MICROSECOND_UNIT, 1472, false),

	/**
	 * 
	 * Bluetooth, short-ranged wireless transmissions between adjacent hosts
	 *
	 * Assuming the following defaults: zero link-layer packet loss, 3Mbit/s BW,
	 * 500us latency, 1kByte MTU
	 */
	BLUETOOTH(NetInterfaceName.BLUETOOTH, 0.0, 3 * Rate.Mbit_s,
			500 * Simulator.MICROSECOND_UNIT, 1024, true);

	private double defaultDropProbability;

	/**
	 * Default bw of a link in this PHY (a link is unidirectional, therefore
	 * this is just one value) in bit/s
	 */
	private long defaultRawBW;

	private long defaultLatency;

	private int defaultMTU;

	private boolean isBroadcastMedium;

	/**
	 * FIXME we lateron want to get rid of the "PHY"-Definition and rely on
	 * NetInterfaceNames instead.
	 */
	private NetInterfaceName netInterfaceName;

	/**
	 * 
	 * @param defaultDropProbability
	 * @param defaultRawBW
	 *            bw in bit/s
	 * @param defaultLatency
	 * @param defaultMTU
	 */
	private PhyType(NetInterfaceName netName, double defaultDropProbability,
			long defaultRawBW, long defaultLatency, int defaultMTU,
			boolean isBroadcastMedium) {
		this.defaultDropProbability = defaultDropProbability;
		this.defaultRawBW = defaultRawBW;
		this.defaultLatency = defaultLatency;
		this.defaultMTU = defaultMTU;
		this.isBroadcastMedium = isBroadcastMedium;
		this.netInterfaceName = netName;
	}

	public double getDefaultDropProbability() {
		return defaultDropProbability;
	}

	public long getDefaultLatency() {
		return defaultLatency;
	}

	public int getDefaultMTU() {
		return defaultMTU;
	}

	/**
	 * The Default BW of a link (unidirectional) in bit/s
	 * 
	 * @return
	 */
	public long getDefaultBW() {
		return defaultRawBW;
	}

	/**
	 * has to return true if the medium is by its nature a broadcast medium (ie.
	 * sending a message will prohibit sending for all other hosts in range). If
	 * the medium supports Multiplexing such as UMTS, set this to false.
	 * 
	 * @return
	 */
	public boolean isBroadcastMedium() {
		return isBroadcastMedium;
	}

	/**
	 * Long-term replacement of PHY
	 * 
	 * @return
	 */
	public NetInterfaceName getNetInterfaceName() {
		return netInterfaceName;
	}

	public static String printTypes() {
		PhyType[] types = values();
		String out = "";
		for (int i = 0; i < types.length; i++) {
			if (i > 0) {
				out += ", ";
			}
			out += types[i].name();
		}
		return out;
	}

}
