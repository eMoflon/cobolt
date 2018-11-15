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

import java.util.Random;

import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy;
import de.tud.kom.p2psim.impl.network.simple.SimpleSubnet;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * This model is abstracting the details of the four lower OSI layers (UDP and
 * TCP) from the end-to-end connections between peers although important network
 * characteristics, like the geographical distance between peers, the processing
 * delay of intermediate systems, signal propagation, congestions,
 * retransmission and packet loss are incorporated into it. The message delay is
 * calculated using the following formula:
 * 
 * Message delay = f * (df + dist/v)
 * 
 * where dist - describes the geographical distance between the start and the
 * end point of the transmission, df - represents the processing delay of the
 * intermediate systems, v - stands for the speed of the signal propagation
 * through the transmission medium, and f - is a variable part which
 * encapsulates the retransmission, congestion.
 * 
 * @author Sebastian Kaune
 * 
 */
public class SimpleLatencyModel implements LatencyStrategy {

	private Random rnd = Randoms.getRandom(SimpleLatencyModel.class);

	/**
	 * Speed in kilometer per second
	 */
	private final int signalSpeed = 100000;

	/**
	 * Earth circumference in kilometres
	 */
	private final int earth_circumference = 40000;

	private final double relSignalSpeed;

	/**
	 * Constructor
	 * 
	 */
	public SimpleLatencyModel() {
		relSignalSpeed = signalSpeed * (SimpleSubnet.SUBNET_WIDTH / earth_circumference);
	}

	/**
	 * Gets the distance.
	 *
	 * @param sender the sender
	 * @param receiver the receiver
	 * @return the distance
	 */
	public double getDistance(NetLayer sender, NetLayer receiver) {
		Location ps = sender.getNetPosition();
		Location pr = receiver.getNetPosition();
		return ps.distanceTo(pr);
	}

	/**
	 * Calc static delay.
	 *
	 * @param receiver the receiver
	 * @param distance the distance
	 * @return the double
	 */
	public double calcStaticDelay(NetLayer receiver, double distance) {
		int df = Math.abs(receiver.hashCode() % 31);
		return (df + (distance / relSignalSpeed) * 1000);
	}

	@Override
	public long getMessagePropagationDelay(NetMessage msg, AbstractNetLayer nlSender, AbstractNetLayer nlReceiver, NetMeasurementDB db) {
		
		double distance = getDistance( nlSender, nlReceiver);
		double staticDelay = Time.MILLISECOND
				* this.calcStaticDelay(nlReceiver, distance);
		
		int f = (rnd.nextInt(10) + 1);
		long latency = Math.round(f * staticDelay * 0.1);
		
		return latency;
		
	}
	
	@Override
	public void writeBackToXML(BackWriter bw) {
		// None.
	}

}