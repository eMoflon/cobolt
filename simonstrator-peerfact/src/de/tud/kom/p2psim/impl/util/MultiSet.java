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


package de.tud.kom.p2psim.impl.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Set that allows multiple occurrences of an object.
 * @author 
 *
 * @param <E>
 */
public class MultiSet<E> implements Iterable<Tuple<E, Integer>> {

	Map<E, Integer> elements = new HashMap<E, Integer>();

	public int getNumOccurrences(E elem) {
		Integer result = elements.get(elem);
		if (result == null) return 0;
		return result;
	}
	
	/**
	 * Adds the occurrences of the object given in the set elements
	 * @param elements
	 */
	public void addAll(MultiSet<E> otherElements) {
		for (Tuple<E, Integer> e : otherElements) {
			addMultiOccurrence(e.getA(), e.getB());
		}
	}
	
	/**
	 * Adds the occurrences of the object given in the set elements
	 * @param elements
	 */
	public void addOccurrences(Iterable<E> elements) {
		for (E element : elements) {
			addOccurrence(element);
		}
	}

	/**
	 * Adds a single occurrence element to the set.
	 * @param element
	 */
	public void addOccurrence(E element) {
		addMultiOccurrence(element, 1);
	}
	
	/**
	 * Adds multiple occurrences of the equal element to the set.
	 * @param element
	 */
	public void addMultiOccurrence(E element, int count) {
		Integer amount = elements.get(element);
		if (amount != null) {
			elements.put(element, amount + count);
		} else {
			elements.put(element, count);
		}
	}

	/**
	 * Removes a single occurrence from this set.
	 * @param element
	 * @return : false if this set does not contain any occurrence
	 * of this object.
	 */
	public boolean removeOccurrence(E element) {
		Integer amount = elements.get(element);
		if (amount != null) {

			if (amount > 1)
				elements.put(element, amount - 1);
			else
				elements.remove(element);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removes occurrences of the object given in the set elements.
	 * @param elements
	 */
	public void removeOccurrences(Iterable<E> elements) {
		for (E element : elements) {
			removeOccurrence(element);
		}
	}

	/**
	 * Returns whether this set contains at least one occurrence of the given
	 * element.
	 * @param element
	 * @return
	 */
	public boolean containsOccurrence(E element) {
		return elements.containsKey(element);
	}

	/**
	 * Returns the size of DIFFERENT elements in the set.
	 * @return
	 */
	public int size() {
		return elements.size();
	}

	@Override
	public String toString() {
		return elements.toString();
	}

	@Override
	public Iterator<Tuple<E, Integer>> iterator() {
		return new IteratorImpl(elements.entrySet().iterator());
	}
	
	class IteratorImpl implements Iterator<Tuple<E, Integer>> {
		
		private Iterator<Entry<E, Integer>> it;

		public IteratorImpl(Iterator<Entry<E, Integer>> it) {
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public Tuple<E, Integer> next() {
			Entry<E, Integer> e = it.next();
			return new Tuple<E, Integer>(e.getKey(), e.getValue());
		}

		@Override
		public void remove() {
			it.remove();
		}
		
	}

	public Map<E, Integer> getUnmodifiableMap() {
		return Collections.unmodifiableMap(elements);
	}

	public void clear() {
		elements.clear();
	}
	
	public MultiSet<Integer> createOccurrenceHistogram() {
		
		MultiSet<Integer> histo = new MultiSet<Integer>();
		
		for (Entry<E, Integer> e : elements.entrySet()) {
			histo.addOccurrence(e.getValue());
		}
		
		return histo;
	}

}
