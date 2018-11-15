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



package de.tud.kom.p2psim.impl.network.simple;

import java.util.HashMap;
import java.util.Map;

import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.AbstractSubnet;
import de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * The default implementation of the SubNet interface.
 * 
 * @author Sebastian Kaune
 */
public class SimpleSubnet extends AbstractSubnet implements EventHandler {

	Map<NetID, NetLayer> netLayers;

	LatencyStrategy netLatencyModel;

	public static final double SUBNET_WIDTH = 2d;

	public static final double SUBNET_HEIGHT = 2d;

	public static final long inOrderOffset = 1;

	Map<SimpleSubnet.LinkID, Long> links = new HashMap<SimpleSubnet.LinkID, Long>();

	public SimpleSubnet() {
		netLayers = new HashMap<NetID, NetLayer>();
	}

	@Override
	public void registerNetLayer(NetLayer net) {
		netLayers.put(net.getNetID(), net);
	}

	@Override
	public void send(NetMessage msg) {
		// In order to enable TransMessageCallbacks at the transport layer on
		// every implemented network layer and subnet, AbstractSubnet contains
		// a method, which determines the next number for a message at the
		// transport layer.
		AbstractTransMessage transMsg = (AbstractTransMessage) msg.getPayload();
		transMsg.setCommId(determineTransMsgNumber(msg));
		
		SimpleNetLayer sender = (SimpleNetLayer) netLayers.get(msg.getSender());
		SimpleNetLayer receiver = (SimpleNetLayer) netLayers.get(msg.getReceiver());
		
		long latency = netLatencyModel.getMessagePropagationDelay(msg, sender, receiver, null);
		Monitor.log(SimpleSubnet.class, Level.INFO, "Send from " + sender
				+ " to " + receiver + " with delay " + latency);
		
		scheduleReceiveEvent(msg, receiver, latency);
	}

	void scheduleReceiveEvent(NetMessage msg, NetLayer receiver, long latency) {
		LinkID link = new LinkID(msg.getSender(), msg.getReceiver());
		long lastArrivalTime = getLastArrivalTime(link);

		long newArrivalTime = Time.getCurrentTime() + latency;
		if (lastArrivalTime > newArrivalTime) { // assure ordered delivery
			newArrivalTime = lastArrivalTime + inOrderOffset;
		}
		links.put(link, newArrivalTime);

		Event.scheduleWithDelay(newArrivalTime - Time.getCurrentTime(), this,
				msg, 0);
	}

	long getLastArrivalTime(LinkID link) {
		long lastArrivalTime = (links.containsKey(link)) ? links.get(link) : -1;
		return lastArrivalTime;
	}

	static class LinkID {
		private NetID srcId;

		private NetID dstId;

		public LinkID(NetID srcId, NetID dstId) {
			super();
			this.srcId = srcId;
			this.dstId = dstId;
		}

		@Override
		public boolean equals(Object obj) {
			if (!LinkID.class.isInstance(obj))
				return false;
			LinkID id2 = (LinkID) obj;
			return srcId.equals(id2.srcId) && dstId.equals(id2.dstId);
		}

		@Override
		public int hashCode() {
			int hCode = 17;
			hCode += (37 * srcId.hashCode());
			hCode += (37 * dstId.hashCode());
			return hCode;
		}

	}

	public void clear() {
		netLayers.clear();
		// NetworkLink.clearPool();
		links.clear();
		// singleton = new SimpleSubnet();
	}

	public void setLatencyModel(LatencyStrategy model) {
		this.netLatencyModel = model;
	}

	/**
	 * Dispatch the arriving message to its destination.
	 * 
	 * @param se
	 *            event containing the network message
	 */
	public void eventOccurred(Object se, int type) {
		SimpleNetMessage msg = (SimpleNetMessage) se;
		NetID senderID = msg.getSender();
		NetID receiverID = msg.getReceiver();
		NetLayer receiver = netLayers.get(receiverID);
		LinkID linkID = new LinkID(senderID, receiverID);
		long lastArrivalTime = getLastArrivalTime(linkID);
		if (lastArrivalTime == Time.getCurrentTime()) {
			links.remove(linkID);
			assert !links.containsKey(linkID);
		}
		((SimpleNetLayer) receiver).receive(msg);
	}

}
