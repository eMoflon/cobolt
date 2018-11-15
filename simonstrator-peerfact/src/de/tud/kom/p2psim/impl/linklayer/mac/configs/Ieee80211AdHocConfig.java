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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.network.BandwidthDetermination;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiPhy.Standard_802_11;
import de.tud.kom.p2psim.impl.linklayer.mac.wifi.AbstractRateManager.RateManagerTypes;
import de.tud.kom.p2psim.impl.linklayer.mac.wifi.Ieee80211AdHocMac;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * This class is a helper to create a WiFi MAC-Layer in AdHoc-Mode.
 * 
 * @author Christoph Muenker
 * @version 1.0, 28.02.2013
 */
public class Ieee80211AdHocConfig extends AbstractMacConfig {

	private Standard_802_11 standard = Standard_802_11.g;

	/**
	 * Specifies the transmission power of a Wi-Fi chip. The 18 Dbm correspond
	 * to 63mw in the 2,4ghz mode of the Wi-Fi chip, as specified for the
	 * Broadcom BCM 4329 Chip (http://www.datasheetdir.com/BCM4329+download),
	 * which is integrated in the Nexus 1.<br>
	 * In ns-3, the corresponding value for the variable <i>TxPowerStart</i> is
	 * set to 16.0206 dBm, as specified in yans-wifi-phy.h.
	 */
	private double txPowerDbm = 16.0206;

	/**
	 * In their tests for ad hoc scenarios, ns-3 sets this value to 2200 bytes.
	 * As mentioned in "802.11 Wireless Networks - Definitive Guide" from
	 * Matthew Gast, the standard specifies that the RTS threshold should be set
	 * to 2,347 bytes.
	 */
	private long rtsCtsThreshold = 2200;

	/**
	 * An arbitrary selected Wi-Fi channel.
	 */
	private int channel = 1;

	/**
	 * As specified in "802.11 Wireless Networks - Definitive Guide" from
	 * Matthew Gast, "a frame requiring RTS/CTS clearing is retransmitted four
	 * times before it is discarded and reported to higher-level protocols. The
	 * short retry limit, which applies to frames shorter than the RTS
	 * threshold, is set to 7 by default." In the current implementation, it is
	 * not differentiated between messages with and without RTS/CTS clearing,
	 * thus, every message is retransmitted 7 times.
	 */
	private int maxRetransmission = 7;

	/**
	 * As specified in ns-3, the maximum lifetime of a packet in the queue is
	 * set to 10 seconds.
	 */
	private long maxTimeInQueue = 10 * Time.SECOND;

	/**
	 * The value 1000 is read from <i>/sys/virtual/net/eth0/tx_queue_len</i> out
	 * of the Nexus One. ns-3 sets the maximum number of packets in the queue to
	 * 400 packets.
	 */
	private int maxQueueLength = 400;

	private RateManagerTypes rateManager = RateManagerTypes.ARF;

	private static Set<Integer> allowed5GhzChannels = new HashSet<Integer>();

	static {
		// allowed channel numbers for the 5 GHz spectrum
		allowed5GhzChannels.add(36);
		allowed5GhzChannels.add(40);
		allowed5GhzChannels.add(44);
		allowed5GhzChannels.add(48);
		allowed5GhzChannels.add(52);
		allowed5GhzChannels.add(56);
		allowed5GhzChannels.add(60);
		allowed5GhzChannels.add(64);
		allowed5GhzChannels.add(100);
		allowed5GhzChannels.add(104);
		allowed5GhzChannels.add(108);
		allowed5GhzChannels.add(112);
		allowed5GhzChannels.add(116);
		allowed5GhzChannels.add(120);
		allowed5GhzChannels.add(124);
		allowed5GhzChannels.add(128);
		allowed5GhzChannels.add(132);
		allowed5GhzChannels.add(136);
		allowed5GhzChannels.add(140);
	}

	@Override
	public MacLayer getConfiguredMacLayer(SimHost host, MacAddress address) {
		Ieee80211AdHocMac mac = new Ieee80211AdHocMac(host, address, getPhy(),
				standard, maxQueueLength, maxTimeInQueue, maxRetransmission,
				new BandwidthImpl(1), rateManager);
		mac.setChannel(channel);
		mac.setRtsCtsThreshold(rtsCtsThreshold);
		mac.setTxPowerDbm(txPowerDbm);
		return mac;
	}

	public void setStandard(String standard) {
		try {
			this.standard = Standard_802_11.valueOf(standard);
		} catch (Throwable e) {
			throw new ConfigurationException(
					"Wrong standard. Allowed Values are: "
							+ Arrays.toString(Standard_802_11.values()));
		}
	}

	public void setRateManager(String rateManager) {
		try {
			this.rateManager = RateManagerTypes.valueOf(rateManager);
		} catch (Throwable e) {
			throw new ConfigurationException(
					"Wrong rateManager. Allowed Values are: "
							+ Arrays.toString(RateManagerTypes.values()));
		}
	}

	public void setTxPowerDbm(double txPowerDbm) {
		this.txPowerDbm = txPowerDbm;
	}

	public void setRtsCtsThreshold(long rtsCtsThreshold) {
		this.rtsCtsThreshold = rtsCtsThreshold;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public void setMaxRetransmission(int maxRetransmission) {
		this.maxRetransmission = maxRetransmission;
	}

	public void setMaxTimeInQueue(long maxTimeInQueue) {
		this.maxTimeInQueue = maxTimeInQueue;
	}

	public void setMaxQueueLength(int maxQueueLength) {
		this.maxQueueLength = maxQueueLength;
	}

	@Override
	public void setDownBandwidth(long downBW) {
		throw new ConfigurationException("This is not usable for Wifi!");
	}

	@Override
	public void setUpBandwidth(long upBW) {
		throw new ConfigurationException("This is not usable for Wifi!");
	}

	@Override
	public void setBandwidthDetermination(
			BandwidthDetermination bandwidthDetermination) {
		throw new ConfigurationException("This is not usable for Wifi!");
	}

	@Override
	public String getHelp() {
		String help = "";
		help += "\tAllowed Channels for 802.11a are: "
				+ allowed5GhzChannels.toString() + "\n";
		help += "\tAllowed Channels for 802.11b and 802.11g are 1 to 13. \n";
		help += "\tMaxTimeInQueue should not be smaller then 0. If you set this to 0, it is disabled.\n";
		help += "\tMaxQueueLength should not be smaller then 0. If you set this to 0, it is disabled.\n";
		help += "\tMaxRetransmission should be bigger then 0.\n";
		help += "\tFor Standard are allowed values: "
				+ Arrays.toString(Standard_802_11.values()) + "\n";
		help += "\tFor RateManager are allowed values: "
				+ Arrays.toString(RateManagerTypes.values()) + "\n";
		return help;
	}

	@Override
	public boolean isWellConfigured() {
		switch (standard) {
		case a:
			if (!allowed5GhzChannels.contains(channel)) {
				return false;
			}
			break;
		case b:
		case g:
			if (channel < 0 || channel > 13) {
				return false;
			}
			break;
		}
		if (maxTimeInQueue < 0) {
			return false;
		}
		if (maxQueueLength < 0) {
			return false;
		}
		if (maxRetransmission <= 0) {
			return false;
		}
		if (standard == null) {
			return false;
		}
		return true;
	}
}
