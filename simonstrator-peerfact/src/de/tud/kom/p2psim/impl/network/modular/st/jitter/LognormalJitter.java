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
import de.tud.kom.p2psim.impl.network.modular.st.JitterStrategy;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;
import umontreal.iro.lecuyer.probdist.LognormalDist;

/**
 * Applies a jitter that is log-normally distributed.
 * 
 * Parameters: mu (double, unit in msec)
 * Parameters: sigma (double, unit in msec)
 * 
 * @author Leo Nobach
 *
 */
public class LognormalJitter implements JitterStrategy {

	LognormalDist dist = null;

	Random rand = Randoms.getRandom(LognormalJitter.class);
	private double mu = 1;	//Default value
	private double sigma = 0.6; //Default value
	
	@Override
	public long getJitter(long cleanMsgPropagationDelay, NetMessage msg,
			ModularNetLayer nlSender, ModularNetLayer nlReceiver,
			NetMeasurementDB db) {
		if (dist == null) {
			dist = new LognormalDist(mu, sigma);
		}
		return Math.round(dist.inverseF(rand.nextDouble()) * Time.MILLISECOND);
	}
	
	/**
	 * Sets the mu parameter (Unit is msec)
	 * @param mu
	 */
	public void setMu(double mu) {
		this.mu = mu;
	}
	
	/**
	 * Sets the sigma parameter (Unit is msec)
	 * @param sigma
	 */
	public void setSigma(double sigma) {
		this.sigma = sigma;
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		bw.writeSimpleType("mu", mu);
		bw.writeSimpleType("sigma", sigma);
	}

}
