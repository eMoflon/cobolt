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

package de.tud.kom.p2psim.impl.network.routed.routing.aodv;

import java.util.ArrayList;
import java.util.List;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.network.routing.RoutingMessage;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.routed.routing.AbstractRoutingAlgorithm;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.message.RouteErrorMessage;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.message.RouteReplyMessage;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.message.RouteRequestMessage;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.state.AodvRouteTableEntry;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.state.AodvState;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.operation.Operation;
import de.tudarmstadt.maki.simonstrator.api.operation.OperationCallback;

/**
 * Implementation of the reactive
 * Ad hoc On-Demand Distance Vector (AODV) Routing
 * based on RFC 3561.
 * 
 * @author Christoph Neumann
 */
public class AodvRouting extends AbstractRoutingAlgorithm {

	private AodvState state;

	private PhyType phy;

	public AodvRouting(SimHost host, PhyType phy) {
		super(host, RoutingType.REACTIVE, phy.getNetInterfaceName());
		this.phy = phy;
	}

	@Override
	public PhyType getPhyType() {
		return phy;
	}

	@Override
	public void initialize() {
		state = new AodvState(getHost(), getNetID(), this);
	}

	@Override
	public void handleMessage(NetMessage msg, PhyType phy, MacAddress lastHop) {
		if (msg.getPayload() instanceof RoutingMessage) {
			/*
			 * Routing control-Message
			 */
			RoutingMessage rMsg = (RoutingMessage) msg.getPayload();
			routingMessageArrived(rMsg, msg.getSender());
		} else {
			/*
			 * Data-Message
			 */
			if (msg.getReceiver().equals(getNetID())
					|| msg.getReceiver().equals(IPv4NetID.LOCAL_BROADCAST)) {
				// this host is the target
				notifyNetLayer(msg);
			} else {
				// forward the message
				nextHop(msg);
			}
		}
	}


	@Override
	public void route(final NetMessage msg) {

		if (msg.getReceiver().equals(IPv4NetID.LOCAL_BROADCAST)) {
			/*
			 * Do NOT route a broadcast...
			 */
			forwardNetMessage(msg, msg.getReceiver(), phy);
		} else {
			state.requestRoute(msg.getReceiver(),
					new OperationCallback<NetID>() {
						@Override
						public void calledOperationFailed(Operation<NetID> op) {
							// unable to find a route -> drop the message
							messageDropped(DropReason.NO_PATH_FOUND, msg);
						}

						@Override
						public void calledOperationSucceeded(Operation<NetID> op) {
							forwardNetMessage(msg, op.getResult());
						}
			});
		}
	}

	/**
	 * Just making this public because otherwise there is a warning
	 * 
	 * @param msg
	 * @param nextHop
	 */
	public void forwardNetMessage(NetMessage msg, NetID nextHop) {
		super.forwardNetMessage(msg, nextHop, phy);
	}
	
	/**
	 * Public version of sendMessage() to allow usage by operations.
	 * @param msg
	 * @param sender
	 * @param receiver 
	 */
	public void sendRoutingMsg(RoutingMessage msg, NetID receiver) {
		super.sendRoutingMsg(msg, receiver, phy);
	}

	/**
	 * Handle routing Messages
	 * 
	 * @param msg
	 * @param sender
	 */
	private void routingMessageArrived(RoutingMessage msg, NetID sender) {

		// RREQ messages
		if (msg instanceof RouteRequestMessage) {
			handleRouteRequestMessage((RouteRequestMessage) msg, sender);
			return;
		}
		
		// RREP (and Hello) messages
		if (msg instanceof RouteReplyMessage) {
			handleRouteReplyMessage((RouteReplyMessage) msg, sender);
			return;
		}
		
		// RERR messages
		if (msg instanceof RouteErrorMessage) {
			handleRouteErrorMessage((RouteErrorMessage) msg, sender);
			return;
		}
	}

