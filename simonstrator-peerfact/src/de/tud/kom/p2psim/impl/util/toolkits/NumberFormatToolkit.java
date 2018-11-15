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


package de.tud.kom.p2psim.impl.util.toolkits;

import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Allows generic tool methods for formatting numbers for human-readable representation.
 * 
 * @author Leo Nobach
 *
 */
public class NumberFormatToolkit {

	/**
	 * Formats the given double value of the simulation time to a string representing 
	 * the seconds from simulation beginning.
	 * 
	 * @param simTime , the current simulation time
	 * @param decimals , the trailing decimal digit count.
	 * @return
	 */
	public static String formatSecondsFromSimTime(double simTime, int decimals) {
		return floorToDecimals(simTime / Time.SECOND, decimals) + " s";
	}

	/**
	 * Formats the given quota value to a string representing the percentage, where
	 * quota = 1 results in '100%' and quota=0 results in '0%'
	 *
	 */
	public static String formatPercentage(double quota, int decimals) {
		return floorToDecimals(quota * 100d, decimals) + "%";
	}

	/**
	 * Floors the given double to the largest double value that is less than or equal
	 * to the argument and is a decimal fraction value representable with the given 
	 * decimal digit count.
	 * @param value
	 * @param decimals , the given decimal count.
	 * @return
	 */
	public static double floorToDecimals(double value, int decimals) {
		double pow = Math.pow(10, decimals);
		return (Math.floor(value * pow)) / pow;
	}

	/**
	 * Floors the given double to the largest double value that is less than or equal
	 * to the argument and is a decimal fraction value representable with the given 
	 * decimal digit count. Returns a string.
	 * @param value
	 * @param decimals , the given decimal count.
	 * @return
	 */
	public static String floorToDecimalsString(double value, int decimals) {
		return String.valueOf(floorToDecimals(value, decimals));
	}
	
	/**
	 * Rounds the given double to a decimal fraction value representable with the given 
	 * decimal digit count.
	 * @param value
	 * @param decimals , the given decimal count.
	 * @return
	 */
	public static double roundToDecimals(double value, int decimals) {
		double pow = Math.pow(10, decimals);
		return (Math.round(value * pow)) / pow;
	}

	/**
	 * Rounds the given double to a decimal fraction value representable with the given 
	 * decimal digit count. Returns a string.
	 * @param value
	 * @param decimals , the given decimal count.
	 * @return
	 */
	public static String roundToDecimalsString(double value, int decimals) {
		return String.valueOf(roundToDecimals(value, decimals));
	}
	
	/**
	 * Formats the given value by using SI prefixes. Currently does NOT support
	 * values smaller than 1 (m, µ, n, p, ...)
	 * @param value
	 * @param decimals , the amount of decimal fraction digits to use to represent the value
	 * @param use1024 , whether to use 1K = 1024, instead of 1K = 1000
	 * @return
	 */
	public static String formatSIPrefix(final double value, int decimals, boolean use1024) {
		
		char[] siPrefixes = {'k','M','G','T','P','E','Z','Y'};
		
		int divisor = use1024?1024:1000;
		
		double valTemp = value;
		
		int i = 0;
		while (valTemp > divisor && i < siPrefixes.length) {
		 valTemp /= divisor;
		 i++;
		}
		
		return floorToDecimalsString(valTemp, decimals) + (i==0?"":siPrefixes[i-1]);
		
		
	}
	
	public static String formatRatio(double numerator, double denominator, int decimals) {
		return denominator <= 0?"n/a":NumberFormatToolkit.formatPercentage(numerator/denominator, decimals);
	}

}
