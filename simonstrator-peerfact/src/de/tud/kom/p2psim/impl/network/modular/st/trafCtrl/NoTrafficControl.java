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


package de.tud.kom.p2psim.impl.network.modular.st.trafCtrl;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.modular.st.TrafficControlStrategy;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * No traffic control applied at all, the hosts have virtually unlimited bandwidth.
 * 
 * @author Leo Nobach
 *
 */
public class NoTrafficControl implements TrafficControlStrategy {

	@Override
	public void onSendRequest(ISendContext ctx, NetMessage msg, NetID receiver) {
		ctx.sendSubnet(msg);
	}

	@Override
	public void onReceive(IReceiveContext ctx, NetMessage message) {
		ctx.arrive(message);
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		//No simple/complex types to write back
	}

}
