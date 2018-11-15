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


package de.tud.kom.p2psim.impl.util.timeoutcollections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * A map of objects where elements 'run out' after a certain amount of time and get
 * thrown out of the collection. Entry (k,v) (added via putNow(k,v)) is removed from this map
 * after a defined timeout since it is added.
 * <br><br>
 * You can add listeners to this set that announce elements that are removed from
 * this set during the cleanup process.
 * 
 * @author Leo Nobach
 *
 * @param <E>
 */
public class TimeoutMap<K, V> {

	Map<K, V> elements = new HashMap<K, V>();

	Queue<TimeoutObject> timeQ = new LinkedBlockingQueue<TimeoutObject>();

	List<ITimeoutMapListener<K, V>> listeners = new ArrayList<ITimeoutMapListener<K, V>>();

	long timeout;

	/**
	 * Creates a new TimeoutMap with the given timeout in simulation time units, 
	 * i.e. the time until entry (k,v) (added via putNow(k,v)) is removed from this map.
	 * @param timeout , the time until entry (k,v) (added via putNow(k,v)) is removed from this map.
	 */
	public TimeoutMap(long timeout) {
		this.timeout = timeout;
	}

	/**
	 * Puts an entry into this timeout map. The entry will
	 * time out in currentTime + timeout simulation time units.
	 * @param key
	 * @param value
	 */
	public void putNow(K key, V value) {
		cleanup();
		long time = getCurrentTime();
		if (elements.containsKey(key))
			timeQ.remove(new TimeoutObject(key, 0));
		elements.put(key, value);
		timeQ.add(new TimeoutObject(key, time + timeout));
		assertSet();
	}

	/**
	 * Returns whether this set contains the specified key.
	 * Behaves like java.util.Map
	 * @param element
	 * @return
	 */
	public boolean containsKey(K key) {
		cleanup();
		assertSet();
		return elements.containsKey(key);
	}
	
	/**
	 * Returns an unmodifiable Map view of this TimeoutMap.
	 * @return
	 */
	public Map<K, V> getUnmodifiableMap() {
		cleanup();
		return Collections.unmodifiableMap(elements);
	}

	/**
	 * Removes the entry with the given key from this map, although
	 * it is not timed out.
	 * Behaves like java.util Map
	 * @param key
	 * @return
	 */
	public boolean remove(K key) {
		// System.out.println("Q+SET-BEFORE: " + timeQ + ", " + elements +
		// " REMOVING: " + element);
		cleanup();
		boolean result = false;
		if (elements.containsKey(key)) {
			result = true;
			timeQ.remove(new TimeoutObject(key, 0));
			// System.out.println("SET CONTAINS ELEMENT");
		}
		elements.remove(key);
		assertSet();
		return result;
	}

	/**
	 * Returns the value associated with the given key.
	 * Behaves like java.util.Map
	 * @param key
	 * @return
	 */
	public V get(K key) {
		cleanup();
		return elements.get(key);
	}

	/**
	 * Returns the size of this TimeoutMap. Behaves like
	 * java.util.Map
	 * @return
	 */
	public int size() {
		cleanup();
		return elements.size();
	}

	private void assertSet() {
		if (elements.size() != timeQ.size())
			throw new AssertionError("Inequal sizes of queue and set: "
					+ timeQ.size() + ", " + elements.size());
	}

	/**
	 * Cleans up this map. Calling this method does not change anything in the semantics
	 * of the TimeoutMap, but should be done when you want to purge old entries from memory.
	 * Automatically done through other methods, if needed.
	 */
	public void cleanup() {
		long time = getCurrentTime();
		while (!timeQ.isEmpty() && timeQ.element().timeout <= time) {
			TimeoutObject obj = timeQ.remove();
			V value = elements.remove(obj.object);
			elementTimeouted(obj.object, value, obj.timeout);
		}
		assertSet();
	}

	protected long getCurrentTime() {
		return Time.getCurrentTime();
	}

	/**
	 * Adds an ITimeoutMapListener to this TimeoutMap.
	 * @param l
	 */
	public void addListener(ITimeoutMapListener<K, V> l) {
		listeners.add(l);
	}

	protected void elementTimeouted(K key, V value, long timeoutTime) {
		for (ITimeoutMapListener<K, V> l : listeners)
			l.elementTimeouted(this, key, value, timeoutTime);
	}

	protected class TimeoutObject {

		public TimeoutObject(K object, long timeout) {
			this.object = object;
			this.timeout = timeout;
		}

		public K object;

		public long timeout;

		public boolean equals(Object o) {
			return ((TimeoutObject) o).object.equals(this.object);
		}

		public int hashCode() {
			return this.object.hashCode();
		}
	}

}
