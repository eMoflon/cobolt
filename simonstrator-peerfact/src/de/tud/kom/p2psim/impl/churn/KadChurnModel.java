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

package de.tud.kom.p2psim.impl.churn;

import java.util.List;

import org.apache.commons.math.distribution.WeibullDistributionImpl;

import de.tud.kom.p2psim.api.churn.ChurnModel;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * This churn model is adapted from the technical report of Moritz Steiner et
 * al.: "Analyzing Peer Behavior in KAD". The authors define the session as the
 * time a host was present in the system without an interruption whereas the
 * inter-session time is defined as the time a host is continuously absent from
 * the system.
 * 
 * The weibull distributions used in this churn model result from a distribution
 * fit of the measured date of about six month in KAD:
 * 
 * Session time distribution (Weibull) - First crawl :
 * mean=670.6789,std=1741.3677,scale=357.7152,shape=0.54512
 * 
 * - Up from 2nd crawl: mean=266.5358,std=671.5063,scale=169.5385,shape=0.61511
 * 
 * Inter-session time distribution (Weibull) - First crawl :
 * mean=1110.2091,std=4308.0877,scale=413.6765,shape=0.47648
 * 
 * @author Sebastian Kaune
 * @version 3.0, 18.03.2008
 * 
 */
public class KadChurnModel implements ChurnModel {

	public WeibullDistributionImpl sessionTime, interSessionTime;

	public KadChurnModel() {
		sessionTime = new WeibullDistributionImpl(0.61511, 169.5385);
		interSessionTime = new WeibullDistributionImpl(0.47648, 413.6765);
	}

	/**
	 * Just a quick hack to ease batch-simulation of different
	 * KADChurn-Parameters
	 * 
	 * @param lambda
	 */
	public void setLambdaFactor(double lambda) {
		sessionTime = new WeibullDistributionImpl(0.61511, 169.5385 * lambda);
		interSessionTime = new WeibullDistributionImpl(0.47648,
				413.6765 * lambda);
	}

	public long getNextDowntime(SimHost host) {
		long time = Math.round(sessionTime.inverseCumulativeProbability(Randoms
				.getRandom(KadChurnModel.class).nextDouble()) * Time.MINUTE);
		return time;
	}

	public long getNextUptime(SimHost host) {
		long time = Math.round(interSessionTime
				.inverseCumulativeProbability(Randoms.getRandom(
						KadChurnModel.class).nextDouble())
				* Time.MINUTE);
		return time;
	}

	public void prepare(List<SimHost> churnHosts) {
		// nothing to do
	}

}
