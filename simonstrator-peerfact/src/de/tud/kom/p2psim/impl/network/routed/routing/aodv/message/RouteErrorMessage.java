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

package de.tud.kom.p2psim.impl.network.routed.routing.aodv.message;

import java.util.ArrayList;

import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * AODV Route Error (RERR) Message
 * 
 * @author Christoph Neumann
 */
public class RouteErrorMessage extends AodvMessage {
	
	private ArrayList<NetID> unreachableDestinations = new ArrayList<NetID>();
	private ArrayList<Integer> unreachableDestinationSeqNos = new ArrayList<Integer>();
	
	/*
	 * Creates a new RREP message. Receiver corresponds to the destination
	 * and sender to the originator.
	 */
	public RouteErrorMessage() {
		super();
	}

	@Override
	public long getSize() {
		// Defined in RFC 3561 section 5.3
		int destsSize = 
				unreachableDestinations.get(0).getTransmissionSize() * 
				unreachableDestinations.size();
		int seqNosSize = 4 * unreachableDestinationSeqNos.size();
		return 4 + destsSize + seqNosSize;
	}

	public ArrayList<Integer> getUnreachableDestinationSeqNos() {
		return unreachableDestinationSeqNos;
	}

	public ArrayList<NetID> getUnreachableDestinations() {
		return unreachableDestinations;
	}
}