	protected void handleRouteRequestMessage(RouteRequestMessage rreq,
			NetID sender) {

		
		// Create/update route to previous hop w/o valid seq no.
		// The previous hop is the sender of the RREQ message.

		AodvRouteTableEntry previousHopRte = new AodvRouteTableEntry();
		previousHopRte.destination = sender;
		previousHopRte.destinationSeqNo = AodvConstants.INVALID_SEQ_NO;
		previousHopRte.nextHop = sender;
		previousHopRte.hopCount = 1;
		previousHopRte.lifetime = Time.getCurrentTime() + AodvConstants.ACTIVE_ROUTE_TIMEOUT;
		state.updateRouteTableEntry(previousHopRte);

		// determine whether we have received a RREQ with the same
		// Originator IP Address and RREQ ID within at least the last
		// PATH_DISCOVERY_TIME.
		if (state.getRreqBuffer().contains(rreq)) {
			// System.out.println(state.getAodvNode() + " discarded "
			// + rreq.toString());
			return;	// silently discard the newly received RREQ
		}
		
		// add rreq to buffer
		state.getRreqBuffer().put(rreq, AodvConstants.PATH_DISCOVERY_TIME);

		// increment hop count
		assert rreq.getHopCount() > -2 : "Byte overflow!";
		/*
		 * FIXME FIXED (bjoern) setHopCount updates the reference (ie. on a
		 * broadcast all other receivers will suddenly see a much higher hop
		 * count...) leads to broken links
		 */
		// rreq.setHopCount((byte) (rreq.getHopCount() + 1));

		// create/update reverse route to the rreq's originator
		AodvRouteTableEntry reverseRte = new AodvRouteTableEntry();
		reverseRte.destination = rreq.getOriginator();
		reverseRte.destinationSeqNo = rreq.getOriginatorSeqNo();
		reverseRte.nextHop = sender;
		reverseRte.hopCount = (byte) (rreq.getHopCount() + 1); // FIXED

		// lifetime = max(ExistingLifetime, MinimalLifetime)
		// MinimalLifetime = (current time + 2*NET_TRAVERSAL_TIME - 2*HopCount*NODE_TRAVERSAL_TIME)
		AodvRouteTableEntry existingReverseRte = state.getRouteTableEntry(rreq
				.getOriginator());

		long existingLifetime = 0;

		if (existingReverseRte != null)
			existingLifetime = existingReverseRte.lifetime;

		long minimalLifetime = Time.getCurrentTime() + 2
				* AodvConstants.NET_TRAVERSAL_TIME - 2
				* (rreq.getHopCount() + 1) * AodvConstants.NODE_TRAVERSAL_TIME;
		
		reverseRte.lifetime = Math.max(existingLifetime, minimalLifetime); 

		state.updateRouteTableEntry(reverseRte);

		// determine whether to send a RREP or not
		if (shouldSendRrep(rreq.getDestination(), rreq.getDestinationSeqNo(),
				sender)) {

			byte rrepHopCount;
			long rrepLifetime;
			int destSequenceNo;
			
			if (rreq.getDestination().equals(state.getAodvNode())) {
				// 6.6.1
				// Section 6.1:
				// Immediately before a destination node originates a RREP in
				// response to a RREQ, it MUST update its own sequence number to the
				// maximum of its current sequence number and the destination
				// sequence number in the RREQ packet.
				//
				// vs.
				//
				// Section 6.6.1:
				// If the generating node is the destination itself, it MUST increment
				// its own sequence number by one if the sequence number in the RREQ
				// packet is equal to that incremented value.  Otherwise, the
				// destination does not change its sequence number before generating the
				// RREP message.
				//
				// Isn't that nice...
				// The AODV-UU and UoB JAdhoc implementations do the following:

				if (state.getSequenceNo() < rreq.getDestinationSeqNo())
					state.setSequenceNo(rreq.getDestinationSeqNo());
				else if (state.getSequenceNo() == rreq.getDestinationSeqNo())
					state.incrementSequenceNo();

				rrepHopCount = 0;
				rrepLifetime = AodvConstants.MY_ROUTE_TIMEOUT;
				destSequenceNo = state.getSequenceNo();
			
			} else {	// 6.6.2 (intermediate node)

				AodvRouteTableEntry destinationRte = state
						.getRouteTableEntry(rreq.getDestination());
				
				rrepHopCount = destinationRte.hopCount;
				rrepLifetime = destinationRte.lifetime - Time.getCurrentTime();
				
				/*
				 * FIXME BUGFIX (bjoern) - If the node generating the RREP is
				 * not the destination node, but instead is an intermediate hop
				 * along the path from the originator to the destination, it
				 * copies its known sequence number for the destination into the
				 * Destination Sequence Number field in the RREP message. NOT
				 * its OWN sequence no.
				 */
				destSequenceNo = destinationRte.destinationSeqNo;

				// update precursor list (forward route)
				state.addPrecursor(rreq.getDestination(), sender);
				
				// update precursor list (reverse route)
				state.addPrecursor(rreq.getOriginator(), destinationRte.nextHop);
			}
			
			RouteReplyMessage rrep = new RouteReplyMessage();
			rrep.setDestination(rreq.getDestination());
			rrep.setDestinationSeqNo(destSequenceNo); // BUFGFIX
			rrep.setOriginator(rreq.getOriginator());
			rrep.setHopCount(rrepHopCount);
			rrep.setLifetime(rrepLifetime);
			
			// the rfc says the originator seq no should be included,
			// but there is no such field in RREP messages...
			
			sendRoutingMsg(rrep, sender);
			return;
		}
		
		// do not send a RREP message,
		// but update and forward the RREQ if the TTL allows it
		
		if (rreq.getTtl() <= 1)
			return;
		
		// forwarding requires a new message (new sender and receiver)
		RouteRequestMessage forwardRreq = new RouteRequestMessage();
		
		// copy existing rreq values
		forwardRreq.setRreqId(rreq.getRreqId());
		forwardRreq.setDestination(rreq.getDestination());
		forwardRreq.setOriginator(rreq.getOriginator());
		forwardRreq.setOriginatorSeqNo(rreq.getOriginatorSeqNo());
				
		// update rreq ttl and hop count
		assert rreq.getTtl() > 1 : "TTL < 2";
		forwardRreq.setTtl((byte) (rreq.getTtl() - 1));
		
		// The RFC mentions the hop count increment again, even though
		// it has been incremented before.
		// Thus, we do not increment a second time here.
		/*
		 * FIXME FIXED (bjoern) first increment was wrong, as desribed in the
		 * fixme above
		 */
		forwardRreq.setHopCount((byte) (rreq.getHopCount() + 1));
		
		// update destination seq no
		AodvRouteTableEntry destRte = state.getRouteTableEntry(rreq
				.getDestination());
		
		int knownDestSeqNo;

		if (destRte == null)
			knownDestSeqNo = AodvConstants.INVALID_SEQ_NO;
		else
			knownDestSeqNo = destRte.destinationSeqNo;
		
		int rreqDestSeqNo = Math.max(rreq.getDestinationSeqNo(), knownDestSeqNo);
		forwardRreq.setDestinationSeqNo(rreqDestSeqNo);
		
		// forward updated rreq
		state.storeRreqTime();
		sendRoutingMsg(forwardRreq, IPv4NetID.LOCAL_BROADCAST);
	}

