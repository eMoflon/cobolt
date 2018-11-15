/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api;

/**
 * Describing bitrates used for bandwidths and throughput. Use these constants
 * within your overlays and applications.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public final class Rate {

	/**
	 * One bit per second (base unit)
	 */
	public final static long bit_s = 1;

	/**
	 * One kilobit per second
	 */
	public final static long kbit_s = bit_s * 1000;

	/**
	 * One megabit per second
	 */
	public final static long Mbit_s = kbit_s * 1000;

	/**
	 * One gigabit per second
	 */
	public final static long Gbit_s = Mbit_s * 1000;

	public static long inBytesPerSecond(long bitrate) {
		return bitrate / 8;
	}

	/**
	 * Pretty-prints the given rate
	 * 
	 * @param rate
	 * @return
	 */
	public static String getFormattedRate(long rate) {
		if (rate > Rate.Gbit_s) {
			return String.format("%.2f GBit/s", rate / (double) Rate.Gbit_s);
		} else if (rate > Rate.Mbit_s) {
			return String.format("%.2f MBit/s", rate / (double) Rate.Mbit_s);
		} else if (rate > Rate.kbit_s) {
			return String.format("%.2f kBit/s", rate / (double) Rate.kbit_s);
		} else {
			return String.format("%.2f bit/s", rate / (double) Rate.bit_s);
		}
	}

}
