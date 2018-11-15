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

/**
 * Toolkit for variuos static string operations.
 * @author Leo Nobach 
 *
 */
public class StringToolkit {

	/**
	 * Concatenates multiple strings to one string, separator-separated
	 * 
	 * @param array
	 * @return
	 */
	public static String arrayToString(Object[] array, String separator) {
		StringBuilder strB = new StringBuilder();
		strB.append(array[0]);
		for (int i = 1; i < array.length; i++) {
			strB.append(separator);
			strB.append(array[i]);
		}
		return strB.toString();
	}
	
	/**
	 * 
	 * 
	 * @param array
	 * @return
	 */
	public static String padFixed(String input, int padSize) {
		StringBuilder strB = new StringBuilder();
		strB.append(input);
		for (int i = input.length(); i < padSize; i++) strB.append(' ');
		return strB.toString();
	}

}
