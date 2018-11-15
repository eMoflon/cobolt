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

import de.tud.kom.p2psim.impl.util.toolkits.TimeToolkit;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Simply a stopwatch.
 * 
 * Abstracts from global time. Should always be replaceable by local time.
 * 
 * @author leo
 * @version 1.0, mm/dd/2011
 */
public class Stopwatch {

	long timeSet = -1;
	
	public void set() {
		timeSet = Time.getCurrentTime();
	}
	
	public boolean wasSet() {
		return (timeSet >= 0);
	}
	
	public long getValue() {
		if (timeSet < 0) throw new IllegalStateException("The timer " + this + " was never set.");
		return Time.getCurrentTime() - timeSet;
	}
	
	public String toString() {
		return (timeSet < 0)?"[not set]":TimeToolkit.getPFSDefaultTimeToolkit().timeStringFromLong(Time.getCurrentTime() - timeSet);
	}
	
}
