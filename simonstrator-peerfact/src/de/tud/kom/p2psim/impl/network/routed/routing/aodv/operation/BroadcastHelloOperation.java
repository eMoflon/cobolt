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

package de.tud.kom.p2psim.impl.network.routed.routing.aodv.operation;

import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.AodvConstants;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.message.RouteReplyMessage;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.state.AodvState;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.operation.PeriodicOperation;

/**
 * Operation for the periodical broadcast of AODV's Hello messages.
 * @author Christoph Neumann
 */
public class BroadcastHelloOperation extends
		PeriodicOperation<AodvState, Object> {

	AodvState state;
	
	public BroadcastHelloOperation(AodvState aodvState) {
		super(aodvState, null, AodvConstants.HELLO_INTERVAL);
		state = aodvState;
	}

	@Override
	protected void executeOnce() {
		if (Time.getCurrentTime() - state.getLastRreqTime() < AodvConstants.HELLO_INTERVAL)
			return;
		
		RouteReplyMessage hello = new RouteReplyMessage();
		
		hello.setTtl((byte) 1);
		hello.setOriginator(state.getAodvNode());
		hello.setDestination(state.getAodvNode());
		hello.setDestinationSeqNo(state.getSequenceNo());
		hello.setHopCount((byte) 0);
		hello.setLifetime(AodvConstants.ALLOWED_HELLO_LOSS * AodvConstants.HELLO_INTERVAL);
		state.sendMessage(hello, IPv4NetID.LOCAL_BROADCAST);
	}

	@Override
	public Object getResult() {
		return null;
	}
	
}
