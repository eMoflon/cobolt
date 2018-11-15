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

package de.tud.kom.p2psim.impl.transport.modular;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.impl.transport.modular.protocol.TransmissionControlProtocol;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

/**
 * Factory class for the {@link ModularTransLayer} which provides support for
 * different implementations of Transport-Protocols such as TCP and UDP. As
 * there is currently only one Implementation per protocol, we assume this as
 * the default configuration and do not provide setters yet.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 08.05.2012
 */
public class ModularTransLayerFactory implements HostComponentFactory {

	public ModularTransLayerFactory() {
		//
	}

	@Override
	public SimHostComponent createComponent(Host host) {
		ModularTransLayer tl = new ModularTransLayer((SimHost) host);
		return tl;
	}

	/**
	 * Normally, TCP sends packets that fit inside the MTU of the underlying
	 * network to prevent IP-Fragmenting. However, for some simulations this
	 * high level of detail and the resulting number of events might be
	 * unwanted. In such a case, <b>disable IP Fragmenting</b> and set this
	 * scaling factor to a value > 1 to split a TCP stream into larger packets.
	 * 
	 * How to choose this factor depends on the average size of messages your
	 * application sends using TCP. If you send 2MB packets, it is sufficient to
	 * simulate with a scaling factor around 50.
	 * 
	 * <b>Please note</b>: If you do not disable IP Fragmenting when setting a
	 * factor > 1, the drop probability of a TCP-packet will increase
	 * significantly which in turn leads to retransmissions and a lower overall
	 * bandwidth.
	 * 
	 * @param factor
	 */
	public void setTcpScalingFactor(int factor) {
		TransmissionControlProtocol.SCALING_FACTOR = Math.max(1, factor);
	}

	/**
	 * Maximum number of retransmission attempts for a TCP-Packet before the
	 * whole session fails.
	 * 
	 * @param maxRetransmissions
	 */
	public void setTcpMaxRetransmissions(int maxRetransmissions) {
		TransmissionControlProtocol.MAX_RETRANSMISSIONS = Math.max(0,
				maxRetransmissions);
	}
	/**
	 * Flag for using real TCP acknowledgment. Every second message will be
	 * acknowledged with a real Message, otherwise all messages will be directly
	 * acknowledged (directly call of the opposite layer)
	 */
	public void setUseRealTcpAck(boolean useRealTcpAck) {
		TransmissionControlProtocol.USE_REAL_TCP_ACK = useRealTcpAck;
	}

}
