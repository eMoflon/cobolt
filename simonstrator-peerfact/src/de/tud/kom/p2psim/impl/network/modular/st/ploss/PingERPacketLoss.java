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


package de.tud.kom.p2psim.impl.network.modular.st.ploss;

import java.util.Random;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.SummaryRelation;
import de.tud.kom.p2psim.impl.network.modular.st.PLossStrategy;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * Applies a packet loss as measured by the PingEr project
 * 
 * @author Leo Nobach
 *
 */
public class PingERPacketLoss implements PLossStrategy {

	Random rand = Randoms.getRandom(PingERPacketLoss.class);

	@Override
	public boolean shallDrop(NetMessage msg, AbstractNetLayer nlSender,
			AbstractNetLayer nlReceiver, NetMeasurementDB db) {
		
		if (db == null) throw new IllegalArgumentException("The PingER packet loss strategy can not access any network " +
				"measurement database. You may not have loaded it in the config file.");
		
		SummaryRelation sumRel = db.getMostAccurateSummaryRelation(nlSender.getDBHostMeta(), nlReceiver.getDBHostMeta());
		
		if (sumRel == null) throw new AssertionError("No summary relation could be found for " + nlSender + " - " + nlReceiver);
		
		// If the message consists of multiple fragments, the loss probability 
		// will be the probability that all fragments have arrived, and
		// every fragment itself has the probability of "sumRel.getpLoss()" to be dropped
		double prob = 1d - Math.pow(1d - sumRel.getpLoss() * 0.01, msg.getNoOfFragments());
		Monitor.log(
				PingERPacketLoss.class,
				Level.DEBUG,
				"Dropping with probability " + prob + ", fragments "
						+ msg.getNoOfFragments());
		return rand.nextDouble() < prob;
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		//No simple/complex types to write back
	}
	
}
