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

package de.tud.kom.p2psim.impl.network.modular.st.latency;

import java.util.Random;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

// TODO: Auto-generated Javadoc
/**
 * The Class StaticLatencyWithVariance.
 */
public class StaticLatencyWithVariance implements LatencyStrategy {

	/** The propagation delay. */
	private long propagationDelay;

	/** The double variance. */
	private long doubleVariance;

	private Random rnd = Randoms.getRandom(StaticLatencyWithVariance.class);

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.util.BackToXMLWritable#writeBackToXML(de.tud.kom.p2psim.impl.util.BackToXMLWritable.BackWriter)
	 */
	@Override
	public void writeBackToXML(BackWriter bw) {
		bw.writeTime("latency", propagationDelay);
		bw.writeTime("variance", doubleVariance / 2);
	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy#getMessagePropagationDelay(de.tud.kom.p2psim.api.network.NetMessage, de.tud.kom.p2psim.impl.network.AbstractNetLayer, de.tud.kom.p2psim.impl.network.AbstractNetLayer, de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB)
	 */
	@Override
	public long getMessagePropagationDelay(NetMessage msg,
			AbstractNetLayer nlSender, AbstractNetLayer nlReceiver,
			NetMeasurementDB db) {
		if (doubleVariance == 0) {
			return propagationDelay;
		} else {
			return propagationDelay
					+ Math.round(((rnd.nextDouble() - 0.5) * doubleVariance));
		}
	}


	/**
	 * Sets the variance.
	 *
	 * @param variance the new variance
	 */
	public void setVariance(long variance) {
		// *2 to save one computational step in getLatency
		this.doubleVariance = variance * 2;
	}

	
	/**
	 * Sets the latency.
	 *
	 * @param propagationDelay the new latency
	 */
	public void setLatency(long latency) {
		this.propagationDelay = latency;
	}
}
