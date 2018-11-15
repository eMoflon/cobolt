/*
 * Copyright (c) 2005-2013 KOM - Multimedia Communications Lab
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
 */

package de.tud.kom.p2psim.impl.util.timeoutcollections;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Holds a counter that is increased for a given time period. After the
 * specified timeout the counter will be reduced by the amount it has been
 * increased.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 19.01.13
 */
public class TimeoutSum {
	private long defaultTimeout;

	private long count = 0;

	private List<Tuple<Long, Long>> timeouts;

	public TimeoutSum(long defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
		timeouts = new LinkedList<TimeoutSum.Tuple<Long, Long>>();
	}

	public void increase(long value) {
		count += value;
		timeouts.add(new Tuple<Long, Long>(Time.getCurrentTime() + defaultTimeout, value));
	}

	public void increase(long value, long timeout) {
		count += value;
		timeouts.add(new Tuple<Long, Long>(Time.getCurrentTime() + timeout, value));
		
		Collections.sort(timeouts, new Comparator<Tuple<Long, Long>>() {
			@Override
			public int compare(Tuple<Long, Long> o1, Tuple<Long, Long> o2) {
				return o2.x.compareTo(o1.x);
			}
		});
		
	}

	public long get() {
		cleanup();
		return count;
	}

	private void cleanup() {
		
		long currentTime = Time.getCurrentTime();
		for (Iterator<Tuple<Long, Long>> iterator = timeouts.iterator(); iterator.hasNext();) {
			Tuple<Long, Long> next = iterator.next();
			if (next.x <= currentTime) {
				iterator.remove();
				count -= next.y;
			}
		}
	}

	private class Tuple<X, Y> {
		public final X x;

		public final Y y;

		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}
	}
}
