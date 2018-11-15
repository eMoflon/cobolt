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

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;

public class SimpleVariableLatencyModel extends SimpleStaticLatencyModel {

	private long variation = -1;

	private Random rnd = Randoms.getRandom(SimpleVariableLatencyModel.class);

	public SimpleVariableLatencyModel(long base, long variation,
			Random generator) {
		super(base);
		this.variation = variation;
		this.rnd = (generator != null) ? generator : rnd;
	}

	public SimpleVariableLatencyModel() {
		super(-1l);
	}

	/**
	 * Sets the base.
	 *
	 * @param base the new base
	 */
	public void setBase(long base) {
		this.setLatency(base);
	}

	/**
	 * Sets the variation.
	 *
	 * @param variation the new variation
	 */
	public void setVariation(long variation) {
		this.variation = variation;
	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.network.modular.st.latency.SimpleStaticLatencyModel#getMessagePropagationDelay(de.tud.kom.p2psim.api.network.NetMessage, de.tud.kom.p2psim.impl.network.AbstractNetLayer, de.tud.kom.p2psim.impl.network.AbstractNetLayer, de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB)
	 */
	@Override
	public long getMessagePropagationDelay(NetMessage msg, AbstractNetLayer nlSender, AbstractNetLayer nlReceiver, NetMeasurementDB db) {

		return (long) (getDistance(nlSender, nlReceiver) * ((rnd.nextDouble() - 0.5)
				* propagationDelay * variation * Time.MILLISECOND));
		
	}
	
}
