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
import de.tud.kom.p2psim.impl.transport.UDPMessage;
import de.tud.kom.p2psim.impl.transport.modular.AbstractTransProtocol;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;

/**
 * Implementation of UDP
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 07.05.2012
 */
public class UserDatagramProtocol extends AbstractTransProtocol {

	public UserDatagramProtocol(SimHost host, SimNetInterface netLayer) {
		super(host, netLayer, NetProtocol.IPv4);
	}

	@Override
	public void send(Message msg, NetID receiverNet, int receiverPort,
			int senderPort, int commId, boolean isReply) {
		UDPMessage udpMsg = new UDPMessage(msg, senderPort, receiverPort,
				commId, isReply);
		if (hasAnalyzer) {
			transportAnalyzerProxy
					.transMsgEvent(udpMsg, getHost(), Reason.SEND);
		}
		// Simulator.getMonitor().transMsgEvent(udpMsg, getHost(), Reason.SEND);
		doSend(udpMsg, receiverNet);
	}

	@Override
	public UDPMessage receive(Message transMsg, TransInfo senderTransInfo) {
		return (UDPMessage) transMsg;
	}

	@Override
	public int getHeaderSize() {
		return 8;
	}

}
