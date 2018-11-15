/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
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
 *˝
 */

package de.tud.kom.p2psim.impl.util.livemon;

import java.util.LinkedList;
import java.util.Queue;

import de.tud.kom.p2psim.impl.util.toolkits.NumberFormatToolkit;
import de.tudarmstadt.maki.simonstrator.api.Time;

public class EventsPerSecond {

	Object treeLock = new Object();
	
	Queue<Long> events;
	private long timeLookBack;
	
	public EventsPerSecond(long timeLookBack) {
		this.timeLookBack = timeLookBack;
		events = new LinkedList<Long>();
	}
	
	void cleanup() {
		long time2cutoff = getTime() - timeLookBack;
		Long lastEvent = events.peek();
		while (lastEvent != null && lastEvent <= time2cutoff) {
			events.remove();
			lastEvent = events.peek();
		}
	}
	
	public double getEventsPerSecond() {
		synchronized(treeLock) {
			cleanup();
			
			double timeF = timeLookBack / (double) Time.SECOND;
			
			return events.size()/timeF;
		}
	}
	
	public void eventOccured() {
		synchronized(treeLock) {
			cleanup();
			events.add(getTime());
		}
	}
	
	long getTime() {
		return Time.getCurrentTime();
	}
	
	public String toString() {
		return NumberFormatToolkit.floorToDecimalsString(getEventsPerSecond(), 2);
	}
	
}
