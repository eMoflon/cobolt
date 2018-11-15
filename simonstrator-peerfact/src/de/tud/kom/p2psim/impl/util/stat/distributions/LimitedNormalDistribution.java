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

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

public class LimitedNormalDistribution implements Distribution {
	private NormalDistributionImpl limitedNormal;

	private double mu;

	private double sigma;

	private boolean limitedMin;

	private boolean limitedMax;

	private double min;

	private double max;

	private double pmin = 0;

	private double pmax = 1;

	// pfactor and pmin are used to determine the range in which the random
	// values are allowed.
	private double pfactor;

	private int limitType;

	private LimitedNormalConfigurer conf;

	private final static int LIMIT_NORMAL_DIST_NONE = 0;

	private final static int LIMIT_NORMAL_DIST_MIN = 1;

	private final static int LIMIT_NORMAL_DIST_MAX = 2;

	private final static int LIMIT_NORMAL_DIST_BOTH = 3;
	
    @XMLConfigurableConstructor({"mu", "sigma", "min", "max", "limitedMin", "limitedMax"})	
	public LimitedNormalDistribution(double mu, double sigma, double min,
			double max, boolean limitedMin, boolean limitedMax) {
    	conf = new LimitedNormalConfigurer(mu, sigma, min, max, limitedMin, limitedMax);
		config(conf);
	}

	public void config(LimitedNormalConfigurer dc) {
		mu = dc.getMu();
		sigma = dc.getSigma();
		limitedMin = dc.isLimitedMin();
		limitedMax = dc.isLimitedMax();

		limitedNormal = new NormalDistributionImpl(mu, sigma);

		if (limitedMin == false) {
			if (limitedMax == false) {
				limitType = LIMIT_NORMAL_DIST_NONE;
			} else {
				// only max is limted
				limitType = LIMIT_NORMAL_DIST_MAX;
				max = dc.getMax();
				try {
					pmax = limitedNormal.cumulativeProbability(max);
				} catch (MathException e) {
					e.printStackTrace();
				}
			}
		} else {
			if (limitedMax == false) {
				// only min is limited.
				limitType = LIMIT_NORMAL_DIST_MIN;
				min = dc.getMin();
				try {
					pmin = limitedNormal.cumulativeProbability(min);
				} catch (MathException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// both sides limited.
				limitType = LIMIT_NORMAL_DIST_BOTH;

				// make sure min is really smaller than max.
				if (max > min) {
					min = dc.getMin();
					max = dc.getMax();
				} else {
					max = dc.getMin();
					min = dc.getMax();
				}

				// get min and max probabilites that are possible
				try {
					pmin = limitedNormal.cumulativeProbability(min);
					pmax = limitedNormal.cumulativeProbability(max);

					pfactor = pmax - pmin;

				} catch (MathException e) {
					e.printStackTrace();
				}
			}
		}
		pfactor = pmax - pmin;
	}

	public double returnValue() {
		double random = pmin + Randoms.getRandom(this).nextDouble() * pfactor;
		double result;

		try {
			result = limitedNormal.inverseCumulativeProbability(random);
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = 0;
		}

		return result;
	}

	/**
	 * @return Returns the limitType.
	 */
	public int getLimitType() {
		return limitType;
	}

	/**
	 * Returns a random value that is distributed as a Normal Distribution with
	 * an upper and lower limit.
	 * 
	 * @param _mu
	 *            average
	 * @param _sigma
	 *            standard deviation
	 * @param _min
	 *            lower limit, set to "null", if no limit
	 * @param _max
	 *            upper limit, set to "null", if no limit
	 * @return as double
	 */
	public static double returnValue(double _mu, double _sigma, Double _min,
			Double _max) {
		int llimitType;
		double lmax;
		double lmin;
		double lpmax = 1;
		double lpmin = 0;
		double lpfactor;

		NormalDistributionImpl llimitedNormal = new NormalDistributionImpl(_mu,
				_sigma);
		if (_min == null) {
			if (_max == null) {
				llimitType = LIMIT_NORMAL_DIST_NONE;
			} else {
				// only max is limted
				llimitType = LIMIT_NORMAL_DIST_MAX;
				lmax = _max.doubleValue();
				try {
					lpmax = llimitedNormal.cumulativeProbability(lmax);
				} catch (MathException e) {
					e.printStackTrace();
				}
			}
		} else {
			if (_max == null) {
				// only min is limited.
				llimitType = LIMIT_NORMAL_DIST_MIN;
				lmin = _min.doubleValue();
				try {
					lpmin = llimitedNormal.cumulativeProbability(lmin);
				} catch (MathException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// both sides limited.
				llimitType = LIMIT_NORMAL_DIST_BOTH;

				// make sure min is really smaller than max.
				if (_max.doubleValue() > _min.doubleValue()) {
					lmin = _min.doubleValue();
					lmax = _max.doubleValue();
				} else {
					lmax = _min.doubleValue();
					lmin = _max.doubleValue();
				}

				// get min and max probabilites that are possible
				try {
					lpmin = llimitedNormal.cumulativeProbability(lmin);
					lpmax = llimitedNormal.cumulativeProbability(lmax);

					lpfactor = lpmax - lpmin;

				} catch (MathException e) {
					e.printStackTrace();
				}
			}
		}
		lpfactor = lpmax - lpmin;

		double lrandom = lpmin
				+ Randoms.getRandom(LimitedNormalDistribution.class)
						.nextDouble()
				* lpfactor;
		double lresult;

		try {
			lresult = llimitedNormal.inverseCumulativeProbability(lrandom);
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lresult = 0;
		}

		return lresult;

	}
	
	private class LimitedNormalConfigurer {
		private double mu;

		private double sigma;

		private double min;

		private double max;

		private boolean limitedMin;

		private boolean limitedMax;

		public LimitedNormalConfigurer(double mu, double sigma, double min,
				double max, boolean limitedMin, boolean limitedMax) {
			super();
			this.mu = mu;
			this.sigma = sigma;
			this.min = min;
			this.max = max;
			this.limitedMin = limitedMin;
			this.limitedMax = limitedMax;
		}
		
		/**
		 * @return Returns the mu.
		 */
		public double getMu() {
			return mu;
		}

		/**
		 * @return Returns the sigma.
		 */
		public double getSigma() {
			return sigma;
		}

		/**
		 * @return Returns the max.
		 */
		public double getMax() {
			return max;
		}

		/**
		 * @return Returns the min.
		 */
		public double getMin() {
			return min;
		}

		/**
		 * @return Returns the limitedMax.
		 */
		public boolean isLimitedMax() {
			return limitedMax;
		}

		/**
		 * @return Returns the limitedMin.
		 */
		public boolean isLimitedMin() {
			return limitedMin;
		}
		
		@Override
		public String toString() {
			return "LimitedNormalDistribution [mu=" + mu + ", sigma=" + sigma
					+ ", min=" + min + ", max=" + max + ", limitedMin="
					+ limitedMin + ", limitedMax=" + limitedMax + "]";
		}
	}
	
	public String toString(){
		return conf.toString();
	}
}
