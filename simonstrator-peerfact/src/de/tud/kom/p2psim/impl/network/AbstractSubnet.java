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



package de.tud.kom.p2psim.impl.network;

import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;
import de.tudarmstadt.maki.simonstrator.api.Message;

/**
 * The Subnet models the intrinsic complexity of the internet as a "big cloud"
 * which appears to be transparent for the end-systems (hosts). That is, when
 * sending a message using the tcp/udp protocol, the message is given from the
 * sending host to the the subnet and the subnet manages the calculation of
 * transmission times, establishes a tcp connection (if necessary) between the
 * sender and receiver, models the packet loss and jitter and schedules the
 * appropriate events at the simulation framework. It also triggers the arrival
 * of a message at the appropriate receiver by using the
 * de.tud.kom.p2psim.impl.network.AbstractNetLayer#receive(NetMessage) method.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 11/29/2007
 */
public abstract class AbstractSubnet {

	/**
	 * Used MeasurementDB for Latency, Jitter, etc.
	 */
	private NetMeasurementDB db;
	
	private int lastCommId = 0;

	/**
	 * This method passes a given NetMessage from the sending host to the
	 * subnet.
	 * 
	 * @param msg
	 *            the message to be send
	 */
	public abstract void send(NetMessage msg);

	/**
	 * Registers a new NetLayer to the subnet
	 * 
	 * @param net
	 *            the NetLayet to be registered
	 */
	public abstract void registerNetLayer(NetLayer net);

	/**
	 * 
	 * @param message
	 * @return
	 */
	public int determineTransMsgNumber(Message message) {
		// Iterate over the nested messages until AbstractTransMessage is
		// reached
		while (!(message instanceof AbstractTransMessage)) {
			message = message.getPayload();
		}

		// Depending on the current commId of transMsg return a new id or the
		// old one
		AbstractTransMessage transMsg = (AbstractTransMessage) message;
		if (transMsg.getCommId() == -1) {
			return lastCommId++;
		} else {
			return transMsg.getCommId();
		}
	}
	

	/**
	 * Set Measurement-DB
	 * 
	 * @param db
	 */
	public void setDB(NetMeasurementDB db) {
		this.db = db;
	}
	
	/**
	 * Get Measurement-DB
	 * 
	 * @return
	 */
	public NetMeasurementDB getDB() {
		return db;
	}

}
