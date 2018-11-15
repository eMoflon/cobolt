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


package de.tud.kom.p2psim.impl.network.modular.st.packetSizing;

import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.impl.network.modular.st.PacketSizingStrategy;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * Returns a packet size that adds the 20-byte IPv4 header to the payload length of the network message.
 * @author Leo Nobach
 *
 */
public class IPv4Header implements PacketSizingStrategy {

	static final int IPV4_HDR_SZ = 20;
	
	@Override
	public long getPacketSize(Message payload, NetID receiver, NetID sender,
			NetProtocol netProtocol, int noOfFragments) {
		return payload.getSize() + noOfFragments*IPV4_HDR_SZ;	//for every fragment a header is needed
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		//No simple/complex types to write back
	}

}
