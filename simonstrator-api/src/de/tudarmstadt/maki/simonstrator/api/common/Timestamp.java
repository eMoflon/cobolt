/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
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

package de.tudarmstadt.maki.simonstrator.api.common;

import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Immutable type that signifies a certain point in time.
 * 
 * @author Roland Kluge
 * 
 * @see Time
 */
public class Timestamp implements Comparable<Timestamp> {

	private final long value;

	public Timestamp(final long value) {
		this.value = value;
	}

	public long getValue() {
		return value;
	}

	@Override
	public int compareTo(Timestamp other) {
		return Long.compare(this.getValue(), other.getValue());
	}
}
