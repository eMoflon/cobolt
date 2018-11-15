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

package de.tud.kom.p2psim.impl.network.gnp.topology;

import java.util.List;

import de.tud.kom.p2psim.impl.network.gnp.topology.Host.CumulateRTTStrategy;
import de.tud.kom.p2psim.impl.util.toolkits.CollectionHelpers;

public class RTTCumulationStrategies {

	public static class Minimum implements CumulateRTTStrategy {

		@Override
		public Double cumulate(List<Double> rtts) {
			if (rtts.isEmpty()) return Double.NaN;
			double minimum = Double.MAX_VALUE;
			for (double rtt : rtts) {
				if (rtt < minimum) minimum = rtt;
			}
			return minimum;
		}
		
	}
	
	public static class Average implements CumulateRTTStrategy {

		@Override
		public Double cumulate(List<Double> rtts) {
			if (rtts.isEmpty()) return Double.NaN;
			double acc = 0d;
			for (double rtt : rtts) acc += rtt;
			return acc/rtts.size();
		}
		
	}
	
	public static class Median implements CumulateRTTStrategy {

		@Override
		public Double cumulate(List<Double> rtts) {
			return CollectionHelpers.getQuantile(rtts, 0.5d);
		}
		
	}
	
}
