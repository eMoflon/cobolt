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

package de.tud.kom.p2psim.impl.linklayer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.analyzer.LinklayerAnalyzer;
import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.LinkLayer;
import de.tud.kom.p2psim.api.linklayer.LinkLayerMessage;
import de.tud.kom.p2psim.api.linklayer.LinkMessageEvent;
import de.tud.kom.p2psim.api.linklayer.LinkMessageListener;
import de.tud.kom.p2psim.api.linklayer.MacStateListener;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.util.LiveMonitoring;
import de.tud.kom.p2psim.impl.util.LiveMonitoring.ProgressValue;
import de.tud.kom.p2psim.impl.util.toolkits.NumberFormatToolkit;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.AnalyzerNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;

/**
 * Basic building-block for a LinkLayer. It may contain multiple MAC-Layers as
 * foreseen by 802.xx. There should be no need to specify additional link
 * layers, as all the magic happens inside the MAC. This is just a simple
 * dispatcher to select the right MAC.
 * 
 * LinkLayers are created and configured using the {@link LinkLayerFactory}.
 * This LinkLayer is to be extended for more advanced functionality or in-depth
 * analyzing to keep the main code sleek and fast. Methods declared as final are
 * not to be altered to ensure correct functionality.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public class ModularLinkLayer implements LinkLayer, LinkMessageListener {

	/**
	 * All Mac-Layers of this Host
	 */
	private Map<PhyType, MacLayer> macLayers;

	/**
	 * All MessageListeners
	 */
	private List<LinkMessageListener> messageListeners;

	/**
	 * All MacStateLIsteners
	 */
	private List<MacStateListener> macStateListeners;
	
	/**
	 * All ConnectivityListeners
	 */
	// private List<ConnectivityListener> connectivityListeners;

	private static Map<NetID, MacAddress> addressResolution = new HashMap<NetID, MacAddress>();

	/**
	 * Host this LinkLayer belongs to.
	 */
	private SimHost host;

	/*
	 * Analyzing
	 */
	public static long _linkBroadcastSent, _linkUnicastSent, _linkUnicastRcvd,
			_linkBroadcastRcvd, _linkDropped = 0;

	public static boolean _analyzersInitialized = false;


	/**
	 * 
	 */
	public ModularLinkLayer(SimHost host) {
		this.host = host;
		macLayers = new LinkedHashMap<PhyType, MacLayer>();
		messageListeners = new LinkedList<LinkMessageListener>();
		macStateListeners = new LinkedList<MacStateListener>();

		if (!_analyzersInitialized) {
			LiveMonitoring
					.addProgressValueIfNotThere(new LinkMessagesSentProgress());
			LiveMonitoring
					.addProgressValueIfNotThere(new LinkMessagesReceivedProgress());
			LiveMonitoring
					.addProgressValueIfNotThere(new LinkMessagesDroppedProgress());
			_analyzersInitialized = true;
		}
	}

	@Override
	public void initialize() {
		/*
		 * Initialize MACs
		 */
		for (MacLayer mac : macLayers.values()) {
			mac.initialize();
			/*
			 * ARP
			 */
			NetInterface net = host.getNetworkComponent().getByName(
					mac.getPhyType().getNetInterfaceName());
			assert !addressResolution.containsKey(net.getLocalInetAddress());
			addressResolution.put(net.getLocalInetAddress(),
					mac.getMacAddress());
		}
	}

	@Override
	public void shutdown() {
		throw new AssertionError(
				"You are not supposed to shutdown this component.");
	}

	@Override
	public MacAddress addressResolution(NetID netID, PhyType phy) {
		/*
		 * This implementation does not really use ARP, but instead relies on a
		 * global table of all MacAddresses
		 */
		if (netID.equals(IPv4NetID.LOCAL_BROADCAST)) {
			return MacAddress.BROADCAST;
		}

		return addressResolution.get(netID);
	}


	@Override
	public final void addMacLayer(MacLayer macLayer) {
		// prevent duplicate MACs for one PHY
		if (macLayers.containsKey(macLayer.getPhyType())) {
			throw new ConfigurationException(
					"You configured two MACs with the same PhyType!");
		}
		// we register this LinkLayer as a Listener for the MAC
		macLayer.setMessageListener(this);
		macLayers.put(macLayer.getPhyType(), macLayer);
	}

	@Override
	public final SimHost getHost() {
		return host;
	}

	@Override
	public boolean hasPhy(PhyType phyType) {
		return macLayers.containsKey(phyType);
	}

	@Override
	public MacLayer getMac(PhyType phyType) {
		return macLayers.get(phyType);
	}

	@Override
	public void goOffline(PhyType phyType) {
		MacLayer activeMac = macLayers.get(phyType);
		if (activeMac == null) {
			throw new UnsupportedOperationException("There is no MAC for "
					+ phyType.toString()
					+ " on this Host. Your NetLayer messed something up!");
		}

		if (activeMac.isOnline()) {
			activeMac.goOffline();
			for (MacStateListener listener : macStateListeners) {
				listener.goneOffline(phyType);
			}
		} else {
			Monitor.log(ModularLinkLayer.class, Level.DEBUG,
					"Mac was already offline...");
		}
	}

	@Override
	public void goOnline(PhyType phyType) {
		MacLayer activeMac = macLayers.get(phyType);
		if (activeMac == null) {
			throw new UnsupportedOperationException("There is no MAC for "
					+ phyType.toString()
					+ " on this Host. Your NetLayer messed something up!");
		}
		if (!activeMac.isOnline()) {
			activeMac.goOnline();
			for (MacStateListener listener : macStateListeners) {
				listener.goneOnline(phyType);
			}
		} else {
			Monitor.log(ModularLinkLayer.class, Level.DEBUG,
					"Mac was already online...");
		}
	}

	@Override
	public boolean isOnline(PhyType phyType) {
		MacLayer activeMac = macLayers.get(phyType);
		if (activeMac == null) {
			Monitor.log(ModularLinkLayer.class, Level.WARN,
					"A MAC with the type " + phyType.toString()
							+ " is not configured.");
			return false;
		}
		return activeMac.isOnline();
	}

	@Override
	public void send(PhyType phyType, MacAddress destination, Message data) {
		MacLayer activeMac = macLayers.get(phyType);
		if (activeMac != null) {
			/*
			 * here we might add Analyzers/Inform the Monitor
			 */
			LinkLayerMessage lMsg = new DefaultLinkLayerMessage(data,
					activeMac.getMacAddress(), destination);

			if (destination.isBroadcast()) {
				_linkBroadcastSent++;
			} else {
				_linkUnicastSent++;
			}
			activeMac.send(destination, lMsg);
		} else {
			throw new UnsupportedOperationException("There is no MAC for "
					+ phyType.toString()
							+ " on this Host. Your NetLayer messed something up!");
		}
	}

	@Override
	public void messageArrived(LinkMessageEvent linkMsgEvent) {
		/*
		 * Notify Listeners. Every message that is received at a MAC and is not
		 * a pure MAC-Message is dispatched through this function! Here we might
		 * add Analyzers ;)
		 */
		if (linkMsgEvent.isBroadcast()) {
			_linkBroadcastRcvd++;
		} else {
			_linkUnicastRcvd++;
		}
		_linkMsgEvent(linkMsgEvent.getLinkLayerMessage(), getHost(),
				Reason.RECEIVE);
		for (LinkMessageListener listener : messageListeners) {
			listener.messageArrived(linkMsgEvent);
		}
	}

	@Override
	public final void addLinkMessageListener(LinkMessageListener listener) {
		messageListeners.add(listener);
	}

	@Override
	public final void removeLinkMessageListener(LinkMessageListener listener) {
		messageListeners.remove(listener);
	}

	@Override
	public BandwidthImpl getCurrentBandwidth(PhyType phy) {
		return getMac(phy).getCurrentBandwidth();
	}

	@Override
	public BandwidthImpl getMaxBandwidth(PhyType phy) {
		return getMac(phy).getMaxBandwidth();
	}

	// @Override
	// public final void addConnectivityListener(ConnectivityListener listener)
	// {
	// connectivityListeners.add(listener);
	// }
	//
	// @Override
	// public final void removeConnectivityListener(ConnectivityListener
	// listener) {
	// connectivityListeners.remove(listener);
	// }

	public class LinkMessagesSentProgress implements ProgressValue {

		@Override
		public String getName() {
			return "Link sent (U/B/T)";
		}

		@Override
		public String getValue() {
			return Long.toString(_linkUnicastSent) + " / "
					+ Long.toString(_linkBroadcastSent) + " / "
					+ Long.toString(_linkUnicastSent + _linkBroadcastSent);
		}

	}

	public class LinkMessagesReceivedProgress implements ProgressValue {

		@Override
		public String getName() {
			return "Link rcvd (U/B/T), drop (U)";
		}

		@Override
		public String getValue() {
			return Long.toString(_linkUnicastRcvd)
					+ " / "
					+ Long.toString(_linkBroadcastRcvd)
					+ " / "
					+ Long.toString(_linkUnicastRcvd + _linkBroadcastRcvd)
					+ ", drop: "
					+ NumberFormatToolkit.formatPercentage(_linkDropped
							/ (double) _linkUnicastSent, 3);
		}

	}

	public class LinkMessagesDroppedProgress implements ProgressValue {

		@Override
		public String getName() {
			return "Link dropped";
		}

		@Override
		public String getValue() {
			return Long.toString(_linkDropped);
		}

	}

	@Override
	public void addMacStateListener(MacStateListener listener) {
		macStateListeners.add(listener);
	}

	@Override
	public void removeMacStateListener(MacStateListener listener) {
		macStateListeners.remove(listener);
	}
	
	private static boolean _linkAnalyzerInitialized = false;
	private static LinklayerAnalyzer _linkAnalyzer = null;
	
	protected static void _linkMsgEvent(LinkLayerMessage msg, SimHost host,
			Reason reason) {
		if (!_linkAnalyzerInitialized) {
			try {
				_linkAnalyzer = Monitor.get(LinklayerAnalyzer.class);
			} catch (AnalyzerNotAvailableException e) {
				_linkAnalyzer = null;
			}
			_linkAnalyzerInitialized = true;
		}
		if (_linkAnalyzerInitialized && _linkAnalyzer != null) {
			_linkAnalyzer.linkMsgEvent(msg, host, reason);
		}
	}
}
