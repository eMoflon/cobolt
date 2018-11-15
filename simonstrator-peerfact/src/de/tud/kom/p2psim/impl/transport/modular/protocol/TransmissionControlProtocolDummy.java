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

package de.tud.kom.p2psim.impl.transport.modular.protocol;

import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.transport.TCPMessage;
import de.tud.kom.p2psim.impl.transport.modular.AbstractTransProtocol;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;

/**
 * A dummy of TCP without fragmentation and ACKs, used with simple NetLayers.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Aug 6, 2013
 */
public class TransmissionControlProtocolDummy extends AbstractTransProtocol {

	public TransmissionControlProtocolDummy(SimHost host,
			SimNetInterface netLayer) {
		super(host, netLayer, NetProtocol.IPv4);
	}

	@Override
	public void send(Message msg, NetID receiverNet, int receiverPort,
			int senderPort, int commId, boolean isReply) {
		TCPMessage tcpMsg = new TCPMessage(msg, senderPort, receiverPort,
				commId, isReply, commId);
		if (hasAnalyzer) {
			transportAnalyzerProxy
					.transMsgEvent(tcpMsg, getHost(), Reason.SEND);
		}
		doSend(tcpMsg, receiverNet);
	}

	@Override
	public TCPMessage receive(Message transMsg, TransInfo senderTransInfo) {
		return (TCPMessage) transMsg;
	}

	@Override
	public int getHeaderSize() {
		return TransProtocol.TCP.getHeaderSize();
	}

}