	/**
	 * Determines if a node should send a RREP message in response to a
	 * RREQ message. See section 6.6 of the RFC.
	 * @param currentNode the node that received the RREQ
	 * @param destination the destination in the RREQ
	 * @param destSeqNo the destination sequence number in the RREQ
	 * @return true if RREP should be sent, false otherwise
	 */
	private boolean shouldSendRrep(NetID destination, int destSeqNo,
			NetID sender) {
		if (destination.equals(state.getAodvNode()))
			return true;
		else
			return false;

		// AodvRouteTableEntry destinationRte = state
		// .getRouteTableEntry(destination);
		//
		// if (destinationRte == null)
		// return false;
		//
		// if (!destinationRte.isValid())
		// return false;
		//
		// if (destinationRte.destinationSeqNo == AodvConstants.INVALID_SEQ_NO)
		// return false;
		//
		// if (destinationRte.destinationSeqNo < 1 + destSeqNo)
		// return false;
		//
		// if (destinationRte.nextHop.equals(sender)) {
		// System.err
		// .println("Detected a LOOP - How could it even come this far?");
		// return false; // LOOP!
		// }
		//
		// return true;
	}
	
	protected void handleRouteReplyMessage(final RouteReplyMessage rrep,
			NetID sender) {
		
		// Hello messages may be identified by a TTL of 1 and the fact
		// that they are broadcasted. Since we ignore the TTL when sending
		// normal RREPs, we can use it directly to identify Hello messages.
		if (rrep.getTtl() == 1) {
			handleHelloMessage(rrep);
			return;
		}
		

		// Create/update route to previous hop w/o valid seq no.
		// The previous hop is the sender of the RREP message.
		AodvRouteTableEntry previousHopRte = new AodvRouteTableEntry();
		previousHopRte.destination = sender;
		previousHopRte.destinationSeqNo = AodvConstants.INVALID_SEQ_NO;
		previousHopRte.nextHop = sender;
		previousHopRte.hopCount = 1;
		previousHopRte.lifetime = Time.getCurrentTime() + AodvConstants.ACTIVE_ROUTE_TIMEOUT;
		boolean upd = state.updateRouteTableEntry(previousHopRte);
		
		// increment hop count
		rrep.setHopCount((byte) (rrep.getHopCount() + 1));
		
		assert rrep.getHopCount() > 0 : "RREP-Hopcount wrap-around - loop?";

		// create/update forward route to the destination
		AodvRouteTableEntry destinationRte = new AodvRouteTableEntry();
		destinationRte.destination = rrep.getDestination();
		destinationRte.destinationSeqNo = rrep.getDestinationSeqNo();
		destinationRte.nextHop = sender;
		destinationRte.hopCount = rrep.getHopCount();
		destinationRte.lifetime = Time.getCurrentTime() + rrep.getLifetime();
		
		boolean destinationRteUpdated = state
				.updateRouteTableEntry(destinationRte);
		
		// If the current node is not the node indicated by the Originator IP
		// Address in the RREP message AND a forward route has been created or
		// updated as described above, the node consults its route table entry
		// for the originating node to determine the next hop for the RREP
		// packet, and then forwards the RREP towards the originator using the
		// information in that route table entry.
		
		if (rrep.getOriginator().equals(state.getAodvNode())) {
			return;
		}
		
		if (!destinationRteUpdated) {
			return;
		}
		
		AodvRouteTableEntry originatorRte = state.getRouteTableEntry(rrep
				.getOriginator());

		if (originatorRte == null)
			return;
		
		// update the route's lifetime
		long originatorRteLifetime = Math
				.max(
				originatorRte.lifetime,
				Time.getCurrentTime() + AodvConstants.ACTIVE_ROUTE_TIMEOUT);
		
		state.updateRouteLifetime(rrep.getOriginator(), originatorRteLifetime);

		
		// The precursor list for the corresponding destination node 
		// is updated by adding to it the next hop node 
		// to which the RREP is forwarded. 
		state.addPrecursor(rrep.getDestination(), originatorRte.nextHop);
		
		// The precursor list for the next hop towards the destination 
		// is updated to contain the next hop towards the source.
		state.addPrecursor(sender, originatorRte.nextHop);
		
		// forward RREP = create a new RREP
		RouteReplyMessage forwardRrep = new RouteReplyMessage();
		
		forwardRrep.setDestination(rrep.getDestination());
		forwardRrep.setDestinationSeqNo(rrep.getDestinationSeqNo());
		forwardRrep.setOriginator(rrep.getOriginator());
		forwardRrep.setHopCount(rrep.getHopCount());
		forwardRrep.setLifetime(rrep.getLifetime());
		
		sendRoutingMsg(forwardRrep, originatorRte.nextHop);
	}
	
