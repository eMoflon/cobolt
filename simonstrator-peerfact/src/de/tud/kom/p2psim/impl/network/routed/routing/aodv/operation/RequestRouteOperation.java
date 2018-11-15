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

package de.tud.kom.p2psim.impl.network.routed.routing.aodv.operation;

import java.util.ArrayList;
import java.util.List;

import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.AodvConstants;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.message.RouteRequestMessage;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.state.AodvRouteTableEntry;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.state.AodvState;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.operation.AbstractOperation;
import de.tudarmstadt.maki.simonstrator.api.operation.OperationCallback;

/**
 * Operation to discover a route in AODV.
 * If there is no valid route in the routing table, it
 * broadcasts RREQ messages with expanding ring search 
 * and binary exponential backoff.
 * The operation is notified when the routing table is updated.
 * 
 * @author Christoph Neumann
 */
public class RequestRouteOperation extends AbstractOperation<AodvState, NetID> {

	
	List<OperationCallback<NetID>> callbacks = new ArrayList<OperationCallback<NetID>>();
	AodvState state;
	NetID destination;
	NetID result;
	
	private byte ttl, retriesMaxTtl;
	long wait;

	private int _calledCounter = 0;

	public RequestRouteOperation(AodvState currentState, NetID dest) {
		super(currentState);
		state = currentState;
		destination = dest;
		
		// log.info("Starting RREQ operation at " + state.getAodvNode() +
		// " (dest: " + dest + ")");
	}
	
	@Override
	protected void operationFinished(boolean success) {
		super.operationFinished(success);
		
		/*
		 * FIXME BUGFIX: do not trigger this twice
		 */
		_calledCounter++;
		assert _calledCounter == 1 : "Op finished multiple times! "
				+ _calledCounter;

		state.removeRouteRequest(destination);
		
		for (OperationCallback<NetID> cb: callbacks) {
			if (success)
				cb.calledOperationSucceeded(this);
			else
				cb.calledOperationFailed(this);
		}
	}

	@Override
	protected void execute() {
		if (isFinished()) {
			/*
			 * BUGFIX: due to the fact that the same op is scheduled twice, it
			 * might also be called twice... and if the first lookup was already
			 * successful, you will end up sending the same message twice
			 */
			return;
		}
		AodvRouteTableEntry rte = state.getRouteTableEntry(destination);
		
		if (rte != null && rte.isValid()) {
			result = rte.nextHop;
			operationFinished(true);
			return;
		}
		
		// expanding ring search and binary exponential backoff
		
		if (ttl == 0) {		// first RREQ
			if (rte == null)
				ttl = AodvConstants.TTL_START;
			else
				ttl = (byte) (rte.hopCount + AodvConstants.TTL_INCREMENT);
			
		} else {	// increment the TTL for subsequent RREQs
			ttl += AodvConstants.TTL_INCREMENT;
		}
		
		if (ttl <= AodvConstants.TTL_THRESHOLD) {
			// wait := RING_TRAVERSAL_TIME, as long as
			// the TTL is below the TTL_THRESHOLD.
			// RING_TRAVERSAL_TIME is not a constant but has to be calculated
			// based on the TTL.
			wait = 2 * AodvConstants.NODE_TRAVERSAL_TIME * (ttl + AodvConstants.TIMEOUT_BUFFER);
			
		} else {	// threshold exceeded
			// Use retriesMaxTtl to determine if it is the first
			// time the threshold has been exceeded.
			// In this case the waiting time has to be initialized
			// to NET_TRAVERSAL_TIME. Subsequent calls then perform
			// the binary exponential backoff.
			if (retriesMaxTtl == 0)
				wait = AodvConstants.NET_TRAVERSAL_TIME;
			else
				wait *= 2;
			
			// Once the threshold has been exceeded, the TTL stays the same.
			ttl = AodvConstants.NET_DIAMETER;
			
			retriesMaxTtl++;
			
			if (retriesMaxTtl > AodvConstants.RREQ_RETRIES) {
				System.err.println("Failed to find a path... tried "
						+ retriesMaxTtl + " times, last with a TTL of " + ttl
						+ " and a waitingTime of " + wait);
				operationFinished(false);
				return;
			}
		}
		
		broadcastRreq(ttl);
		scheduleWithDelay(wait);
	}
	
	private void broadcastRreq(byte ttl) {
		AodvRouteTableEntry rte = state.getRouteTableEntry(destination);
		
		if (rte != null && rte.isValid()) {
			// if we know about a valid route, this method should not
			// have been called in the first place
			Monitor.log(RequestRouteOperation.class, Level.WARN,
					"Tried to broadcast a RREQ even though I know about a valid route.");
			return;
		}
	
		RouteRequestMessage rreq = new RouteRequestMessage();

		rreq.setOriginator(state.getAodvNode());

		state.incrementSequenceNo();
		rreq.setOriginatorSeqNo(state.getSequenceNo());

		rreq.setDestination(destination);

		if (rte != null)
			rreq.setDestinationSeqNo(rte.destinationSeqNo);
		else
			rreq.setDestinationSeqNo(AodvConstants.INVALID_SEQ_NO);

		rreq.setHopCount((byte) 0);
		rreq.setTtl(ttl);

		state.incrementRreqId();
		rreq.setRreqId(state.getRreqId());

		// buffer rreq (id and originator (= self))
		state.getRreqBuffer().put(rreq, AodvConstants.PATH_DISCOVERY_TIME);
		state.storeRreqTime();
		state.sendMessage(rreq, IPv4NetID.LOCAL_BROADCAST);
	}

	@Override
	public NetID getResult() {
		// We assume that the route is used if getResult() is called.
		// Thus, the route's lifetime has to be updated.	
		if (result != null) {
			state.updateRouteLifetime(
					destination, 
					Time.getCurrentTime() + AodvConstants.ACTIVE_ROUTE_TIMEOUT);
		}
		
		return result;
	}
	
	public void notifyAboutRouteUpdate() {
		AodvRouteTableEntry rte = state.getRouteTableEntry(destination);
		
		if (rte == null)
			return;
		
		if (!rte.isValid())
			return;
		
		result = rte.nextHop;
		operationFinished(true);
	}
	
	public void addCallback(OperationCallback<NetID> cb) {
		assert !this.isFinished() : "Op already finished...";
		callbacks.add(cb);
	}

}
