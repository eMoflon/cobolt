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



package de.tud.kom.p2psim.impl.util.stat.distributions;

import java.util.Random;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

public class NormalDistribution implements Distribution {

	private NormalDistributionImpl normal;

	private Random randomGen = Randoms.getRandom(this);

	private double mu;

	private double sigma;

    @XMLConfigurableConstructor({"mu", "sigma"})
	public NormalDistribution(double mu, double sigma) {
		this.mu = mu;
		this.sigma = sigma;
		normal = new NormalDistributionImpl(mu, sigma);
	}

	@Override
	public double returnValue() {
        double random = randomGen.nextDouble();

		double result;

		try {
			result = normal.inverseCumulativeProbability(random);
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = 0;
		}

		return result;
	}

	@Override
	public String toString() {
		return "NormalDistribution [mu=" + mu + ", sigma=" + sigma + "]";
	}

	/**
	 * returns a random value normally distributed with mu = _mu and sigma =
	 * _sigma.
	 * 
	 * @param _mu
	 * @param _sigma
	 * @return as double
	 */
	public static double returnValue(double _mu, double _sigma) {
		try {
			NormalDistributionImpl d = new NormalDistributionImpl(_mu, _sigma);
			return d.inverseCumulativeProbability(Randoms.getRandom(
					NormalDistribution.class)
					.nextDouble());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

	}
	
}
