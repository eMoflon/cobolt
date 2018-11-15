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

import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class SortedHead<T> {

	private Comparator<T> comp;
	int headSize;
	SortedSet<T> head;
	
	public SortedHead(int headSize, Comparator<T> comp) {
		this.headSize = headSize;
		this.comp = comp;
		head = new TreeSet<T>(comp);
	}
	
	public boolean add(T elem) {
		if (headSize <= 0 || head.contains(elem)) return false;
		if (head.size() < headSize) {
			head.add(elem);
			return true;
		}
		if (comp.compare(head.last(), elem) > 0) {
			head.remove(head.last());
			head.add(elem);
			return true;
		}
		return false;
	}
	
	public SortedSet<T> getHead() {
		return Collections.unmodifiableSortedSet(head);
	}

	public void clear() {
		head.clear();
	}

	public boolean remove(T elem) {
		return head.remove(elem);
	}

	public boolean isEmpty() {
		return head.isEmpty();
	}

	public void addAll(Iterable<T> elems) {
		for (T elem : elems) {
			add(elem);
		}
	}
	
	public int getSize() {
		return head.size();
	}
	
	public boolean isFull() {
		return head.size() >= headSize;
	}

	public boolean wouldBeAdded(T elem) {
		if (headSize <= 0 || head.contains(elem)) return false;
		if (head.size() < headSize) return true;
		if (comp.compare(head.last(), elem) > 0) return true;
		return false;
	}
	
}
