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


package de.tud.kom.p2psim.impl.network.modular.st.jitter;

import java.util.Random;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.modular.ModularNetLayer;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.SummaryRelation;
import de.tud.kom.p2psim.impl.network.modular.st.JitterStrategy;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Applies a jitter with parameters derived from the PingER project.
 * Requires the network measurement database containing PingER measurements.
 * 
 * @author Leo Nobach
 *
 */
public class PingErJitter implements JitterStrategy {

	Random rand = Randoms.getRandom(PingErJitter.class);
	
	@Override
	public long getJitter(long cleanMsgPropagationDelay, NetMessage msg,
			ModularNetLayer nlSender, ModularNetLayer nlReceiver,
			NetMeasurementDB db) {
		
		if (db == null) throw new IllegalArgumentException("The PingEr jitter strategy can not access any network " +
		"measurement database. You may not have loaded it in the config file.");

		SummaryRelation rel = db.getMostAccurateSummaryRelation(nlSender.getDBHostMeta(), nlReceiver.getDBHostMeta());
		
		return Math.round(rel.tGetJitterLnDist().inverseF(rand.nextDouble())
				* 0.5 * Time.MILLISECOND);
			// divided by 2 because we have the RTT jitter, but want the delay jitter
		
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		//No simple/complex types to write back
	}

	
	
}
