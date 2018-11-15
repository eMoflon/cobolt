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

package de.tud.kom.p2psim.impl.network;

import java.util.LinkedList;
import java.util.List;

import de.tud.kom.p2psim.api.analyzer.ConnectivityAnalyzer;
import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.analyzer.NetlayerAnalyzer;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.network.NetMessageListener;
import de.tud.kom.p2psim.api.network.NetMsgEvent;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tud.kom.p2psim.api.network.SimNetworkComponent;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.AnalyzerNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.Bandwidth;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ConnectivityListener;

/**
 * This abstract class provides a skeletal implementation of the
 * <code>StandaloneNetLayer<code> interface to lighten the effort for implementing this interface.
 * 
 * All Netlayers implementing this abstract base class can only provide exactly one {@link NetInterface}, 
 * i.e., they do not have multiple communication interfaces. For simulations with multiple interfaces, 
 * you need to use the routedNetLayer and corresponding Linklayers.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 11/29/2007
 * 
 */
public abstract class AbstractNetLayer implements SimNetworkComponent,
		SimNetInterface {

	private final List<NetMessageListener> msgListeners;

	private final List<ConnectivityListener> connListeners;

	private final NetID myID;

	protected NetMeasurementDB.Host hostMeta;

	private boolean online;

	private Location position;

	Bandwidth currentBandwidth;

	Bandwidth maxBandwidth;

	private final SimHost host;

	protected boolean hasAnalyzer = false;

	protected NetlayerAnalyzer netAnalyzerProxy;

	/**
	 * Abstract constructor called by a subclass of this instance
	 * 
	 * @param maxDownBandwidth
	 *            the maximum physical download bandwidth
	 * @param maxUpBandwidth
	 *            the maximum physical upload bandwidth
	 * @param position
	 *            the NetPosition of the network layer
	 */
	public AbstractNetLayer(SimHost host, NetID netId, Bandwidth maxBandwidth,
			Location position, NetMeasurementDB.Host hostMeta) {
		this.myID = netId;
		this.msgListeners = new LinkedList<NetMessageListener>();
		this.connListeners = new LinkedList<ConnectivityListener>();
		this.maxBandwidth = maxBandwidth;
		this.currentBandwidth = maxBandwidth.clone();
		this.position = position;
		this.hostMeta = hostMeta;
		this.host = host;
	}

	@Override
	public void initialize() {
		/*
		 * Bind the analyzer-Proxy
		 */
		try {
			netAnalyzerProxy = Monitor.get(NetlayerAnalyzer.class);
			hasAnalyzer = true;
		} catch (AnalyzerNotAvailableException e) {
			// no analyzer, no problem
		}

	}

	@Override
	public void shutdown() {
		throw new AssertionError(
				"You are not supposed to shutdown this component.");
	}

	/**
	 * This message is called by the subnet to deliver a new NetMessage to a
	 * remote NetLayer. (@see de.tud.kom.p2psim.impl.network.AbstractSubnet).
	 * Calling this method informs further all registered NetMsgListeners about
	 * the receipt of this NetMessage using a appropriate NetMsgEvent.
	 * 
	 * @param message
	 *            The NetMessage that was received by the NetLayer.
	 */
	public void receive(NetMessage message) {
		if (this.isOnline()) {

			// log.info(Simulator.getSimulatedRealtime() + " Receiving " +
			// message);

			if (hasAnalyzer) {
				netAnalyzerProxy
						.netMsgEvent(message, getHost(),
					Reason.RECEIVE);
			}
			NetMsgEvent event = new NetMsgEvent(message, this);
			if (msgListeners == null || msgListeners.isEmpty()) {
				if (hasAnalyzer) {
					netAnalyzerProxy.netMsgEvent(message, getHost(),
						Reason.DROP);
				}
				Monitor.log(AbstractNetLayer.class, Level.WARN,
						"Cannot deliver message "
						+ message.getPayload() + " at netID=" + myID
						+ " as no message msgListeners registered");
			} else {
				for (NetMessageListener listener : msgListeners) {
					listener.messageArrived(event);
				}
			}
		} else {
			if (hasAnalyzer) {
				netAnalyzerProxy.netMsgEvent(message, getHost(), Reason.DROP);
			}
		}
	}

	/**
	 * Return whether the required transport protocol is supported by the given
	 * NetLayer instance
	 * 
	 * @param protocol
	 *            the required transport protocol
	 * @return true if supported
	 */
	protected abstract boolean isSupported(TransProtocol protocol);

	/**
	 * As the download bandwidth of a host might be shared between concurrently
	 * established connections, this method will be used by the subnet in order
	 * to adapt the current available download bandwidth.
	 * 
	 * @param currentDownBandwidth
	 *            the new available download bandwidth
	 */
	@Deprecated
	public void setCurrentDownBandwidth(long currentDownBandwidth) {
		this.currentBandwidth.setDownBW(currentDownBandwidth);
	}

	/**
	 * As the upload bandwidth of a host might be shared between concurrently
	 * established connections, this method will be used by the subnet in order
	 * to adapt the current available upload bandwidth.
	 * 
	 * @param currentUpBandwidth
	 *            the new available upload bandwidth
	 */
	@Deprecated
	public void setCurrentUpBandwidth(long currentUpBandwidth) {
		this.currentBandwidth.setUpBW(currentUpBandwidth);
	}

	public void setCurrentBandwidth(Bandwidth currentBandwidth) {
		this.currentBandwidth = currentBandwidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.tud.kom.p2psim.api.api.network.NetLayer#addNetMsgListener(
	 * NetMessageListener) listener)
	 */
	public void addNetMsgListener(NetMessageListener listener) {
		if (!this.msgListeners.contains(listener)) {
			this.msgListeners.add(listener);
		}
	}

	public List<NetMessageListener> getNetMsgListeners() {
		return this.msgListeners;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.tud.kom.p2psim.api.api.network.NetLayer#removeNetMsgListener(
	 * NetMessageListener) listener)
	 */
	public void removeNetMsgListener(NetMessageListener listener) {
		this.msgListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.api.network.NetLayer#getNetID()
	 */
	public NetID getNetID() {
		return this.myID;
	}

	@Override
	public NetID getLocalInetAddress() {
		return this.myID;
	}

	@Override
	public NetID getBroadcastAddress() {
		return IPv4NetID.LOCAL_BROADCAST;
	}

	@Override
	public SimNetInterface getByName(NetInterfaceName name) {
		if (name == NetInterfaceName.ETHERNET) {
			return this;
		} else {
			throw new AssertionError("This NetLayer supports only ETHERNET!");
		}
	}

	@Override
	public NetID getByName(String name) {
		return new IPv4NetID(name);
	}

	@Override
	public SimNetInterface getByNetId(NetID netID) {
		if (getLocalInetAddress().equals(netID)) {
			return this;
		} else {
			return null;
		}
	}

	@Override
	public Iterable<NetInterface> getNetworkInterfaces() {
		List<NetInterface> list = new LinkedList<NetInterface>();
		list.add(this);
		return list;
	}

	@Override
	public Iterable<SimNetInterface> getSimNetworkInterfaces() {
		List<SimNetInterface> list = new LinkedList<SimNetInterface>();
		list.add(this);
		return list;
	}

	@Override
	public NetInterfaceName getName() {
		return NetInterfaceName.ETHERNET;
	}

	@Override
	public int getMTU() {
		return PhyType.ETHERNET.getDefaultMTU();
	}

	@Override
	public boolean isUp() {
		return isOnline();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.api.network.NetLayer#goOffline()
	 */
	public void goOffline() {
		if (this.online) {
			this.online = false;
			for (ConnectivityListener connListener : connListeners) {
				connListener.wentOffline(host, this);
			}
			try {
				Monitor.get(ConnectivityAnalyzer.class).wentOffline(host);
			} catch (AnalyzerNotAvailableException e) {
				//
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.api.network.NetLayer#goOnline()
	 */
	public void goOnline() {
		if (!this.online) {
			this.online = true;
			for (ConnectivityListener connListener : connListeners) {
				connListener.wentOnline(host, this);
			}
			try {
				Monitor.get(ConnectivityAnalyzer.class).wentOnline(host);
			} catch (AnalyzerNotAvailableException e) {
				//
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.api.network.NetLayer#isOffline()
	 */
	@Override
	public boolean isOffline() {
		return !online;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.api.network.NetLayer#isOnline()
	 */
	@Override
	public boolean isOnline() {
		return online;
	}
	
	@Override
	public boolean isActive() {
		return online;
	}
	
	@Override
	public void startComponent() {
		goOnline();
	}
	
	@Override
	public void stopComponent() {
		goOffline();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.common.Component#getHost()
	 */
	@Override
	public SimHost getHost() {
		return this.host;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.api.network.NetLayer#getNetPosition()
	 */
	@Override
	public Location getNetPosition() {
		return this.position;
	}

	@Override
	public Bandwidth getCurrentBandwidth() {
		return currentBandwidth;
	}

	@Override
	public Bandwidth getMaxBandwidth() {
		return maxBandwidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tud.kom.p2psim.api.network.NetLayer#removeConnectivityListener(de.
	 * tud.kom.p2psim.api.common.ConnectivityListener)
	 */
	@Override
	public void removeConnectivityListener(ConnectivityListener listener) {
		this.connListeners.remove(listener);
	}

	@Override
	public void addConnectivityListener(ConnectivityListener listener) {
		this.connListeners.add(listener);
	}

	/**
	 * Gets the dB host meta from NetMessurementDB
	 * 
	 * @return the dB host meta
	 */
	public NetMeasurementDB.Host getDBHostMeta() {
		return hostMeta;
	}

}
