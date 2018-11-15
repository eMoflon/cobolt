package de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.spanner;
/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
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

/**
 * 
 * @author Michael Stein
 *
 */
public class PowerSpanner extends SpannerMetric {

	private double alpha = 2.0;
	
	@Override
	protected double getSpannerWeight(double len) {
		return computePowerConsumption(len, alpha);
	}
	
	/**
	 * Computes the power consumption for 1-hop communication on the given
	 * distance with the given path loss parameter
	 * 
	 * @param euclideanDistance
	 * @param alpha
	 */
	public static double computePowerConsumption(double euclideanDistance,
			double alpha) {
		return Math.pow(euclideanDistance, alpha);
	}
	
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	@Override
	protected String getWeightMetricName() {
		return "PowerAlpha" + alpha;
	}
}
