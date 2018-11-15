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

import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.core.TimeComponent;

/**
 * Definition of time constants used for scaling and definition of the current
 * time. Whenever times are calculated throughout the overlay/service/app, it
 * has to be done based on these relative units!
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public final class Time {

	/**
	 * A microsecond
	 */
	public final static long MICROSECOND = 1l;

	/**
	 * A millisecond
	 */
	public final static long MILLISECOND = 1000l * MICROSECOND;

	/**
	 * A second
	 */
	public final static long SECOND = 1000l * MILLISECOND;

	/**
	 * A minute
	 */
	public final static long MINUTE = 60l * SECOND;

	/**
	 * These constant should be ALWAYS used for virtual time calculations.
	 */
	public final static long HOUR = 60l * MINUTE;

	public static final double NANOSECOND = 0.001d;

	/**
	 * 
	 */
	public static TimeComponent bindedTime = null;

	private static TimeComponent getTimeComponent() {
		if (bindedTime == null) {
			try {
				bindedTime = Binder.getComponent(TimeComponent.class);
			} catch (ComponentNotAvailableException e) {
				System.err
						.println("Simonstrator-API WARNING: you are using Time, but no TimeComponent is provided. Relative time calculations will NOT work as expected!");
				bindedTime = new TimeComponent() {
					@Override
					public long getCurrentTime() {
						return System.currentTimeMillis();
					}
				};
			}
		}
		return bindedTime;
	}

	/**
	 * Returns the current unified time (i.e., in simulations the simTime and in
	 * real deployments the synchronized system time)
	 * 
	 * @return time
	 */
	public static long getCurrentTime() {
		return getTimeComponent().getCurrentTime();
	}

	/**
	 * Convenience method returning the current time as a readable string
	 * 
	 * @return
	 */
	public static String getFormattedTime() {
		return getFormattedTime(getCurrentTime());
	}

	/**
	 * Parse a time from a string containing units, i.e., 2s or 4h or 10ms or
	 * 1m.
	 * 
	 * @param value
	 * @return
	 */
	public static long parseTime(String value) {
		if (value.matches("\\d+(ms|s|m|h)")) {
			String number;
			long factor;
			if (value.matches("\\d+(ms)")) {
				number = value.substring(0, value.length() - 2);
				factor = Time.MILLISECOND;
			} else {
				number = value.substring(0, value.length() - 1);
				factor = 1;
				char unit = value.charAt(value.length() - 1);
				switch (unit) {
				case 'h':
					factor = Time.HOUR;
					break;
				case 'm':
					factor = Time.MINUTE;
					break;
				case 's':
					factor = Time.SECOND;
					break;
				default:
					throw new IllegalStateException("time unit " + unit
							+ " is not allowed");
				}
			}
			return factor * Long.valueOf(number);
		} else {
			throw new AssertionError("Not a valid Time!");
		}
	}

	/**
	 * Convenience method returning the given time as a readable string
	 * 
	 * @param time
	 * @return
	 */
	public static String getFormattedTime(long time) {
		return getHours(time) + ":" + getSimMinutes(time) % 60 + ":"
				+ getSimSeconds(time) % 60 + ":" + getSimMilliSeconds(time)
				% 1000 + " (H:m:s:ms)";
	}

	public static long getSimMilliSeconds(long time) {
		return Math.round(Math.floor((double) time / Time.MILLISECOND));
	}

	public static long getSimSeconds(long time) {
		return Math.round(Math.floor(getSimMilliSeconds(time) / 1000d));
	}

	public static long getSimMinutes(long time) {
		return Math.round(Math.floor(getSimSeconds(time) / 60d));
	}

	public static long getHours(long time) {
		return Math.round(Math.floor(getSimMinutes(time) / 60d));
	}

}
