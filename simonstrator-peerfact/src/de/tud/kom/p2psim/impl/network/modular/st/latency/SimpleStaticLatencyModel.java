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
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

public class SimpleStaticLatencyModel implements LatencyStrategy {
	
	protected long propagationDelay = 10; // 10 ms

	public SimpleStaticLatencyModel(long staticLatency) {
		this.setLatency(staticLatency);
	}

	/**
	 * Sets the static latency which is expected in millseconds. That is, if
	 * <code>staticLatency</code> is set to 10, the simulator will translate it
	 * into simulation units as follows: staticLatency *
	 * Simulator.MILLISECOND_UNIT.
	 * 
	 * @param staticLatency
	 *            the static latency in milliseconds.
	 */
	public void setLatency(long staticLatency) {
		this.propagationDelay = staticLatency;
	}
	
	/**
	 * Gets the distance.
	 *
	 * @param nlSender the nl sender
	 * @param nlReceiver the nl receiver
	 * @return the distance
	 */
	protected double getDistance(AbstractNetLayer nlSender, AbstractNetLayer nlReceiver){
			
		Location ps = nlSender.getNetPosition();
		Location pr = nlReceiver.getNetPosition();
		
		return ps.distanceTo(pr);
		
	}
	

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy#getMessagePropagationDelay(de.tud.kom.p2psim.api.network.NetMessage, de.tud.kom.p2psim.impl.network.AbstractNetLayer, de.tud.kom.p2psim.impl.network.AbstractNetLayer, de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB)
	 */
	@Override
	public long getMessagePropagationDelay(NetMessage msg, AbstractNetLayer nlSender, AbstractNetLayer nlReceiver,	NetMeasurementDB db) {
		
		return (long) (getDistance(nlSender, nlReceiver) * propagationDelay * Time.MILLISECOND);
		
	}
	

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.util.BackToXMLWritable#writeBackToXML(de.tud.kom.p2psim.impl.util.BackToXMLWritable.BackWriter)
	 */
	@Override
	public void writeBackToXML(BackWriter bw) {
		bw.writeTime("propagationDelay", propagationDelay);
	}

}
