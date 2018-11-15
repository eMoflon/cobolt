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

import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Returns a normal distributed latency.<br>
 * If the variance to big, so that exists negative delay, then will be returned
 * the mean delay.
 * 
 * Parameters: propagationMeanDelay:long (in Simulation time units) <br>
 * standardDeviation:long (in Simulation time units)
 * 
 * @author Christoph Muenker
 * @version 1.0, 04/07/2011
 */
public class NormalDistributionLatency implements LatencyStrategy {

	private Random rnd = Randoms.getRandom(NormalDistributionLatency.class);

	/**
	 * The size of precomputed delays
	 */
	private static final int DELAY_SIZE = 10000;

	/**
	 * The mean delay
	 */
	private long propagationMeanDelay = 10 * Time.MILLISECOND; // 10
																			// ms

	/**
	 * The standard deviation
	 */
	private long standardDeviation = 2 * Time.MICROSECOND; // 2ms

	private NormalDistributionImpl normalDistribution = new NormalDistributionImpl(
			propagationMeanDelay, standardDeviation);

	/**
	 * The precomputed delays
	 */
	private List<Long> delays = null;

	@Override
	public long getMessagePropagationDelay(NetMessage msg,
			AbstractNetLayer nlSender, AbstractNetLayer nlReceiver,
			NetMeasurementDB db) {
		if (delays == null) {
			createDelays(DELAY_SIZE);
		}

		return delays.get(rnd.nextInt(DELAY_SIZE));
	}

	/**
	 * Precompute the delays.
	 * 
	 * @param size
	 *            The number of delays, which should be computed and stored in
	 *            delays.
	 */
	private void createDelays(int size) {

		if (propagationMeanDelay < standardDeviation) {
			Monitor.log(
					NormalDistributionLatency.class,
					Level.WARN,
					"Bad Configuration! It is possible, that huge delays are negative and consequently this values will be set to the mean delay value.");
		}

		delays = new Vector<Long>(size);
		for (int i = 0; i < size; i++) {
			long delay = 0;
			try {
				delay = (long) normalDistribution
						.inverseCumulativeProbability(rnd.nextDouble());
			} catch (MathException e) {
				throw new RuntimeException(e);
			}
			if (delay <= 0) {
				delay = propagationMeanDelay;
			}
			delays.add(delay);
		}
	}

	public void setPropagationMeanDelay(long delay) {
		if (delay < 0) {
			throw new RuntimeException(
					"Bad PropagationMeanDelay. It should be positiv!");
		}
		this.propagationMeanDelay = delay;
		this.normalDistribution = new NormalDistributionImpl(
				this.propagationMeanDelay, this.standardDeviation);
	}

	public void setStandardDeviation(long standardDeviation) {
		this.standardDeviation = standardDeviation;
		this.normalDistribution = new NormalDistributionImpl(
				this.propagationMeanDelay, this.standardDeviation);
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		bw.writeTime("propagationMeanDelay", propagationMeanDelay);
		bw.writeTime("standardDeviation", standardDeviation);
	}

}
