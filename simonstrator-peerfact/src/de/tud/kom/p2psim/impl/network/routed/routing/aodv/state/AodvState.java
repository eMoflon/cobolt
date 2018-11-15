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

package de.tud.kom.p2psim.impl.network.routed.routing.aodv.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.api.network.routing.RoutingMessage;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.AodvConstants;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.AodvRouting;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.message.RouteErrorMessage;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.message.RouteRequestMessage;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.operation.BroadcastHelloOperation;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.operation.RequestRouteOperation;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.operation.OperationCallback;

/**
 * The state of an AODV node.
 * 
 * @author Christoph Neumann
 */
public class AodvState implements SimHostComponent {
	private AodvRouting aodvRouting;
	private NetID aodvNode;
	private int sequenceNo;
	private int rreqId;
	private long lastRreqTime;
	private AodvBuffer<RouteRequestMessage> rreqBuffer = new AodvBuffer<RouteRequestMessage>();
	private Map<NetID, AodvRouteTableEntry> routeTable = new HashMap<NetID, AodvRouteTableEntry>();
	private Map<NetID, RequestRouteOperation> routeOperations = new HashMap<NetID, RequestRouteOperation>();
	private BroadcastHelloOperation helloOp;

	private SimHost host;

	public AodvState(SimHost host, NetID aodvNode, AodvRouting aodvRouting) {
		this.host = host;
		this.aodvNode = aodvNode;
		this.aodvRouting = aodvRouting;
		
		helloOp = new BroadcastHelloOperation(this);
	}

	@Override
	public void initialize() {
		// not called!
	}

	@Override
	public void shutdown() {
		throw new AssertionError(
				"You are not supposed to shutdown this component.");
	}

	@Override
	public SimHost getHost() {
		return host;
	}

	public AodvRouteTableEntry getRouteTableEntry(NetID dest) {
		AodvRouteTableEntry rte = routeTable.get(dest);
		if (rte == null)
			return null;

		return new AodvRouteTableEntry(rte);
	}

	// TODO route deletion
	// atm routes are invalidated (so they are not used to route messages),
	// but not deleted
	
	public void putRouteTableEntry(AodvRouteTableEntry rte) {
		routeTable.put(rte.destination, new AodvRouteTableEntry(rte));

		RequestRouteOperation op = routeOperations.get(rte.destination);
		
		/*
		 * FIXME FIXED (bjoern): if op is finished, do not trigger it again!
		 */
		if (op != null && !op.isFinished())
			op.notifyAboutRouteUpdate();
	}

	public boolean updateRouteTableEntry(AodvRouteTableEntry updatedRte) {
		AodvRouteTableEntry existingRte = getRouteTableEntry(updatedRte.destination);

		if (existingRte == null) { // create new route table entry
			putRouteTableEntry(updatedRte);
			return true;
		}

		// 6.2
		// (i) seq is higher
		if (existingRte.destinationSeqNo == AodvConstants.INVALID_SEQ_NO
				|| existingRte.destinationSeqNo < updatedRte.destinationSeqNo) {
			putRouteTableEntry(updatedRte);
			return true;
		}

		// (ii)
		if (existingRte.destinationSeqNo == updatedRte.destinationSeqNo
				&& !existingRte.isValid()) {
			putRouteTableEntry(updatedRte);
			return true;
		}

		// (iii) seq ==, hop count is smaller
		if (existingRte.destinationSeqNo == updatedRte.destinationSeqNo
				&& updatedRte.hopCount < existingRte.hopCount) {
			// check if +1 is needed in the if (upd.hop +1)
			putRouteTableEntry(updatedRte);
			return true;
		}

		/*
		 * FIXME fixed (bjoern) update lifetime on contact
		 */
		if (existingRte.lifetime < updatedRte.lifetime) {
			updateRouteLifetime(existingRte.destination, updatedRte.lifetime);
		}

		return false;
		//
		// // update existing route table entry if the new information is
		// "better"
		//
		// // always update in case of an invalid existing seq no
		// if (existingRte.destinationSeqNo != AodvConstants.INVALID_SEQ_NO) {
		// // TODO seq no rollover in these checks
		//
		// // do not update if the new seq no is smaller than the existing one.
		// // This also handles the case when the new seq is invalid (because
		// // in this case it is -1).
		// if (updatedRte.destinationSeqNo < existingRte.destinationSeqNo)
		// return false;
		//
		// // do not update if the seq nos are equal, but the new hop count
		// // is not smaller than the existing one and it is valid
		// if (updatedRte.destinationSeqNo == existingRte.destinationSeqNo) {
		// if (existingRte.isValid() &&
		// updatedRte.hopCount >= existingRte.destinationSeqNo)
		// return false;
		// }
		// }
		//
		// // System.out.println("AODV: UpdateRTE on " + getAodvNode() + " -- "
		// // + updatedRte.toString());
		//
		// putRouteTableEntry(updatedRte);
		// return true;
	}
	
	public void updateRouteLifetime(NetID dest, long lifetime) {
		AodvRouteTableEntry destRte = getRouteTableEntry(dest);
		
		if (destRte == null)
			return;
		
		destRte.lifetime = lifetime;
		putRouteTableEntry(destRte);
	}
	
