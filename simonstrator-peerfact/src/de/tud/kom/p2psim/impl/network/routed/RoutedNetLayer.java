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

package de.tud.kom.p2psim.impl.network.routed;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.analyzer.ConnectivityAnalyzer;
import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.analyzer.NetlayerAnalyzer;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.api.linklayer.LinkLayer;
import de.tud.kom.p2psim.api.linklayer.LinkMessageEvent;
import de.tud.kom.p2psim.api.linklayer.LinkMessageListener;
import de.tud.kom.p2psim.api.linklayer.MacStateListener;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.network.NetMessageEvent;
import de.tud.kom.p2psim.api.network.NetMessageListener;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tud.kom.p2psim.api.network.SimNetworkComponent;
import de.tud.kom.p2psim.api.network.routing.RoutingAlgorithm;
import de.tud.kom.p2psim.api.network.routing.RoutingListener;
import de.tud.kom.p2psim.api.network.routing.RoutingMessage;
import de.tud.kom.p2psim.impl.network.DefaultNetMessageEvent;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.util.LiveMonitoring;
import de.tud.kom.p2psim.impl.util.livemon.AvgAccumulatorDouble;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.AnalyzerNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.Bandwidth;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ConnectivityListener;

/**
 * This NetLayer supports a {@link LinkLayer} and provides more realistic
 * simulations for multi-hop message propagation and energy consumption. It is
 * therefore very different from the other NetLayers. It provides some IP-like
 * functionality (most important: a routing protocol and fragmenting).
 * 
 * TODO: due to the introduction of the {@link NetInterface} and
 * {@link NetworkComponent} API, this NetLayer has to be altered conceptionally
 * to support multiple NetIDs for different PHYs.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 23.02.2012
 */
