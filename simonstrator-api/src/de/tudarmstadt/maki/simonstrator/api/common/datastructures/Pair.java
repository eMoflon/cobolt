/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
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

package de.tudarmstadt.maki.simonstrator.api.common.datastructures;

/***
 * Utility class, which holds a pair of the generic type T.
 *
 * @author Julian M. Klomp
 */
public class Pair<T> {
	/**
	 * The first object in the pair
	 */
	public final T x;

	/**
	 * The second object in the pair
	 */
	public final T y;

	/**
	 * Creates a pair out of {@code x} and {@code y}
	 */
	public Pair(final T x, final T y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns {@code true} if {@code o} is a {@link Pair} and its {@code x} and
	 * {@code y} elements are equal to those of this pair. Note that equality is
	 * specific to the ordering of {@code x} and {@code y}.
	 */
	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof Pair)) {
			return false;
		}
		final Pair<?> p = (Pair<?>) o;
		return (x == p.x || (x != null && x.equals(p.x))) && (y == p.y || (y != null && y.equals(p.y)));
	}

	@Override
	public int hashCode() {
		return ((x == null) ? 0 : x.hashCode()) ^ ((y == null) ? 0 : y.hashCode());
	}

	@Override
	public String toString() {
		return "{" + x + ", " + y + "}";
	}

	public T getFirst() {
		return x;
	}

	public T getSecond() {
		return y;
	}

	public T getX() {
		return getFirst();
	}

	public T getY() {
		return getSecond();
	}

}
