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

package de.tud.kom.p2psim.impl.util.stats;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * This class provides a statistical calculations for a collection of data.
 * 
 * @author Christoph Muenker
 * @version 03/08/2011
 */
public class StatisticComputation {

	/**
	 * Derives the average for the list of doubles.
	 * 
	 * @param values
	 *            A list of doubles.
	 * @return The average of the values. If the size of the list equals 0, then
	 *         return null or the list is null
	 */
	public static Double arithmeticMean(List<Double> values) {
		if (values == null || values.size() == 0) {
			return null;
		}
		double sum = 0;

		for (Double v : values) {
			sum += v;
		}
		Double avg = sum / values.size();
		return avg;
	}

	/**
	 * Derives the standard deviation for the given list of doubles, with the
	 * arithmetic mean.
	 * 
	 * @param values
	 *            A list of doubles.
	 * @return The standard deviation of the values. If the size of the list is
	 *         smaller then 2 or the list is null, then return null;
	 */
	public static Double standardDeviation(List<Double> values) {
		if (values == null) {
			return null;
		}
		Double standardDeviation = computeStandardDeviation(values,
				arithmeticMean(values));
		return standardDeviation;
	}

	/**
	 * Derives the standard deviation for the given list of doubles to the given
	 * average.
	 * 
	 * @param values
	 *            A list of doubles.
	 * @param average
	 *            The average of the given list.
	 * @return The standard deviation of the values. If the size of the list is
	 *         smaller then 2 or the list is null, then return null;
	 */
	private static Double computeStandardDeviation(List<Double> values,
			Double average) {
		if (average == null) {
			return null;
		}
		double sumOfSquares = 0;
		Double standardDeviation = 0.0;
		if (values != null && values.size() > 1) {
			for (Double dd : values) {
				double ddMinusAvg = dd - average;
				sumOfSquares += ddMinusAvg * ddMinusAvg;
			}
			standardDeviation = Math.sqrt(sumOfSquares / (values.size() - 1));
		} else {
			standardDeviation = null;
		}
		return standardDeviation;
	}

	/**
	 * Derives the median for the given list of doubles.
	 * 
	 * @param values
	 *            A list of doubles.
	 * @return The median of the values. If the size of the list is equals 0 or
	 *         the list is null, then return null;
	 */
	public static Double median(List<Double> values) {
		if (values == null || values.size() == 0)
			return null;

		List<Double> cpyValues = new Vector<Double>(values);

		Collections.sort(cpyValues);

		Double median = cpyValues.get((int) Math.floor(cpyValues.size() / 2));

		return median;
	}

	/**
	 * Derives the truncated mean for the given list of doubles. For that it
	 * removes values which smaller then quantil alpha and greater then quantil
	 * (1-alpha). Over the other values will be derived the average.
	 * 
	 * @param values
	 *            A list of doubles.
	 * @param alpha
	 *            A value between 0 and 0.5.
	 * @return The truncated Mean of the values. If the list is null or it is
	 *         not possible to compute the truncated mean, then return null
	 */
	public static Double truncatedMean(List<Double> values, double alpha) {
		if (alpha < 0 && alpha > 0.5) {
			throw new IllegalArgumentException(
					"The alpha value isn't defined ("
							+ alpha
							+ "). The alpha value for truncated mean is defined for in interval [0,0.5] ∈ ℝ");
		}

		if (values == null || values.size() == 0)
			return null;

		List<Double> cpyValues = new Vector<Double>(values);

		Collections.sort(cpyValues);
		int k = (int) (cpyValues.size() * alpha);
		int start = k;
		int end = cpyValues.size() - k;
		double sum = 0;
		for (int i = start; i < end; i++) {
			sum += cpyValues.get(i);
		}
		Double result = 0.0;
		if ((cpyValues.size() - 2 * k) == 0) {
			result = null;
		} else {
			result = sum / (cpyValues.size() - 2 * k);
		}
		return result;
	}