public class RoutedNetLayer implements SimNetworkComponent, NetworkComponent,
		SimHostComponent {

	protected final SimHost host;

	/*
	 * Analyzing
	 */
	public static AvgAccumulatorDouble _avgHops = new AvgAccumulatorDouble(
			"Net avg. hops", 1000);

	public static AvgAccumulatorDouble _avgFragments = new AvgAccumulatorDouble(
			"Net avg. fragments", 1000);

	private final Map<NetInterfaceName, SimNetInterface> nets = new LinkedHashMap<NetInterfaceName, SimNetInterface>();

	public static boolean _analyzersInitialized = false;

	/**
	 * If this is set to "true", hosts will start offline.
	 */
	private final boolean startOffline;

	private boolean enableFragmenting = false;

	private long fragmentSize = 0;

	/**
	 * Create a new NetLayer for the given NetID with fragmenting disabled.
	 * 
	 * @param netID
	 */
	public RoutedNetLayer(SimHost host) {
		this(host, false, 0, false);
	}

	/**
	 * Create a new Netlayer with support for Message fragmenting.
	 * 
	 * @param netID
	 * @param enableFragmenting
	 * @param fragmentSize
	 */
	public RoutedNetLayer(SimHost host, boolean enableFragmenting,
			long fragmentSize, boolean startOffline) {
		this.host = host;
		this.startOffline = startOffline;
		this.enableFragmenting = enableFragmenting;
		this.fragmentSize = fragmentSize;
		if (!_analyzersInitialized) {
			LiveMonitoring.addProgressValueIfNotThere(_avgHops);
			if (enableFragmenting)
				LiveMonitoring.addProgressValueIfNotThere(_avgFragments);
			_analyzersInitialized = true;
		}
	}

	@Override
	public void initialize() {
		/*
		 * Create the NetinterfaceImpls for all specified PHYs in the LinkLayer
		 * 
		 * TODO maybe get rid of PhyType and use NetInterfaceName throughout the
		 * Simulator
		 */
		for (NetInterface netI : nets.values()) {
			((NetInterfaceImpl) netI).initialize();
		}
	}

	@Override
	public void shutdown() {
		throw new AssertionError(
				"You are not supposed to shutdown this component.");
	}
	
	@Override
	public boolean isActive() {
		/*
		 * Active, as long as at least one interface is active.
		 */
		for (SimNetInterface netI : nets.values()) {
			if (netI.isActive()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void startComponent() {
		for (SimNetInterface netI : nets.values()) {
			if (!netI.isActive()) {
				netI.startComponent();
			}
		}
	}
	
	@Override
	public void stopComponent() {
		for (SimNetInterface netI : nets.values()) {
			if (netI.isActive()) {
				netI.stopComponent();
			}
		}
	}

	/**
	 * Configures the NetLayer with the provided routing Algorithm for a PHY
	 * 
	 * @param routing
	 */
	public void addRoutingAlgorithm(RoutingAlgorithm routing, NetID netId) {
		if (nets.containsKey(routing.getNetInterfaceName())) {
			throw new AssertionError(
					"Another routing algorithm for "
							+ routing.getNetInterfaceName()
							+ " has already been configured. Ensure, that routing is only set in the Hostbuilder-Section of your configuration!!");
		}
		nets.put(routing.getNetInterfaceName(),
				new NetInterfaceImpl(routing.getNetInterfaceName(), netId,
						routing.getPhyType(), routing, enableFragmenting,
						fragmentSize));
	}

	@Override
	public SimHost getHost() {
		return host;
	}

	/**
	 * Adapter to the MacLayer (now, a Netlayer corresponds to a single
	 * NetInterface - i.e., a host may have multiple NetLayers that are managed
	 * by a single NetComponent.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, May 16, 2013
	 */
	private class NetInterfaceImpl implements SimNetInterface,
			MacStateListener, LinkMessageListener, RoutingListener {

		private final boolean enableFragmenting;

		/**
		 * Size in byte for one fragment
		 */
		private final long fragmentSize;

		private final NetInterfaceName name;

		private final NetID localNetId;

		private final PhyType underlyingPhy;

		private final RoutingAlgorithm routing;

		private final List<NetMessageListener> netListeners;

		private final List<ConnectivityListener> connListeners = new LinkedList<ConnectivityListener>();

		private NetlayerAnalyzer netAnalyzerProxy;

		private boolean hasAnalyzer = false;

		public NetInterfaceImpl(NetInterfaceName name, NetID localNetId,
				PhyType phy, RoutingAlgorithm routing,
				boolean enableFragmenting, long fragmentSize) {
			this.enableFragmenting = enableFragmenting;
			this.fragmentSize = fragmentSize;
			this.localNetId = localNetId;
			this.name = name;
			this.underlyingPhy = phy;
			this.routing = routing;
			this.netListeners = new LinkedList<NetMessageListener>();
		}

		/**
		 * Called by the NetComponent-wrapper
		 */
		@Override
		public void initialize() {
			getHost().getLinkLayer().addMacStateListener(this);
			getHost().getLinkLayer().addLinkMessageListener(this);
			routing.setMessageListener(this);
			routing.setNetInterface(this);

			/*
			 * For convenience, all hosts are assumed to be online at the
			 * beginning of the Simulation. If you do not want this behavior,
			 * you can set START_HOSTS_OFFLINE in the configuration of the
			 * factory.
			 */
			if (!startOffline) {
				goOnline();
			}

			try {
				netAnalyzerProxy = Monitor.get(NetlayerAnalyzer.class);
				hasAnalyzer = true;
			} catch (AnalyzerNotAvailableException e) {
				// No analyzer, no problem.
			}
		}
		
		@Override
		public void shutdown() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void send(Message msg, NetID receiver, NetProtocol protocol) {
			/*
			 * TODO Lateron, the protocol should not be needed anymore, instead
			 * we rely on the NetInterface (our local IP), as soon as multiple
			 * IPs per Host are available.
			 */
			if (routing == null) {
				throw new UnsupportedOperationException(
						"There is no handler (routing algorithm) for the NetProtocol "
								+ protocol.toString() + " defined.");
			}

			/*
			 * Fragmenting
			 */
			if (enableFragmenting) {
				long messageSize = msg.getSize();
				int numberOfFragments = (int) Math.ceil((double) messageSize
						/ (double) fragmentSize);
				long lastFragmentSize = messageSize % fragmentSize;
				NetMessage nMsg = null;
				FragmentReceivedInfo fragmentInfo = new FragmentReceivedInfo();
				for (int i = 1; i <= numberOfFragments; i++) {
					if (i == numberOfFragments) {
						nMsg = new RoutedNetMessage(msg, receiver, getNetID(),
								protocol, lastFragmentSize, i,
								numberOfFragments, fragmentInfo);
						/*
						 * Inform the monitor only once (after the last fragment
						 * has been sent), but with the complete Message
						 */
						RoutedNetMessage complete = new RoutedNetMessage(msg,
								receiver, getNetID(), protocol);

						if (hasAnalyzer) {
							netAnalyzerProxy.netMsgEvent(complete, getHost(),
									Reason.SEND);
						}

					} else {
						nMsg = new RoutedNetMessage(msg, receiver, getNetID(),
								protocol, fragmentSize, i, numberOfFragments,
								fragmentInfo);
					}
					routing.route(nMsg);
				}
				_avgFragments.newVal(numberOfFragments);
			} else {
				NetMessage nMsg = new RoutedNetMessage(msg, receiver,
						getNetID(), protocol);
				/*
				 * Notify NetLayer Monitor
				 */
				if (hasAnalyzer) {
					netAnalyzerProxy.netMsgEvent(nMsg, getHost(),
							Reason.SEND);
				}

				/*
				 * let the RoutingAlgorithm do the heavy lifting.
				 */
				routing.route(nMsg);
			}
		}

		@Override
		public void deliverMessage(NetMessage msg) {
			/*
			 * Specified by the RoutingListener
			 */
			if (!msg.getReceiver().equals(localNetId)
					&& !msg.getReceiver().equals(IPv4NetID.LOCAL_BROADCAST)) {
				throw new UnsupportedOperationException(
						"The message was intended for another NetLayer: "
								+ msg.getReceiver());
			}

			assert !(msg.getPayload() instanceof RoutingMessage) : "Routing messages are not allowed!";
			assert msg instanceof RoutedNetMessage;

			RoutedNetMessage rMsg = (RoutedNetMessage) msg;

			if (!rMsg.getReceiver().equals(IPv4NetID.LOCAL_BROADCAST)) {
				// analyze average hop count
				_avgHops.newVal(rMsg.getRoutedNetMessageHopCount());
			}

			/*
			 * Re-Assemble partitioned messages!
			 */
			RoutedNetMessage completeMessage;
			if (enableFragmenting) {
				/*
				 * Check, if the message is complete
				 */
				if (!rMsg.getFragmentReceiverInfo().receivedFragment(
						getNetID(), rMsg.getFragmentNumber(),
						rMsg.getTotalNumberOfFragments())) {
					// only a fragment arrived, not yet complete
					return;
				}
				completeMessage = new RoutedNetMessage(msg.getPayload(),
						msg.getReceiver(), msg.getSender(),
						msg.getNetProtocol());
			} else {
				completeMessage = rMsg;
			}

			/*
			 * Notify NetLayer Monitor with the COMPLETE message
			 */
			NetMessageEvent nme = new DefaultNetMessageEvent(
					completeMessage.getNetProtocol(),
					completeMessage.getSender(), localNetId,
					completeMessage.getPayload());

			if (hasAnalyzer) {
				netAnalyzerProxy.netMsgEvent(completeMessage, getHost(),
						Reason.RECEIVE);
			}

			for (NetMessageListener listener : netListeners) {
				listener.messageArrived(nme);
			}
		}

		@Override
		public void messageArrived(LinkMessageEvent linkMsgEvent) {
			if (linkMsgEvent.getPhyType() == underlyingPhy) {
				/*
				 * This is triggered by the LinkLayer, we dispatch to the
				 * correct routing algorithm
				 */
				if (linkMsgEvent.getPayload() instanceof RoutedNetMessage) {
					RoutedNetMessage nMsg = (RoutedNetMessage) linkMsgEvent
							.getPayload();
					/*
					 * increment hop-count for Unicast-Msgs
					 */
					if (!linkMsgEvent.isBroadcast()) {
						nMsg.incrementRoutedNetMessageHopCount();
					}

					if (routing == null) {
						throw new UnsupportedOperationException(
								"There is no handler (routing algorithm) for the NetProtocol "
										+ nMsg.getNetProtocol().toString()
										+ " defined.");
					}

					/*
					 * DEBUG: trace the path through the network
					 */
					// nMsg.traceHop(getNetID());

					routing.handleMessage(nMsg, linkMsgEvent.getPhyType(),
							linkMsgEvent.getSender());
				} else {
					/*
					 * TODO RKluge / MSt: The following else statement was removed by me because it makes additional components that don't send RoutedNetMessages on top of the LinkLayer impossible
					 */
					
					throw new UnsupportedOperationException(
							"The RoutedNetLayer only accepts RoutedNetMessages from the LinkLayer.");
				}
			}
		}

		@Override
		public NetInterfaceName getName() {
			return name;
		}

		@Override
		public int getMTU() {
			return underlyingPhy.getDefaultMTU();
		}

		@Override
		public NetID getLocalInetAddress() {
			return localNetId;
		}

		@Override
		public NetID getBroadcastAddress() {
			return IPv4NetID.LOCAL_BROADCAST;
		}

		@Override
		public NetID getByName(String name) {
			return new IPv4NetID(name);
		}

		@Override
		public boolean isUp() {
			return (isOnline() && getHost().getLinkLayer()
					.getMac(underlyingPhy).isOnline());
		}

		@Override
		public void addConnectivityListener(ConnectivityListener listener) {
			connListeners.add(listener);
		}

		@Override
		public void removeConnectivityListener(ConnectivityListener listener) {
			connListeners.remove(listener);
		}

		@Override
		public void goneOffline(PhyType phy) {
			if (phy == underlyingPhy) {
				for (ConnectivityListener listener : connListeners) {
					listener.wentOffline(getHost(), this);
				}
			}
		}

		@Override
		public void goneOnline(PhyType phy) {
			if (phy == underlyingPhy) {
				for (ConnectivityListener listener : connListeners) {
					listener.wentOnline(getHost(), this);
				}
			}
		}

		@Override
		public Bandwidth getCurrentBandwidth() {
			return getHost().getLinkLayer().getCurrentBandwidth(underlyingPhy);
		}

		@Override
		public Bandwidth getMaxBandwidth() {
			return getHost().getLinkLayer().getMaxBandwidth(underlyingPhy);
		}

		@Override
		public void addNetMsgListener(NetMessageListener listener) {
			netListeners.add(listener);
		}

		@Override
		public void removeNetMsgListener(NetMessageListener listener) {
			netListeners.remove(listener);
		}

		@Override
		public void goOnline() {
			boolean wentOnline = false;
			if (host.getLinkLayer().hasPhy(underlyingPhy)
					&& !host.getLinkLayer().isOnline(underlyingPhy)) {
				host.getLinkLayer().goOnline(underlyingPhy);
				wentOnline |= host.getLinkLayer().isOnline(underlyingPhy);
			}
			if (wentOnline) {
				try {
					Monitor.log(RoutedNetLayer.class, Level.INFO,
							"Netlayer %s of host %s went online.", name, host);
					Monitor.get(ConnectivityAnalyzer.class).wentOnline(host);
				} catch (AnalyzerNotAvailableException e) {
					// No analyzer, no problem.
				}
			}
		}

		@Override
		public void goOffline() {
			boolean wentOffline = false;
			if (host.getLinkLayer().hasPhy(underlyingPhy)
					&& host.getLinkLayer().isOnline(underlyingPhy)) {
				host.getLinkLayer().goOffline(underlyingPhy);
				wentOffline |= !host.getLinkLayer().isOnline(underlyingPhy);
			}
			if (wentOffline) {
				try {
					Monitor.log(RoutedNetLayer.class, Level.INFO,
							"Netlayer %s of host %s went offline.", name, host);
					Monitor.get(ConnectivityAnalyzer.class).wentOffline(host);
				} catch (AnalyzerNotAvailableException e) {
					// No analyzer, no problem.
				}
			}
		}

		@Override
		public boolean isOnline() {
			/*
			 * Online, if one+ PHY is online
			 */
			if (host.getLinkLayer().hasPhy(underlyingPhy)
					&& host.getLinkLayer().isOnline(underlyingPhy)) {
				return true;
			}
			return false;
		}

		@Override
		public boolean isOffline() {
			return !isOnline();
		}
		
		@Override
		public boolean isActive() {
			return isOnline();
		}
		
		@Override
		public void startComponent() {
			goOnline();
		}
		
		@Override
		public void stopComponent() {
			goOffline();
		}

		@Override
		public NetID getNetID() {
			return localNetId;
		}

		@Override
		public Location getNetPosition() {
			return host.getTopologyComponent().getRealPosition();
		}

		@Override
		public SimHost getHost() {
			return RoutedNetLayer.this.getHost();
		}

	}

	@Override
	public Iterable<NetInterface> getNetworkInterfaces() {
		return new LinkedList<NetInterface>(nets.values());
	}

	@Override
	public Iterable<SimNetInterface> getSimNetworkInterfaces() {
		return nets.values();
	}

	@Override
	public SimNetInterface getByNetId(NetID netID) {
		for (SimNetInterface net : nets.values()) {
			if (net.getLocalInetAddress().equals(netID)) {
				return net;
			}
		}
		return null;
	}

	@Override
	public SimNetInterface getByName(NetInterfaceName name) {
		return nets.get(name);
	}

}
