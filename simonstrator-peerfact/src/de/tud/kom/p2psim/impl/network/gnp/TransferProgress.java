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


package de.tud.kom.p2psim.impl.network.gnp;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tudarmstadt.maki.simonstrator.api.Time;

public class TransferProgress {

	private NetMessage message;

	private double bandwidth; // in Simulator Time Units;

	private double remainingBytes;

	private long scheduledAt;

	public boolean firstSchedule = true;

	public boolean obsolete = false;

	public TransferProgress(NetMessage msg, double bandwidth,
			double remainingBytes, long scheduledAt) {
		this.message = msg;
		this.bandwidth = bandwidth / Time.SECOND;
		this.remainingBytes = remainingBytes;
		this.scheduledAt = scheduledAt;
	}

	public NetMessage getMessage() {
		return message;
	}

	public double getRemainingBytes(long time) {
		long interval = time - scheduledAt;
		return remainingBytes - (interval * bandwidth);
	}

	/*
	 * public long getArrivalTime() { return arrivalTime; }
	 */

}
