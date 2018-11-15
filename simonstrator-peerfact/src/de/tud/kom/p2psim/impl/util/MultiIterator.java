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

import java.util.Iterator;

public class MultiIterator<T> implements Iterator<T> {

	private Iterator<Iterator<T>> its;
	
	Iterator<T> cur = null;

	public MultiIterator(Iterable<Iterator<T>> its) {
		this.its = its.iterator();
	}

	@Override
	public boolean hasNext() {
		while(cur == null || !cur.hasNext()) {
			if (!its.hasNext()) return false;
			cur = its.next();
		}
		return cur.hasNext();
	}

	@Override
	public T next() {
		if (hasNext()) return cur.next();
		return null;
	}

	@Override
	public void remove() {
		cur.remove();
	}

}
