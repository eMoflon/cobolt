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
 * String parsing and displaying toolkit for simulator's time units
 * 
 * @author Leo Nobach
 * 
 */
public class TimeToolkit {

	private long millisecondUnit;

	final long day;

	final long hour;

	final long minute;

	final long second;
	
	boolean separateTimeUnitsWhitespace = true;

	/**
	 * Creates a new TimeToolkit with the given millisecond unit. With
	 * PeerfactSim, you probably want to give Simulator.MILLISECOND_UNIT here.
	 * 
	 * @param millisecondUnit
	 */
	public TimeToolkit(long millisecondUnit) {
		this.millisecondUnit = millisecondUnit;
		day = (millisecondUnit * 1000l * 60l * 60l * 24l);
		hour = (millisecondUnit * 1000l * 60l * 60l);
		minute = (millisecondUnit * 1000l * 60l);
		second = (millisecondUnit * 1000l);
	}
	
	public void setSeparateTimeUnitsWhitespace (boolean separateTimeUnitsWhitespace) {
		this.separateTimeUnitsWhitespace = separateTimeUnitsWhitespace;
	}

	/**
	 * Parses the given string and returns the value in simulation time units.
	 * Format should be:
	 * 
	 * <pre>
	 * --h--m--s--ms
	 * </pre>
	 * 
	 * where every <b>-</b> should be a valid integer. Alternatively, a valid
	 * "long" simulation time unit may be parsed, if there are no time units
	 * given. Whitespaces and dots will be ignored.
	 * 
	 * @param value
	 * @return
	 */
	public long longFromTimeString(final String value) {
		int ms = 0;
		int s = 0;
		int m = 0;
		int h = 0;

		StringBuilder buf = new StringBuilder();

		boolean msSwitch = false;
		boolean msSwitchAfter = false;

		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);

			if (msSwitch == true) {
				if (c == 's') {
					// ===>milliseconds
					if (ms != 0 || buf.length() == 0)
						return -1;
					ms = Integer.valueOf(buf.toString());
					buf = new StringBuilder();
					msSwitchAfter = true;
				} else {
					// ===>minutes
					if (m != 0 || buf.length() == 0)
						return -1;
					m = Integer.valueOf(buf.toString());
					buf = new StringBuilder();
				}
				msSwitch = false;
			}

			if (Character.isDigit(c)) {
				buf.append(c);
			} else if (c == '.' || c == ' ') {
				// ignored
			} else if (c == 'h') {
				// ===>hours
				if (h != 0 || buf.length() == 0)
					return -1;
				h = Integer.valueOf(buf.toString());
				buf = new StringBuilder();
			} else if (c == 'm') {
				msSwitch = true;
			} else if (c == 's') {
				if (msSwitchAfter) {
					msSwitchAfter = false;
				} else {
					// ===>seconds
					if (s != 0 || buf.length() == 0)
						return -1;
					s = Integer.valueOf(buf.toString());
					buf = new StringBuilder();
				}
			} else
				return -1;
		}

		if (msSwitch == true) {
			// ===>minutes
			if (m != 0 || buf.length() == 0)
				return -1;
			m = Integer.valueOf(buf.toString());
			buf = new StringBuilder();
		}

		if ((buf.length() != 0) && ms == 0 && s == 0 && m == 0 && h == 0)
			return Long.valueOf(buf.toString()); // Sim-Units

		return millisecondUnit * (ms + 1000l * (s + 60l * (m + 60l * h)));
	}

	/**
	 * Returns a time string in the format:
	 * 
	 * <pre>
	 * --h--m--s--ms
	 * </pre>
	 * 
	 * where every "--" is a valid integer matching the trailing time unit.
	 */
	public String timeStringFromLong(final long time) {

		if (time < 0) return "Unknown";
		
		long hours = time / hour;
		long hoursDiv = hours * hour;

		long timeRem = time - hoursDiv;

		long minutes = timeRem / minute;
		long minutesDiv = minutes * minute;

		timeRem -= minutesDiv;

		long seconds = timeRem / second;
		long secondsDiv = seconds * second;

		timeRem -= secondsDiv;

		long milliseconds = timeRem / millisecondUnit;

		String result = (hours == 0) ? "" : hours + "h" + whiteSpaceCond();
		if (minutes != 0)
			result += minutes + "m" + whiteSpaceCond();
		if (seconds != 0)
			result += seconds + "s" + whiteSpaceCond();
		if (milliseconds != 0 || (hours == 0 && minutes == 0 && seconds == 0))
			result += milliseconds + "ms";

		return result;

	}
	
	String whiteSpaceCond() {
		return separateTimeUnitsWhitespace?" ":"";
	}

	public String richTimeStringFromLong(final long time)  {

		if (time < 0) return "Unbekannt";
		
		long days = time / day;
		long daysDiv = days * day;
		
		long timeRem = time - daysDiv;
		
		long hours = timeRem / hour;
		long hoursDiv = hours * hour;

		timeRem = time - hoursDiv;

		long minutes = timeRem / minute;
		long minutesDiv = minutes * minute;

		timeRem -= minutesDiv;

		long seconds = timeRem / second;

		String result = (days == 0) ? "" : days + whiteSpaceCond() + "d";
		if (hours != 0) {
			if (days != 0) result += "," + whiteSpaceCond();
			result += hours + " h";
		}
		if (minutes != 0) {
			if (hours != 0) result += "," + whiteSpaceCond();
			result += minutes + " m";
		}
		if (seconds != 0 || (hours == 0 && minutes == 0 && seconds == 0)) {
			if (minutes != 0) result += "," + whiteSpaceCond();
			result += seconds + " s";
		}

		return result;
	}
	
	static TimeToolkit pfsDef = new TimeToolkit(Time.MILLISECOND);
	
	public static TimeToolkit getPFSDefaultTimeToolkit() {
		return pfsDef;
	}

}
