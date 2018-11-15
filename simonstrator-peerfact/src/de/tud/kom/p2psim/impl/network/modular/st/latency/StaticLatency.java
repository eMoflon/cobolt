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


package de.tud.kom.p2psim.impl.network.modular.st.latency;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Returns a fixed propagation delay.
 * 
 * Parameters: propagationDelay(long, simulation time units)
 * 
 * @author Leo Nobach
 *
 */
public class StaticLatency implements LatencyStrategy {

	private long propagationDelay = 10 * Time.MILLISECOND; // 10 ms

	@Override
	public long getMessagePropagationDelay(NetMessage msg,
			AbstractNetLayer nlSender, AbstractNetLayer nlReceiver,
			NetMeasurementDB db) {
		return propagationDelay;
	}
	
	public void setPropagationDelay(long delay) {
		this.propagationDelay = delay;
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		bw.writeTime("propagationDelay", propagationDelay);
	}

}
