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


package de.tud.kom.p2psim.impl.network.modular.common;

import java.util.ArrayList;
import java.util.Collections;

import umontreal.iro.lecuyer.probdist.LognormalDist;

/**
 * 
 * Taken from Gerald Klunker's PingErLookup
 * 
 * @author Gerald Klunker, Leo Nobach
 *
 */
public class PingErToolkit {

	public static JitterParameter getJitterParameterFrom(double avgRtt, double minRtt, double dVar) {
		return getJitterParameterDownhillSimplex(avgRtt - minRtt, dVar);
	}
	
	public static LognormalDist getNewLnDistFrom(double avgRtt, double minRtt,
			double dVar) {
		JitterParameter params = getJitterParameterFrom(avgRtt, minRtt, dVar);
		return new LognormalDist(params.m, params.s);
	}
	
	/**
	 * Implemenation of a downhill simplex algortihm that finds the
	 * log-normal parameters mu and sigma that minimized the error between
	 * measured expectation and iqr and the resulting expectation and iqr.
	 * 
	 * @param expectation
	 *            value
	 * @param iqr
	 *            variation
	 * @return JitterParameter object with the log-normal parameter mu and
	 *         sigma
	 */
	public static JitterParameter getJitterParameterDownhillSimplex(
			double expectation, double iqr) {

		ArrayList<JitterParameter> solutions = new ArrayList<JitterParameter>();
		solutions.add(new JitterParameter(0.1, 0.1, expectation, iqr));
		solutions.add(new JitterParameter(0.1, 5.0, expectation, iqr));
		solutions.add(new JitterParameter(5.0, 0.1, expectation, iqr));
		Collections.sort(solutions);

		// 100 interations are enough for good results
		for (int c = 0; c < 100; c++) {
			JitterParameter newSolution = getNewParameter1(solutions,
					expectation, iqr);
			if (newSolution != null
					&& newSolution.getError() < solutions.get(0).getError()) {
				JitterParameter newSolution2 = getNewParameter2(solutions,
						expectation, iqr);
				if (newSolution2 != null
						&& newSolution2.getError() < newSolution.getError()) {
					solutions.remove(2);
					solutions.add(newSolution2);
				} else {
					solutions.remove(2);
					solutions.add(newSolution);
				}
			} else if (newSolution != null
					&& newSolution.getError() < solutions.get(2).getError()) {
				solutions.remove(2);
				solutions.add(newSolution);
			} else {
				solutions.get(1).m = solutions.get(1).m + 0.5
						* (solutions.get(0).m - solutions.get(1).m);
				solutions.get(2).m = solutions.get(2).m + 0.5
						* (solutions.get(0).m - solutions.get(2).m);
				solutions.get(1).s = solutions.get(1).s + 0.5
						* (solutions.get(0).s - solutions.get(1).s);
				solutions.get(2).s = solutions.get(2).s + 0.5
						* (solutions.get(0).s - solutions.get(2).s);
			}
			Collections.sort(solutions);
		}
		return solutions.get(0);
	}
	
	/**
	 * Container the log-normal distribution parameters. Used in the
	 * Downhill Simplex method.
	 * 
	 */
	public static class JitterParameter implements Comparable<JitterParameter> {

		double m;

		double s;

		double ew;

		double iqr;

		public JitterParameter(double m, double s, double ew, double iqr) {
			this.m = m;
			this.s = s;
			this.ew = ew;
			this.iqr = iqr;
		}

		/**
		 * error will be minimized within the downhill simplx algorithm
		 * 
		 * @return error (variation between measured expectation and iqr and
		 *         the resulting log-normal expectation and iqr.
		 */
		public double getError() {
			LognormalDist jitterDistribution = new LognormalDist(m, s);
			double error1 = Math.pow((iqr - (jitterDistribution
					.inverseF(0.75) - jitterDistribution.inverseF(0.25)))
					/ iqr, 2);
			double error2 = Math.pow((ew - Math.exp(m
					+ (Math.pow(s, 2) / 2.0)))
					/ ew, 2);
			return error1 + error2;
		}

		public int compareTo(JitterParameter p) {
			double error1 = this.getError();
			double error2 = p.getError();
			if (error1 < error2)
				return -1;
			else if (error1 > error2)
				return 1;
			else
				return 0;
		}

		/**
		 * 
		 * @return expectation value of the log-normal distribution
		 */
		public double getAverageJitter() {
			return Math.exp(m + (Math.pow(s, 2) / 2.0));
		}

		/**
		 * 
		 * @return iqr of the log-normal distribution
		 */
		public double getIQR() {
			LognormalDist jitterDistribution = new LognormalDist(m, s);
			return jitterDistribution.inverseF(0.75)
					- jitterDistribution.inverseF(0.25);
		}

		@Override
		public String toString() {
			LognormalDist jitterDistribution = new LognormalDist(m, s);
			double iqr1 = jitterDistribution.inverseF(0.75)
					- jitterDistribution.inverseF(0.25);
			double ew1 = Math.exp(m + (Math.pow(s, 2) / 2.0));
			return "m: " + m + " s: " + s + " Error: " + getError()
					+ " iqr: " + iqr1 + " ew: " + ew1;
		}

	}
	
	/**
	 * movement of factor 2 to center of solutions
	 * 
	 * @param solutions
	 * @param expectation
	 * @param iqr
	 * @return moved solution
	 */
	private static JitterParameter getNewParameter1(
			ArrayList<JitterParameter> solutions, double expectation,
			double iqr) {
		double middleM = (solutions.get(0).m + solutions.get(1).m + solutions
				.get(2).m) / 3.0;
		double middleS = (solutions.get(0).s + solutions.get(1).s + solutions
				.get(2).s) / 3.0;
		double newM = middleM + (solutions.get(0).m - solutions.get(2).m);
		double newS = middleS + (solutions.get(0).s - solutions.get(2).s);
		if (newS > 0)
			return new JitterParameter(newM, newS, expectation, iqr);
		else
			return null;
	}

	/**
	 * movement of factor 3 to center of solutions
	 * 
	 * @param solutions
	 * @param expectation
	 * @param iqr
	 * @return moved solution
	 */
	private static JitterParameter getNewParameter2(
			ArrayList<JitterParameter> solutions, double expectation,
			double iqr) {
		double middleM = (solutions.get(0).m + solutions.get(1).m + solutions
				.get(2).m) / 3.0;
		double middleS = (solutions.get(0).s + solutions.get(1).s + solutions
				.get(2).s) / 3.0;
		double newM = middleM + 2
				* (solutions.get(0).m - solutions.get(2).m);
		double newS = middleS + 2
				* (solutions.get(0).s - solutions.get(2).s);
		if (newS > 0)
			return new JitterParameter(newM, newS, expectation, iqr);
		else
			return null;
	}
	
}
