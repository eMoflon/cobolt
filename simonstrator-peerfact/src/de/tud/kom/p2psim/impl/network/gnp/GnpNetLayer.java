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


package de.tud.kom.p2psim.impl.network.gnp;

import java.util.HashMap;
import java.util.Map;

import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.IPv4Message;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.gnp.topology.GnpPosition;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * 
 * @author geraldklunker
 * 
 */
public class GnpNetLayer extends AbstractNetLayer implements EventHandler {

	private GeoLocation geoLocation;

	private GnpSubnet subnet;

	private long nextFreeSendingTime = 0;

	private long nextFreeReceiveTime = 0;

	private Map<GnpNetLayer, GnpNetBandwidthAllocation> connections = new HashMap<GnpNetLayer, GnpNetBandwidthAllocation>();
	
	private static final int EVENT_RECEIVE = 1;

	public GnpNetLayer(SimHost host, GnpSubnet subNet, IPv4NetID netID,
			GnpPosition netPosition, GeoLocation geoLoc, BandwidthImpl maxBW) {
		super(host, netID, maxBW, netPosition, null);
		this.subnet = subNet;
		this.geoLocation = geoLoc;
		subNet.registerNetLayer(this);
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	/**
	 * 
	 * @return 2-digit country code
	 */
	public String getCountryCode() {
		return geoLocation.getCountryCode();
	}

	/**
	 * 
	 * @return first time sending is possible (line is free)
	 */
	public long getNextFreeSendingTime() {
		return nextFreeSendingTime;
	}

	/**
	 * 
	 * @param time
	 *            first time sending is possible (line is free)
	 */
	public void setNextFreeSendingTime(long time) {
		nextFreeSendingTime = time;
	}

	/**
	 * 
	 * @param netLayer
	 * @return
	 */
	public boolean isConnected(GnpNetLayer netLayer) {
		return connections.containsKey(netLayer);
	}

	/**
	 * 
	 * @param netLayer
	 * @param allocation
	 */
	public void addConnection(GnpNetLayer netLayer,
			GnpNetBandwidthAllocation allocation) {
		connections.put(netLayer, allocation);
	}

	/**
	 * 
	 * @param netLayer
	 * @return
	 */
	public GnpNetBandwidthAllocation getConnection(GnpNetLayer netLayer) {
		return connections.get(netLayer);
	}

	/**
	 * 
	 * @param netLayer
	 */
	public void removeConnection(GnpNetLayer netLayer) {
		connections.remove(netLayer);
	}

	/**
	 * 
	 * @param msg
	 */
	public void addToReceiveQueue(IPv4Message msg) {
		long receiveTime = subnet.getLatencyModel().getTransmissionDelay(
				msg.getSize(), getMaxBandwidth().getDownBW());
		long currenTime = Time.getCurrentTime();
		long arrivalTime = nextFreeReceiveTime + receiveTime;
		if (arrivalTime <= currenTime) {
			nextFreeReceiveTime = currenTime;
			receive(msg);
		} else {
			nextFreeReceiveTime = arrivalTime;
			Event.scheduleWithDelay(arrivalTime - Time.getCurrentTime(), this,
					msg, EVENT_RECEIVE);
		}
	}

	@Override
	public boolean isSupported(TransProtocol transProtocol) {
		return (transProtocol.equals(TransProtocol.UDP) || transProtocol
				.equals(TransProtocol.TCP));
	}

	public void send(Message msg, NetID receiver, NetProtocol netProtocol) {
		// outer if-else-block is used to avoid sending although the host is
		// offline
		if (this.isOnline()) {
			TransProtocol usedTransProtocol = ((AbstractTransMessage) msg)
					.getProtocol();
			if (this.isSupported(usedTransProtocol)) {
				NetMessage netMsg = new IPv4Message(msg, receiver,
						this.getLocalInetAddress());
				if (hasAnalyzer) {
					netAnalyzerProxy
							.netMsgEvent(netMsg, getHost(), Reason.SEND);
				}
				this.subnet.send(netMsg);
			} else
				throw new IllegalArgumentException("Transport protocol "
						+ usedTransProtocol
						+ " not supported by this NetLayer implementation.");
		} else {
			int assignedMsgId = subnet.determineTransMsgNumber(msg);
			((AbstractTransMessage) msg).setCommId(assignedMsgId);

			if (hasAnalyzer) {
				NetMessage netMsg = new IPv4Message(msg, receiver,
						this.getLocalInetAddress());
				netAnalyzerProxy.netMsgEvent(netMsg, getHost(), Reason.DROP);
			}
		}

	}

	@Override
	public String toString() {
		return this.getNetID().toString() + " ( "
				+ this.getHost().getProperties().getGroupID() + " )";
	}

	
	@Override
	public void eventOccurred(Object content, int type) {
		if (type == EVENT_RECEIVE) {
			receive((NetMessage) content);
		}
	}

	public void goOffline() {
		super.goOffline();
		subnet.goOffline(this);
	}

	public void cancelTransmission(int commId) {
		subnet.cancelTransmission(commId);
	}

}