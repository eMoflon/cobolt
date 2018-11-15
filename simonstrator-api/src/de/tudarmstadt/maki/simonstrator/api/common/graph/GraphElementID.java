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

package de.tudarmstadt.maki.simonstrator.api.common.graph;

import de.tudarmstadt.maki.simonstrator.api.common.UniqueID;

public abstract class GraphElementID implements UniqueID {

	private static final long serialVersionUID = 1L;
	private final long id;
	private final String stringId;
	private final int hashCode;

	protected GraphElementID(final long id) {
		this.id = id;
		this.stringId = Long.toString(this.id);
		this.hashCode = (int) (id ^ (id >>> 32));
	}

	/**
	 * Initializes the ID using a String
	 * 
	 * @param stringId
	 */
	protected GraphElementID(String stringId) {
		this(stringId, stringId.hashCode());
	}

	/**
	 * This constructor allows to set the hash code of this ID explicitly
	 */
	protected GraphElementID(String stringId, final int hashCode) {
		this.id = stringId.hashCode();
		this.stringId = stringId;
		this.hashCode = hashCode;
	}

	@Override
	public long value() {
		return id;
	}

	@Override
	public String valueAsString() {
		return this.stringId;
	}

	@Override
	public int getTransmissionSize() {
		// a long ID
		return 8;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphElementID other = (GraphElementID) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.stringId;
	}

	@Override
	public int compareTo(UniqueID o) {
		return Long.compare(id, o.value());
	}

}