	/**
	 * Handles incoming Hello message. Hello messages are RREP message
	 * with a TTL of 1.
	 * @param nlAct
	 * @param rrep 
	 */
	protected void handleHelloMessage(final RouteReplyMessage hello) {
		
		// The normal RREP handling could handle Hello messages just fine,
		// but the RFC wants some special lifetime handling for Hello messages.
		
		AodvRouteTableEntry existingNeighborRte = state
				.getRouteTableEntry(hello.getDestination());
		
		long lifetime;
		
		if (existingNeighborRte == null) {
			lifetime = Time.getCurrentTime() + AodvConstants.ACTIVE_ROUTE_TIMEOUT;
		} else {
			lifetime = Math.max(
					existingNeighborRte.lifetime,
					Time.getCurrentTime() + hello.getLifetime());
		}
		
		AodvRouteTableEntry neighborRte = new AodvRouteTableEntry();
		neighborRte.destination = hello.getDestination();
		neighborRte.destinationSeqNo = hello.getDestinationSeqNo();
		neighborRte.nextHop = hello.getDestination();
		neighborRte.lifetime = lifetime;
		
		// They forgot to increment the hop count in the RFC.
		// The hop count in a Hello message is 0.
		// In the routing table it obviously should be 1, 
		// since it is a neighboring node.
		// Again, the normal RREP handling would account for that.
		neighborRte.hopCount = 1;
		
		state.updateRouteTableEntry(neighborRte);
	}
	
