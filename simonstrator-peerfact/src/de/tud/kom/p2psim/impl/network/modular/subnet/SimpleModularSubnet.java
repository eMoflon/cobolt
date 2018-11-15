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

package de.tud.kom.p2psim.impl.network.modular.subnet;

import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.modular.AbstractModularSubnet;
import de.tud.kom.p2psim.impl.network.modular.IStrategies;
import de.tud.kom.p2psim.impl.network.modular.ModularNetLayer;
import de.tud.kom.p2psim.impl.network.modular.ModularNetMessage;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Simple Subnet (former ModularSubnet), default if no Subnet is specified.
 * Subnets allow for routing of messages and different transmission paths like
 * for example WiFi and Ad-Hoc. This subnet implements the "big cloud"
 * assumption and specifies no network topology. Nevertheless, it provides
 * support for different Device types.
 * 
 * @author Leo Nobach (moved into this package and slightly modified by Bjoern
 *         Richerzhagen)
 */
public class SimpleModularSubnet extends AbstractModularSubnet {

	public SimpleModularSubnet(IStrategies strategies) {
		super(strategies);
	}

	@Override
	public void send(NetMessage message) {
		final ModularNetMessage msg = (ModularNetMessage) message;

		// Broadcast
		if (msg.getReceiver().equals(IPv4NetID.LOCAL_BROADCAST)) {
			throw new AssertionError(
					"The SimpleModularSubnet does not support Broadcast! "
							+ "(Target IP must not be LOCAL_BROADCAST)");
		}

		// At this point, the network message may only contain a UDP transport
		// message,
		// this is asserted by the network layer calling this method.

		final ModularNetLayer nlSender = getNetLayer(msg.getSender());
		final ModularNetLayer nlReceiver = getNetLayer(msg.getReceiver());

		// drop or not?
		if (shallBeDropped(msg, nlSender, nlReceiver)) {
			return;
		}

		// receive time?
		long rcvTime = getRcvTime(msg, nlSender, nlReceiver);

		Event.scheduleWithDelay(rcvTime - Time.getCurrentTime(),
				new EventHandler() {

					@Override
					public void eventOccurred(Object se, int type) {
						nlReceiver.receive(msg, nlSender);
					}
				}, null, 0);
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		// no types to write back
	}

	@Override
	protected void netLayerWentOnline(NetLayer net) {
		// nothing to do here.
	}

	@Override
	protected void netLayerWentOffline(NetLayer net) {
		// nothing to do here.
	}

}
