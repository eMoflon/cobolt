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

import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

public class UniformDistribution implements Distribution {
	@Override
	public String toString() {
		return "UniformDistribution [min=" + min + ", max=" + max + "]";
	}

	private double min;

	private double max;

	private double factor;

	private Random random;

    @XMLConfigurableConstructor({"min", "max"})
	public UniformDistribution(double min, double max) {
		this.min = Math.min(min, max);
		this.max = Math.max(min, max);
		this.factor = Math.abs(max - min);
		this.random = Randoms.getRandom(this);
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	/**
	 * Delivers a random value distributed as the configured distribution.
	 */
	@Override
	public double returnValue() {
		return min + factor * random.nextDouble();
	}

	/**
	 * delivers a random value that is uniformly distributed between the _min
	 * and the _max value.
	 * 
	 * @param _min
	 * @param _max
	 * @return random value as double
	 */
	public static double returnValue(double _min, double _max) {
		double lmin, lmax, lfactor;
		if (_min < _max) {
			lmin = _min;
			lmax = _max;
		} else {
			lmin = _max;
			lmax = _min;
		}
		lfactor = Math.abs(lmax - lmin);

		return lmin + lfactor
				* Randoms.getRandom(UniformDistribution.class).nextDouble();
	}

}
