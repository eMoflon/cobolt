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

package de.tudarmstadt.maki.simonstrator.api.component.topology;

import java.util.LinkedHashMap;

/**
 * Identifier for Topologies that is globally unique (i.e., depends on the
 * mechanism that exports the identifier - globally, and on the respective layer
 * within that mechanism - locally). This object is only to be used locally on a
 * node (i.e., it is not transmitable).
 * 
 * In simulations, we try to maintain only one instance per ID.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public final class TopologyID {

	private final String stringRepresentation;

	private final int hashUnique;

	private final static LinkedHashMap<Integer, TopologyID> instances = new LinkedHashMap<>();

	/**
	 * Returns the {@link TopologyID} for the given local ID and source
	 * component (must extend {@link TopologyProvider})
	 * 
	 * @param id
	 * @param sourceComponentClass
	 * @return
	 */
	public static <T extends TopologyProvider> TopologyID getIdentifier(
			String id, Class<T> sourceComponentClass) {
		assert id != null && sourceComponentClass != null;
		int hash = TopologyID.computeHash(id, sourceComponentClass);
		TopologyID instance = instances.get(hash);
		if (instance == null) {
			instance = new TopologyID(id + sourceComponentClass.getSimpleName(), hash);
			instances.put(hash, instance);
		}
		return instance;
	}
	
	/**
	 * Compute the hash for a given combination of string id and source class
	 * 
	 * @param id
	 * @param sourceComponentClass
	 * @return
	 */
	private static <T extends TopologyProvider> int computeHash(String id,
			Class<T> sourceComponentClass) {
		return (id + sourceComponentClass.getCanonicalName()).hashCode();
	}

	/**
	 * Private constructor, as TopologyIDs have to be created via the static
	 * method.
	 * 
	 * @param localId
	 *            a local ID (within the {@link TopologyProvider}
	 * @param sourceComponentClass
	 *            the {@link TopologyProvider} that sourced this identifier
	 */
	public <T extends TopologyProvider> TopologyID(String simpleName, int hash) {
		this.stringRepresentation = simpleName;
		this.hashUnique = hash;
	}

	/**
	 * Returns a human-readable name for this topology ID
	 * 
	 * @return the string representation of this
	 */
	public String getStringRepresentation() {
		return stringRepresentation;
	}

	/**
	 * String representation of the identifier. To ensure uniqueness, it is
	 * better to not operate on that string but to use the {@link TopologyID}
	 * object as key.
	 */
	@Override
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public int hashCode() {
		return hashUnique;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TopologyID other = (TopologyID) obj;
		if (hashUnique != other.hashUnique)
			return false;
		return true;
	}

}