	/* TODO
	 *    A node MAY determine connectivity by listening for packets from its
   set of neighbors.  If, within the past DELETE_PERIOD, it has received
   a Hello message from a neighbor, and then for that neighbor does not
   receive any packets (Hello messages or otherwise) for more than
   ALLOWED_HELLO_LOSS * HELLO_INTERVAL milliseconds, the node SHOULD
   assume that the link to this neighbor is currently lost.  When this
   happens, the node SHOULD proceed as in Section 6.11.
	 */
	
	protected void handleRouteErrorMessage(RouteErrorMessage rerr, NetID sender) {
		
		// build list of relevant unreachable destinations
		List<NetID> relevantUnreachableDestinations = new ArrayList<NetID>();
		List<Integer> relevantSeqNos = new ArrayList<Integer>();
		int i = -1;
		
		for (NetID unreachableDest: rerr.getUnreachableDestinations()) {
			AodvRouteTableEntry rte = state.getRouteTableEntry(unreachableDest);
			i++;
			
			if (rte == null)
				continue;
			
			// check if the sender of the RERR is the next hop
			// to the unreachable destination
			if (rte.nextHop.equals(sender)) {
				relevantUnreachableDestinations.add(unreachableDest);
				relevantSeqNos.add(rerr.getUnreachableDestinationSeqNos().get(i));
			}
		}
		
		state.sendRerr(relevantUnreachableDestinations, relevantSeqNos);
	}

	private void nextHop(NetMessage msg) {
		// check if a route to the destination is known and valid
		NetID destination = msg.getReceiver();

		AodvRouteTableEntry rte = state.getRouteTableEntry(destination);

		if (rte != null && rte.isValid()) {
			state.updateRouteLifetime(destination, Time.getCurrentTime()
					+ AodvConstants.ACTIVE_ROUTE_TIMEOUT);
			forwardNetMessage(msg, rte.nextHop, phy);
			return;
		}
		
		if (rte != null && msg.getReceiver().equals(rte.destination)) {
			System.out.println("lifetime " + rte.lifetime + " time "
					+ Time.getCurrentTime());
		}

		// no valid route found: send RERR (6.11)
		List<NetID> unreachableDestinations = new ArrayList<NetID>();
		unreachableDestinations.add(destination);

		state.sendRerr(unreachableDestinations, null);

		// TODO update deletion schedule

		// drop the message
		if (rte == null) {
			messageDropped(DropReason.PATH_BROKEN, msg);
		} else {
			/*
			 * TODO DEBUG this
			 */
			rte.isValid();
			if (msg.getReceiver().equals(rte.destination)) {
				System.err.println("last hop");
			}

			messageDropped(DropReason.PATH_OUTDATED, msg);
		}
	}
	

	@Override
	public void start() {
		// start Hello broadcasting
		state.getHelloOp().start();
	}

	@Override
	public void stop() {
		// stop Hello broadcasting
		// is there anything else to do here?
		state.getHelloOp().stop();
	}
}