	/**
	 * Derives the quantil of the values, with the given p-quantil
	 * 
	 * @param values
	 *            A list of doubles
	 * @param p_quantil
	 *            The pth quantil. A value between [0,1]
	 * @return The p-quantil of the values. If the list is null or the size is
	 *         equals 0, then return null.
	 */
	public static Double quantile(List<Double> values, double p_quantil) {
		if (p_quantil < 0 && p_quantil > 1) {
			throw new IllegalArgumentException(
					"The quantil isn't defined for the value "
							+ p_quantil
							+ ". The quantil is defined for in interval [0,1] ∈ ℝ");
		}

		if (values == null || values.size() == 0)
			return null;

		List<Double> cpyValues = new Vector<Double>(values);

		Collections.sort(cpyValues);

		double quantil = cpyValues.get((int) Math.floor(cpyValues.size()
				* p_quantil));
		return quantil;
	}

	/**
	 * Derives the maximal value for the given list of values.
	 * 
	 * @param values
	 *            A list of doubles
	 * @return The maximal value. If the list null or the size is equals 0, then
	 *         return null.
	 */
	public static Double max(List<Double> values) {
		if (values == null || values.size() == 0)
			return 0.0;

		List<Double> cpyValues = new Vector<Double>(values);
		Collections.sort(cpyValues);
		return cpyValues.get(cpyValues.size() - 1);
	}

	/**
	 * Derives the minimal value for the given list of values.
	 * 
	 * @param values
	 *            A list of doubles
	 * @return The minimal value. If the list null or the size is equals 0, then
	 *         return null.
	 */
	public static Double min(List<Double> values) {
		if (values == null || values.size() == 0)
			return 0.0;

		List<Double> cpyValues = new Vector<Double>(values);
		Collections.sort(cpyValues);
		return cpyValues.get(0);
	}

	/**
	 * Derives the geometric mean for the given values
	 * 
	 * @param values
	 *            A list of doubles
	 * @return The geometric mean for the values. If the list null or the size
	 *         is equals 0, then return null.
	 */
	public static Double geometricMean(List<Double> values) {
		if (values == null || values.size() == 0)
			return null;
		double temp = 0;
		for (Double v : values) {
			temp *= v;
		}
		return Math.pow(temp, 1.0 / values.size());
	}

	/**
	 * Derives the root mean square for the given values
	 * 
	 * @param values
	 *            A list of doubles
	 * @return The root mean square for the values. If the list null or the size
	 *         is equals 0, then return null.
	 */
	public static Double rootMeanSquare(List<Double> values) {
		return generalizedMean(values, 2);
	}

	/**
	 * Derives the cubic means for the given values
	 * 
	 * @param values
	 *            A list of doubles
	 * @return The cubic means for the values. If the list null or the size is
	 *         equals 0, then return null.
	 */
	public static Double cubicMeans(List<Double> values) {
		return generalizedMean(values, 3);
	}

	/**
	 * Derives the generalized mean, also known as power mean or Hölder mean for
	 * the given values. It computes root(sum(values^p)/values.size, p)
	 * 
	 * @param values
	 *            A list of doubles
	 * @param p
	 *            A real number, if not equals 0.
	 * @return The generalized mean for the values. If the list null or the size
	 *         is equals 0, then return null.
	 */
	public static Double generalizedMean(List<Double> values, double p) {
		if (p == 0) {
			throw new IllegalArgumentException(
					"The argument in p for generalizedMean cannot be 0.");
		}
		if (values == null || values.size() == 0)
			return null;
		double squareSum = 0;
		for (Double v : values) {
			squareSum += Math.pow(v, p);
		}
		return Math.pow(squareSum / values.size(), 1.0 / p);
	}

	/**
	 * Derives the sum of all values in the given list.
	 * 
	 * @param values
	 *            A list of doubles
	 * @return The sum of all values
	 */
	public static Double sum(List<Double> values) {
		if (values == null || values.size() == 0)
			return 0.0;
		double sum = 0;
		for (Double v : values) {
			sum += v;
		}
		return sum;
	}

	/**
	 * Derives the square sum of all values in the given list
	 * 
	 * @param values
	 *            A list of doubles
	 * @return The squared sum of all values.
	 */
	public static Double sum2(List<Double> values) {
		if (values == null || values.size() == 0)
			return 0.0;
		double sumSquare = 0;
		for (Double v : values) {
			sumSquare += v * v;
		}
		return sumSquare;
	}
}
