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

import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;
import umontreal.iro.lecuyer.probdist.LognormalDist;

public class MixedLogNormalDistribution implements Distribution {

	@Override
	public String toString() {
		return "MixedLogNormalDistribution [weight1=" + weight1 + ", mu1="
				+ mu1 + ", sigma1=" + sigma1 + ", weight2=" + weight2
				+ ", mu2=" + mu2 + ", sigma2=" + sigma2 + "]";
	}

	private double weight1;

	private double mu1;

	private double sigma1;

	private double weight2;

	private double mu2;

	private double sigma2;

	private LognormalDist distr1;

	private LognormalDist distr2;

    @XMLConfigurableConstructor({"mu1", "mu2", "sigma1", "sigma2", "weight1", "weight2"})
	public MixedLogNormalDistribution(double mu1, double mu2, double sigma1, double sigma2, double weight1, double weight2) {
		this.mu1 = mu1;
		this.mu2 = mu2;
		this.sigma1 = sigma1;
		this.sigma2 = sigma2;
		this.weight1 = weight1;
		this.weight2 = weight2;
		distr1 = new LognormalDist(mu1, sigma1);
		distr2 = new LognormalDist(mu2, sigma2);
	}
	
	public double returnValue() {
		double random = Randoms.getRandom(this).nextDouble();
		double result = 0;
		try {
			result = weight1 * distr1.inverseF(random) + weight2
					* distr2.inverseF(random);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * returns a random value distributed after a mixed lognormal distribution:
	 * _weight1 * lognormal1 + _weight2 * lognormal2 with lorgnormal1(_mu1,
	 * _sigma1) and with lorgnormal1(_mu2, _sigma2).
	 * 
	 * @param _mu1
	 * @param _sigma1
	 * @param _weight1
	 * @param _mu2
	 * @param _sigma2
	 * @param _weight2
	 * @return
	 */
	public static double returnValue(double _mu1, double _sigma1,
			double _weight1, double _mu2, double _sigma2, double _weight2) {
		try {
			LognormalDist d1 = new LognormalDist(_mu1, _sigma1);
			LognormalDist d2 = new LognormalDist(_mu2, _sigma2);
			double random = Randoms.getRandom(MixedLogNormalDistribution.class)
					.nextDouble();

			return _weight1 * d1.inverseF(random) + _weight2
					* d2.inverseF(random);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

	}

}