	public void addPrecursor(NetID dest, NetID precursor) {
		AodvRouteTableEntry destRte = getRouteTableEntry(dest);
		
		if (destRte == null)
			return;
		
		destRte.precursors.add(precursor);
		putRouteTableEntry(destRte);
	}
	
	public void sendRerr(List<NetID> unreachableDestinations, List<Integer> incomingRerrSeqNos) {
		
		// RERR entries: unreachable destinations with non-empty precursor lists
		List<NetID> rerrUnreachableDestinations = new ArrayList<NetID>();
		List<Integer> rerrUnreachableDestinationSeqNos = new ArrayList<Integer>();
		int i = -1;
		
		// RERR receivers: nodes belonging to a precursor list
		Set<NetID> rerrReceivers = new HashSet<NetID>();
		
		for (NetID unreachableDest: unreachableDestinations) {
			AodvRouteTableEntry rte = getRouteTableEntry(unreachableDest);
			i++;
			
			if (rte == null)
				continue;
			
			if (rte.isValid()) {
				if (incomingRerrSeqNos == null) {
					rte.destinationSeqNo++;
					assert rte.destinationSeqNo > 0 : "Seq-No. wrap-around!";
				} else
					rte.destinationSeqNo = incomingRerrSeqNos.get(i);
					
				rte.lifetime = -1;
				updateRouteTableEntry(rte);
				// TODO schedule rte deletion (current time + delete_period)
			}
			
			if (!rte.precursors.isEmpty()) {				
				rerrUnreachableDestinations.add(unreachableDest);
				rerrUnreachableDestinationSeqNos.add(rte.destinationSeqNo);
				rerrReceivers.addAll(rte.precursors);
			}
		}
		
		// determine RERR receiver
		NetID rerrReceiver;
		
		if (rerrReceivers.size() > 1) {
			// many receivers, broadcast
			rerrReceiver = IPv4NetID.LOCAL_BROADCAST;
		} else if (rerrReceivers.size() == 1) {
			// one receiver, unicast
			rerrReceiver = rerrReceivers.iterator().next();
		} else {
			// no receivers, do nothing
			return;
		}
		
		RouteErrorMessage rerr = new RouteErrorMessage();
		rerr.getUnreachableDestinations().addAll(rerrUnreachableDestinations);
		rerr.getUnreachableDestinationSeqNos().addAll(rerrUnreachableDestinationSeqNos);
		
		sendMessage(rerr, rerrReceiver);
	}
	
	public void requestRoute(NetID destination, OperationCallback<NetID> callback) {
		if (routeOperations.containsKey(destination)) {
			RequestRouteOperation op = routeOperations.get(destination);
			if (!op.isFinished()) {
				op.addCallback(callback);
				return;
			}
		}
		
		RequestRouteOperation op = new RequestRouteOperation(this, destination);
		op.addCallback(callback);
		routeOperations.put(destination, op);
		op.scheduleImmediately();
		
		// TODO  A node SHOULD NOT originate more than RREQ_RATELIMIT RREQ messages per second.
	}
	
	public void removeRouteRequest(NetID destination) {
		routeOperations.remove(destination);
	}
	
	/**
	 * Returns true, if at least one known route is valid (and it is not
	 * a self-route).
	 * Used to determine if Hello Messages should be broadcasted.
	 * 
	 * @return true if at least one route is valid
	 */
	public boolean hasValidRoute() {
		for (AodvRouteTableEntry rte: routeTable.values()) {
			if (rte.isValid() && !rte.destination.equals(aodvNode))
				return true;
		}
		
		return false;
	}
	
	public Set<NetID> getValidNeighbors() {
		Set<NetID> neighbors = new HashSet<NetID>();
		
		for (AodvRouteTableEntry rte: routeTable.values()) {
			if (rte.isValid())
				neighbors.add(rte.nextHop);
		}
		
		return neighbors;
	}
	
	public void storeRreqTime() {
		lastRreqTime = Time.getCurrentTime();
	}
	
	public long getLastRreqTime() {
		return lastRreqTime;
	}
	
	public int getSequenceNo() {
		return sequenceNo;
	}

	public void setSequenceNo(int sequenceNo) {
		assert sequenceNo >= this.sequenceNo : "Seq-No decrement?";
		this.sequenceNo = sequenceNo;
	}
	
	public void incrementSequenceNo() {
		// TODO rollover
		sequenceNo++;
		assert sequenceNo > 0 : "Sequence-No wrap-around!";
	}

	public int getRreqId() {
		return rreqId;
	}

	public void incrementRreqId() {
		rreqId++;
		assert rreqId > 0 : "RREQ-Sequence-No wrap-around!";
	}

	public AodvBuffer<RouteRequestMessage> getRreqBuffer() {
		return rreqBuffer;
	}

	public NetID getAodvNode() {
		return aodvNode;
	}

	public void sendMessage(RoutingMessage msg, NetID receiver) {
		aodvRouting.sendRoutingMsg(msg, receiver);
	}
	
	public BroadcastHelloOperation getHelloOp() {
		return helloOp;
	}
}
