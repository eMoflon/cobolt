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

package de.tud.kom.p2psim.impl.util;

/**
 * A rational number expressed with long values for numerator and
 * denominator.
 * @author Leo Nobach
 * @version 1.0, mm/dd/2011
 */
public class RationalLong {

	public long getNumerator() {
		return numerator;
	}

	public void setNumerator(long enumerator) {
		this.numerator = enumerator;
	}

	public long getDenominator() {
		return denominator;
	}
	
	public boolean isNegative() {
		return (numerator < 0 && denominator > 0) || (numerator > 0 && denominator < 0);
	}

	public void setDenominator(long denominator) {
		this.denominator = denominator;
		if (denominator == 0) throw new IllegalArgumentException("The denominator must not be 0.");
	}

	long numerator;
	long denominator;
	
	public RationalLong(long numerator, long denominator) {
		this.numerator = numerator;
		if (denominator == 0) throw new IllegalArgumentException("The denominator must not be 0.");
		this.denominator = denominator;
	}
	
	/**
	 * Returns the double value closest or equal to the rational number
	 * expressed with this class.
	 * @return
	 */
	public double toDouble() {
		return numerator / (double) denominator;
	}
	
	public String toString() {
		return numerator + "/" + denominator;
	}
	
}
