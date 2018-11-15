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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * A set of objects where elements 'run out' after a certain amount of time and get
 * thrown out of the collection. Element e (added via addNow(e)) is removed from this set
 * after a defined timeout since it is added.
 * <br><br>
 * You can add listeners to this set that announce elements that are removed from
 * this set during the cleanup process.
 * 
 * @author Leo Nobach
 *
 * @param <E>
 */
public class TimeoutSet<E> {

	Set<E> elements = new LinkedHashSet<E>();

	Queue<TimeoutObject> timeQ = new LinkedBlockingQueue<TimeoutObject>();

	List<ITimeoutSetListener<E>> listeners = new ArrayList<ITimeoutSetListener<E>>();

	long timeout;

	/**
	 * Creates a new TimeoutSet with the given timeout in simulation time units, 
	 * i.e. the time until element e (added via addNow(e)) is removed from this set.
	 * @param timeout , the time until element e (added via addNow(e)) is removed from this set.
	 */
	public TimeoutSet(long timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * Removes the oldest element that is currently i this set.
	 * @return
	 */
	public E removeOldest() {
		cleanup();
		TimeoutObject obj = timeQ.remove();
		elements.remove(obj.object);
		//assertSet();
		return obj.object;
	}
	
	public String toString() {
		return elements.toString();
	}
	
	/**
	 * Returns an unmodifiable view of this timeout set, using
	 * the interface java.util.Set
	 * @return
	 */
	public Set<E> getUnmodifiableSet() {
		cleanup();
		return Collections.unmodifiableSet(elements);
	}

	/**
	 * Adds an element to this timeout set. The element will
	 * time out in currentTime + timeout simulation time units.
	 * @param element
	 */
	public void addNow(E element) {
		cleanup();
		long time = getCurrentTime();
		if (elements.contains(element))
			timeQ.remove(new TimeoutObject(element, 0));
		elements.add(element);
		timeQ.add(new TimeoutObject(element, time + timeout));
		//assertSet();
	}

	/**
	 * Returns whether this set contains the specified element.
	 * Behaves like java.util.Set
	 * @param element
	 * @return
	 */
	public boolean contains(E element) {
		cleanup();
		//assertSet();
		return elements.contains(element);
	}

	/**
	 * Removes the given element from this set, although if it is
	 * not timeouted.
	 * Returns whether the given element was contained in this set.
	 * 
	 * @param element
	 * @return
	 */
	public boolean remove(E element) {
		// System.out.println("Q+SET-BEFORE: " + timeQ + ", " + elements +
		// " REMOVING: " + element);
		cleanup();
		boolean result = false;
		if (elements.contains(element)) {
			result = true;
			timeQ.remove(new TimeoutObject(element, 0));
			// System.out.println("SET CONTAINS ELEMENT");
		}
		elements.remove(element);
		//assertSet();
		return result;
	}

	/**
	 * Returns the size of this timeout set. Works like in java.util.Set
	 */
	public int size() {
		cleanup();
		return elements.size();
	}
	
	/**
	 * Returns whether this timeout set is empty. Works like in java.util.Set
	 * @return
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/*
	private void assertSet() {
		if (elements.size() != timeQ.size())
			throw new AssertionError("Inequal sizes of queue and set: "
					+ timeQ.size() + ", " + elements.size());
	}
	*/

	/**
	 * Cleans up this set. Calling this method does not change anything in the semantics
	 * of the TimeoutSet, but should be done when you want to purge old elements from memory.
	 * Automatically done by other methods.
	 * 
	 */
	public void cleanup() {
		long time = getCurrentTime();
		while (!timeQ.isEmpty() && timeQ.element().timeout <= time) {
			TimeoutObject obj = timeQ.remove();
			elements.remove(obj.object);
			elementTimeouted(obj.object, obj.timeout);
		}
		//assertSet();
	}

	protected long getCurrentTime() {
		return Time.getCurrentTime();
	}

	/**
	 * Adds a new ITimeoutCollectionListener to this timeout set.
	 * @param l
	 */
	public void addListener(ITimeoutSetListener<E> l) {
		listeners.add(l);
	}

	protected void elementTimeouted(E element, long timeoutTime) {
		for (ITimeoutSetListener<E> l : listeners)
			l.elementTimeouted(this, element, timeoutTime);
	}

	protected class TimeoutObject {

		public TimeoutObject(E object, long timeout) {
			this.object = object;
			this.timeout = timeout;
		}

		public E object;

		public long timeout;

		public boolean equals(Object o) {
			return ((TimeoutObject) o).object.equals(this.object);
		}

		public int hashCode() {
			return this.object.hashCode();
		}
	}

}
