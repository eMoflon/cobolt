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

package de.tud.kom.p2psim.impl.network.routed.routing;

import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.analyzer.NetlayerAnalyzer;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.network.routing.RoutingAlgorithm;
import de.tud.kom.p2psim.api.network.routing.RoutingListener;
import de.tud.kom.p2psim.api.network.routing.RoutingMessage;
import de.tud.kom.p2psim.impl.network.routed.RoutedNetMessage;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.AnalyzerNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;

/**
 * Abstract base class for a routing Algorithm. This provides some
 * convenience-methods for forwarding of payload and for the exchange of
 * control-messages.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 29.02.2012
 */
public abstract class AbstractRoutingAlgorithm implements RoutingAlgorithm {

	private SimHost host;

	private RoutingListener netlayer;

	private RoutingType type;

	private final NetInterfaceName netName;

	private NetInterface netInterface;

	private boolean hasAnalyzer = false;

	private NetlayerAnalyzer netAnalyzerProxy;

	public static enum DropReason {
		NO_PATH_FOUND, PATH_BROKEN, PATH_OUTDATED, ADRESS_RESOLUTION_FAILED
	}

	/**
	 * Creates a new Routing Algorithm to work on the given NetProtocol
	 * 
	 * @param type
	 * @param protocol
	 */
	public AbstractRoutingAlgorithm(SimHost host, RoutingType type,
			NetInterfaceName netName) {
		this.type = type;
		this.netName = netName;
		this.host = host;
	}

	@Override
	public void shutdown() {
		throw new AssertionError(
				"You are not supposed to shutdown this component.");
	}

	@Override
	public final SimHost getHost() {
		return host;
	}

	public NetInterface getNetInterface() {
		return netInterface;
	}

	@Override
	public NetInterfaceName getNetInterfaceName() {
		return netName;
	}

	@Override
	public void setNetInterface(NetInterface net) {
		netInterface = net;
		netInterface.addConnectivityListener(this);
		initialize();

		// binding the analyzer
		try {
			netAnalyzerProxy = Monitor.get(NetlayerAnalyzer.class);
			hasAnalyzer = true;
		} catch (AnalyzerNotAvailableException e) {
			// no analyzer, no problem
		}
	}

	/**
	 * Convenience-getter for our own NetID
	 * 
	 * @return
	 */
	public NetID getNetID() {
		return netInterface.getLocalInetAddress();
	}

	@Override
	public final RoutingType getType() {
		return type;
	}

	@Override
	public final void setMessageListener(RoutingListener listener) {
		this.netlayer = listener;
	}


	/**
	 * Call this, whenever a message is dropped inside your routing algorithm.
	 * If you overwrite the method make sure to call super, as this allows
	 * consistent analyzing
	 * 
	 * @param reason
	 * @param msg
	 */
	protected void messageDropped(DropReason reason, NetMessage msg) {
		if (hasAnalyzer) {
			Monitor.log(AbstractRoutingAlgorithm.class, Level.INFO,
					"Dropped because of %s - msg: %s", reason, msg);
			netAnalyzerProxy.netMsgEvent(msg, host, Reason.DROP);
		}
	}


	/**
	 * Call this if the {@link NetMessage} is intended for the upper layers and
	 * is to be processed by the NetLayer.
	 * 
	 * @param nme
	 */
	protected void notifyNetLayer(NetMessage msg) {
		netlayer.deliverMessage(msg);
	}

	/**
	 * Send a Control-Message to the receivers Routing Algorithm. The message
	 * will be encapsulated into a {@link RoutedNetMessage}. If your algorithm
	 * adds information to forwarded payload, you just introduce a new Message
	 * extending {@link RoutingMessage} and add the forwarded message as payload
	 * 
	 * @param msg
	 * @param receiver
	 * @param phy
	 *            the PHY to use
	 */
	protected final void sendRoutingMsg(RoutingMessage msg, NetID receiver,
			PhyType phy) {
		RoutedNetMessage rNet = new RoutedNetMessage(msg, receiver,
				getNetInterface().getLocalInetAddress(), NetProtocol.IPv4);
		MacAddress macReceiver = host.getLinkLayer().addressResolution(receiver, phy);
		if (receiver == null) {
			messageDropped(DropReason.ADRESS_RESOLUTION_FAILED, rNet);
		} else {
			host.getLinkLayer().send(phy, macReceiver, rNet);
		}
	}

	/**
	 * If your algorithm does not add any information to the forwarded payload
	 * but instead just knows the IP of the next hop, it should use this method
	 * to forward the message to the next hop. Otherwise, you have to add the
	 * NetMessage as payload to a {@link RoutingMessage} and then use
	 * sendRoutingMsg()
	 * 
	 * @param netMsg
	 * @param nextHop
	 * @param phy
	 */
	protected final void forwardNetMessage(NetMessage netMsg, NetID nextHop,
			PhyType phy) {
		MacAddress macReceiver = host.getLinkLayer().addressResolution(nextHop,
				phy);
		if (macReceiver == null) {
			messageDropped(DropReason.ADRESS_RESOLUTION_FAILED, netMsg);
		} else {
			host.getLinkLayer().send(phy, macReceiver, netMsg);
			// log.error("FORWARDED, hop "
			// + ((RoutedNetMessage) netMsg).getRoutedNetMessageHopCount()
			// + ": " + netMsg.toString() + " payload: "
			// + netMsg.getPayload().getPayload().toString());
		}
	}

	/**
	 * Called after the host went online
	 */
	protected abstract void start();

	/**
	 * Called after the host went offline
	 */
	protected abstract void stop();

	@Override
	public void wentOffline(Host host, NetInterface netInterface) {
		stop();
	}

	@Override
	public void wentOnline(Host host, NetInterface netInterface) {
		start();
	}

}
