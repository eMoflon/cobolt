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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;

/**
 * 
 * 
 * Note: This class contains many inconsistencies. Discovered by simulating
 * Gnutella06v2 with 10000 peers. Tried to fix them. Leo Nobach
 * 
 * @author Gerald Klunker
 * @version 0.1, 09.01.2008
 * 
 */
public abstract class AbstractGnpNetBandwidthManager {

	public enum BandwidthAllocation {
		PERIODICAL, EVENT
	}

	protected Map<AbstractNetLayer, Map<AbstractNetLayer, GnpNetBandwidthAllocation>> connectionsSenderToReceiver;

	protected Map<AbstractNetLayer, Map<AbstractNetLayer, GnpNetBandwidthAllocation>> connectionsReceiverToSender;

	public AbstractGnpNetBandwidthManager() {
		connectionsSenderToReceiver = new HashMap<AbstractNetLayer, Map<AbstractNetLayer, GnpNetBandwidthAllocation>>();
		connectionsReceiverToSender = new HashMap<AbstractNetLayer, Map<AbstractNetLayer, GnpNetBandwidthAllocation>>();
	}

	public GnpNetBandwidthAllocation addConnection(AbstractNetLayer sender,
			AbstractNetLayer receiver, double bandwidth) {
		if (!connectionsSenderToReceiver.containsKey(sender))
			connectionsSenderToReceiver.put(sender,
					new HashMap<AbstractNetLayer, GnpNetBandwidthAllocation>());
		if (!connectionsSenderToReceiver.get(sender).containsKey(receiver))
			connectionsSenderToReceiver.get(sender).put(receiver,
					new GnpNetBandwidthAllocation(sender, receiver));
		GnpNetBandwidthAllocation c = connectionsSenderToReceiver.get(sender)
				.get(receiver);
		if (!connectionsReceiverToSender.containsKey(receiver))
			connectionsReceiverToSender.put(receiver,
					new HashMap<AbstractNetLayer, GnpNetBandwidthAllocation>());
		if (!connectionsReceiverToSender.get(receiver).containsKey(sender))
			connectionsReceiverToSender.get(receiver).put(sender, c);
		c.setAllocatedBandwidth(0);
		sender.getCurrentBandwidth().setUpBW(sender.getMaxBandwidth().getUpBW());
		receiver.getCurrentBandwidth().setDownBW(sender.getMaxBandwidth().getDownBW());
		c.setBandwidthNeeds(c.getBandwidthNeeds() + bandwidth);
		return c;
	}

	public GnpNetBandwidthAllocation removeConnection(AbstractNetLayer sender,
			AbstractNetLayer receiver, double bandwidth) {
		GnpNetBandwidthAllocation ba = null;
		if (connectionsSenderToReceiver.containsKey(sender)) {
			ba = connectionsSenderToReceiver.get(sender).get(receiver);
			if (ba != null) { // Disabled a NullPointerException
				if (bandwidth < 0)
					ba.setBandwidthNeeds(0);
				else {
					ba.setBandwidthNeeds(ba.getBandwidthNeeds() - bandwidth);
				}
				if (ba.getBandwidthNeeds() == 0.0) {
					connectionsSenderToReceiver.get(sender).remove(receiver);
					if (connectionsSenderToReceiver.get(sender).isEmpty())
						connectionsSenderToReceiver.remove(sender);
					connectionsReceiverToSender.get(receiver).remove(sender);
					if (connectionsReceiverToSender.get(receiver).isEmpty())
						connectionsReceiverToSender.remove(receiver);
				}
				ba.setAllocatedBandwidth(0);
			}
		}
		sender.getCurrentBandwidth().setUpBW(sender.getMaxBandwidth().getUpBW());
		receiver.getCurrentBandwidth().setDownBW(sender.getMaxBandwidth().getDownBW());
		return ba;
	}

	public Set<GnpNetBandwidthAllocation> removeConnections(
			AbstractNetLayer netLayer) {
		Set<GnpNetBandwidthAllocation> connections = new HashSet<GnpNetBandwidthAllocation>();
		if (connectionsSenderToReceiver.containsKey(netLayer)) {
			// Disabled a ConcurrentModificationException
			for (AbstractNetLayer receiver : new ArrayList<AbstractNetLayer>(
					connectionsSenderToReceiver.get(netLayer).keySet())) {
				connections.add(removeConnection(netLayer, receiver, -1));
			}
		}
		if (connectionsReceiverToSender.containsKey(netLayer)) {
			// Disabled a ConcurrentModificationException
			for (AbstractNetLayer sender : new ArrayList<AbstractNetLayer>(
					connectionsReceiverToSender.get(netLayer).keySet())) {
				connections.add(removeConnection(sender, netLayer, -1));
			}
		}
		return connections;
	}

	public GnpNetBandwidthAllocation getBandwidthAllocation(NetLayer sender,
			NetLayer receiver) {
		if (connectionsSenderToReceiver.get(sender) == null)
			return null; // Disabled a NullPointerException
		else
			return connectionsSenderToReceiver.get(sender).get(receiver);
	}

	public abstract void allocateBandwidth();

	public abstract BandwidthAllocation getBandwidthAllocationType();

	public abstract Set<GnpNetBandwidthAllocation> getChangedAllocations();

}
