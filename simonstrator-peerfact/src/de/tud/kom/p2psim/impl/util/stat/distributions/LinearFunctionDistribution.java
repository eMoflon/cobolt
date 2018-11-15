/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
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

import de.tudarmstadt.maki.simonstrator.api.util.Distribution;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This distribution delivers values following a linear function from start to
 * end in the specified interval steps. It can be used for example to schedule
 * operations with a linearly increasing or decreasing time between each
 * execution.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 25.07.2012
 */
public class LinearFunctionDistribution implements Distribution {

	/**
	 * Slope
	 */
	private double m;

	/**
	 * Offset part
	 */
	private double c;

	private double x;

	private double x_max;

	/**
	 * To implement a decreasing distribution, just switch start and end
	 * 
	 * @param startValue
	 * @param endValue
	 *            upper bound for the distribution (all further calls after the
	 *            bound is reached will just return the bound itself)
	 * @param discretizationInterval
	 *            when going from start to end, each step will increment the
	 *            value according to this parameter
	 */
	@XMLConfigurableConstructor({ "startValue", "endValue",
			"discretizationInterval" })
	public LinearFunctionDistribution(double startValue, double endValue,
			double discretizationInterval) {
		this.x = 0;
		this.m = Math.signum(endValue - startValue) * discretizationInterval;
		this.c = startValue;
		this.x_max = Math.abs(endValue - startValue)
				/ discretizationInterval;
	}

	@Override
	public double returnValue() {
		if (x < x_max) {
			x++;
		}
		return m * x + c;
	}

}
